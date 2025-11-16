package jpabasic.pinnolbe.service.study;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.study.*;
import jpabasic.pinnolbe.dto.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.dto.study.*;
import jpabasic.pinnolbe.dto.study.book.BookListResponseDto;
import jpabasic.pinnolbe.dto.study.chapter.ChapterListResponseDto;
import jpabasic.pinnolbe.dto.study.feedback.FeedBackRequestDto;
import jpabasic.pinnolbe.dto.study.feedback.FeedBackResponse;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.analyze.StudySessionLogRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.study.*;
import jpabasic.pinnolbe.service.analyze.QuizService;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import lombok.RequiredArgsConstructor;
import org.apache.http.HttpHeaders;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static jpabasic.pinnolbe.dto.study.ChapterDto.convertDto;

@Service
@RequiredArgsConstructor
public class StudyService {

  private final BookRepository bookRepository;
  private final ChapterRepository chapterRepository;
  private final StudyRepository studyRepository;
  private final UserRepository userRepository;
  private final Map<String,FeedBackResponse> sessionStore=new ConcurrentHashMap<>();
  private final UserFeedbackRepository userFeedbackRepository;
  private final AskQuestionTemplate askQuestionTemplate;
  private final QuizRepository quizRepository;

  @Autowired
  WebClient webClient;
    @Autowired
    private StudySessionLogRepository studySessionLogRepository;
    @Autowired
    private WeeklyAnalysisRepository weeklyAnalysisRepository;



    //학습하고 싶은 단원 선택
    public Map<String,Object> getChapterContents(String chapterId,int level) {
        Chapter chapter=chapterRepository.findById(chapterId)
                .orElseThrow(()->new CustomException(ErrorCode.CHAPTER_NOT_FOUND));

        //chapter 본문 내용 받아오기
        ChapterDto chapterDto=convertDto(chapterId,chapter);
        Map<String,Object> result=getLevelContents(level,chapterDto);
        return result;
    }

    /// level 별로 학습할 컨텐츠 제공
    Map<String,Object> getLevelContents(int level,ChapterDto chapterDto){
        Map<String,Object> result=new HashMap<>();
        String chapterId=chapterDto.getChapterId();
        result.put("chapterId",chapterId);

        switch(level){
            case 1: {
                String chapterTitle=chapterDto.getChapterTitle();
                result.put("chapterTitle",chapterTitle);
                break;
            }
            case 2:{
                String objective = chapterDto.getObjective();
                String objectiveQuestion = chapterDto.getObjectiveQuestion();
                result.put("objective", objective);
                result.put("objectiveQuestion", objectiveQuestion);
                System.out.println("result" + result);
                break;
            }
            case 3:{
                String content=chapterDto.getContent();
                String imgUrl=chapterDto.getImgUrl();
                result.put("content",content);
                result.put("imgUrl",imgUrl);
                System.out.println("result:"+result);
                break;
            }
            case 4: {
                List<Quiz> quiz = quizRepository.findByChapterId(chapterId);
                Collections.shuffle(quiz);
                result.put("quiz", quiz.stream().limit(5).toList());
            }
            case 5:{
                String summary=chapterDto.getSummary();
                String summaryImgUrl=chapterDto.getSummaryImgUrl();
                result.put("summary",summary);
                result.put("summaryImgUrl",summaryImgUrl);
                System.out.println("result:"+result);
                break;
            }
            case 6:{
                String topic=chapterDto.getTopic();
                result.put("topic", topic);
                System.out.println("result:"+result);
                break;
            }
        }
        return result;
    }


    ///chapterRepository에 S3 image url 저장
    public void saveImgUrl(String chapterId,String fileUrl){
        Chapter chapter=getChapterByString(chapterId);
        chapter.setImgUrl(fileUrl);


        chapterRepository.save(chapter);
        //StudyRepository, BookRepository도 수정해야함..
    }

    ///chapterRepository에 S3 image url 저장
    public void saveSummaryImgUrl(String chapterId,String fileUrl){
        Chapter chapter=getChapterByString(chapterId);
        chapter.setSummaryImgUrl(fileUrl);


        chapterRepository.save(chapter);
        //StudyRepository, BookRepository도 수정해야함..
    }


    //학습 완료
    public void finishChapter(User user,StudySessionSummaryDto summaryDto){
        String chapterId=summaryDto.getChapterId();
        String userId= summaryDto.getUserId();
        StudySessionLog log;
        Chapter chapter=getChapterByString(chapterId);
        Chapter nextChapter;

        //6단계까지 완료 -> 완료한 단원의 StudySessionLog 모두 삭제
        List<StudySessionLog> list = studySessionLogRepository.findByChapterIdAndUserId(chapterId, userId);
        try {
            studySessionLogRepository.deleteAll(list);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_LOG_DELETE_ERROR);
        }

        //다음 챕터 탐색
        Optional<Chapter> nextChapterOpt=chapterRepository
                .findByBookIdAndOrder(chapter.getBookId(),chapter.getOrder()+1);

        //학습해야할 다음 단원을 담은 StudySessionLog 생성
        if(nextChapterOpt.isPresent()){
            nextChapter=nextChapterOpt.get();

            log=new StudySessionLog();
            log.setUserId(summaryDto.getUserId());
            log.setBookId(chapter.getBookId());
            log.setChapterId(String.valueOf(nextChapter.getId()));
            log.setLevel(1);

            System.out.println("✔️ log 업데이트: "+ log);
        }else{ //이미 해당 교재의 모든 단원을 마무리함
            log=new StudySessionLog();
            log.setUserId(summaryDto.getUserId());
            log.setBookId(null);
            log.setChapterId(null);
        }
        StudySessionLog sessionLog=studySessionLogRepository.save(log);
        String id=sessionLog.getId();

        user.setStudySessionLogId(id);
        userRepository.save(user);
        System.out.println("✔️ 학습 완료 : 다음 진도 sessionLog 생성 완료");
    }

    // 학습 참여도 (이번주 학습 완료 단원 수) 가져오기
    public StudyStatsDto getStudyStats(String userId) {
        //이번주 weeklyAnalysis 엔티티 가져오기
        LocalDate weekStart = LocalDate.now(ZoneId.of("Asia/Seoul"))
                .with(DayOfWeek.MONDAY);

        WeeklyAnalysis weeklyAnalysis =
                weeklyAnalysisRepository.findByUserIdAndWeekStartDate(userId,weekStart)
                        .orElseGet(() -> new WeeklyAnalysis(userId,weekStart));

        List<String> completed=weeklyAnalysis.getCompletedChapters();
        if (completed == null) return new StudyStatsDto(0);

        int total = completed.size();

//        int weekly = (int) completed.stream()
//                .filter(c -> c.getCompletedAt().toLocalDate().isAfter(weekStart.minusDays(1)))
//                .count();

        return new StudyStatsDto(total);
    }

    //학습 완료 후 다음 단원으로 이동
    public Chapter getNextChapter(Study study){
        ObjectId objectId=new ObjectId(study.getBookId());
        Book book=bookRepository.findById(objectId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없어요."));
        List<String> chapterIds=book.getChapters(); //순서 보장된 단원 ID 리스트
        ObjectId currentChapterId=study.getChapter().getId();

        //현재 단원의 인덱스 탐색
        int index=chapterIds.indexOf(currentChapterId.toString());
        if (index==-1){
            throw new IllegalStateException("현재 단원이 책의 chapter 목록에 없습니다.");
        }

        //다음 단원이 존재하면 반환
        if (index+1<chapterIds.size()){
            String nextChapterId=chapterIds.get(index+1);
            return getChapterByString(nextChapterId);
        }else{
            return study.getChapter(); //책에 대한 모든 단원 마무리-> 현재의 마지막 단원으로 그대로 저장
        }
    }


    //3단계 학습하기: AI와 상호작용 후 답변 저장 //수정 요망
    public QuestionResponse getFeedback(User user, FeedBackRequestDto request){
        String userId= user.getId();
        String question=request.getQuestion();

        // AI에 유저의 질문 전달
        try {
            /// AI 수정 필요
            QuestionResponse answer = askQuestionTemplate.feedbackQuestionToAI(request);


            //사용자 세션 가져오기
            FeedBackResponse session=sessionStore.computeIfAbsent(userId, k->new FeedBackResponse());
            session.add(request.getQuestion(),request.getUserAnswer(),answer.getResult());

            // AI의 답변 내용을 반환
            return answer;
        }catch(RestClientException e){
            throw new RuntimeException("AI 서버 호출 중 오류 발생", e);
        }


    }


    //모든 피드백 저장
    public void saveAllFeedBacks(User user,String chapterId){
        String userId=user.getId();
        FeedBackResponse session=sessionStore.get(userId);

        if(session==null||session.getQuestions().isEmpty()) return;

        UserFeedback doc=new UserFeedback();
        doc.setUserId(userId);
        doc.setQuestions(session.getQuestions());
        doc.setUserAnswers(session.getUserAnswers());
        doc.setChapterId(chapterId);
        LocalDateTime nowKST=LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        doc.setDate(nowKST);

        userFeedbackRepository.save(doc);

        //저장 후 세션 초기화  //sessionStore에서 key가 userId인 entry하나만 삭제
        sessionStore.remove(userId);

    }



    //String chapterId로 chapter찾기
    public Chapter getChapterByString(String id){
        ObjectId realId=new ObjectId(id);
        Chapter chapter=chapterRepository.findById(realId)
                .orElseThrow(()->new IllegalArgumentException("해당 단원이 없음"));
        return chapter;
    }

    //String studyId로 study 찾기
    public Study getStudyByString(String id){
        ObjectId realId=new ObjectId(id);
        Study study=studyRepository.findById(realId)
                .orElseThrow(()->new IllegalArgumentException("해당 Study 없음"));
            return study;
    }


    //카카오톡으로 피드백 전송하기
    public void sendFeedback(String accessToken,FeedBackRequestDto request){
        createFeedback(accessToken,request);
    }

    //에러 핸들링 추가하기
    public Mono<String> createFeedback(String accessToken, FeedBackRequestDto request){
        return webClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) //카카오에서 발급한 access Token 주입
                .body(Mono.just(request), FeedBackRequestDto.class) //request 객체를 JSON으로 직렬화에서 넣기
                .retrieve()
                .bodyToMono(String.class);
    }

    /**
     * 책 리스트 제공
     */
    public BookListResponseDto getBookList(User user){
        StudySessionLog log;
        String currentBookId;

        //모든 책 리스트
        List<Book> books=bookRepository.findAll();
        List<Map<String,String>> bookList=BookListResponseDto.toDto(books);

        //현재 진행 중인 교재
        String sessionLogId=user.getStudySessionLogId();
        if (sessionLogId == null) {
            System.out.println("첫 학습이어서 첫번째 교재 자동 설정");
            currentBookId = "682829208c776a1ffa92fd4d"; // 첫 교재 하드코딩
        } else {
            Optional<StudySessionLog> optLog = studySessionLogRepository.findById(sessionLogId);
            if (optLog.isPresent()) {
                System.out.println("✔️ 현재 진행중인 교재가 있음");
                log = optLog.get();
                currentBookId = log.getBookId();
            } else {
                currentBookId = "682829208c776a1ffa92fd4d"; // fallback
            }
        }
        //dto로 변환
        BookListResponseDto result=new BookListResponseDto(sessionLogId,currentBookId,bookList);
        return result;
    }

    /**
     * 단원 리스트 제공
     */
    public ChapterListResponseDto getChapterList(User user, String bookId,int page){
        String currentChapterId;
        StudySessionLog log;
        int currentLevel;

        //해당 교재의 모든 chapter List
        Slice<ChapterListResponseDto.ChapterResponseDto> chapters=getChaptersByBook(bookId,page,5);

        //현재 진행 중인 chapter
        String sessionLogId=user.getStudySessionLogId();
        if (sessionLogId == null) {
            System.out.println("첫 학습이어서 첫번째 챕터로 자동 설정");
            currentChapterId = "682829708c776a1ffa92fd50"; // 첫 교재,첫 챕터 하드코딩
            currentLevel = 1;
        } else {
            Optional<StudySessionLog> optLog = studySessionLogRepository.findById(sessionLogId);
            if (optLog.isPresent()) {
                log = optLog.get();
                System.out.println("currentChapterId 가져오기");
                currentChapterId = log.getChapterId();
                currentLevel = log.getLevel();
            } else {
                currentChapterId = "682829708c776a1ffa92fd50"; // fallback
                currentLevel = 1;
            }
        }
        //dto로 변환
        ChapterListResponseDto result=new ChapterListResponseDto(sessionLogId,currentChapterId,currentLevel,chapters);
        return result;
    }

    public Slice<ChapterListResponseDto.ChapterResponseDto> getChaptersByBook(String bookId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").ascending());
        Slice<Chapter> slice = chapterRepository.findByBookId(bookId, pageable);

        return slice.map(ChapterListResponseDto.ChapterResponseDto::fromEntity);
    }

}

