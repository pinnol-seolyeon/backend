package jpabasic.pinnolbe.service.study;

import jpabasic.pinnolbe.domain.Status;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.StudySessionLog;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.domain.study.*;
import jpabasic.pinnolbe.dto.analyze.StudySessionSummaryDto;
import jpabasic.pinnolbe.dto.study.*;
import jpabasic.pinnolbe.dto.study.book.BookListResponseDto;
import jpabasic.pinnolbe.dto.study.chapter.ChapterListResponseDto;
import jpabasic.pinnolbe.dto.study.feedback.AiResponseResponseDto;
import jpabasic.pinnolbe.dto.study.feedback.ReactionRequestDto;
import jpabasic.pinnolbe.global.ErrorCode;
import jpabasic.pinnolbe.global.exception.user.CustomException;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.analyze.StudySessionLogRepository;
import jpabasic.pinnolbe.repository.analyze.WeeklyAnalysisRepository;
import jpabasic.pinnolbe.repository.study.*;
import jpabasic.pinnolbe.service.facade.SessionFacade;
import jpabasic.pinnolbe.service.model.AskQuestionTemplate;
import lombok.RequiredArgsConstructor;

import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

import static jpabasic.pinnolbe.dto.study.ChapterDto.convertDto;

@Service
@RequiredArgsConstructor
public class StudyService {
  private static final int FIRST_STUDY_LEVEL = 1;

  private final BookRepository bookRepository;
  private final ChapterRepository chapterRepository;
  private final UserRepository userRepository;
  private final AskQuestionTemplate askQuestionTemplate;
  private final StudySessionLogRepository studySessionLogRepository;
  private final WeeklyAnalysisRepository weeklyAnalysisRepository;
    private final SessionFacade sessionFacade;

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
                List<QuizItem> quizItems = chapterDto.getQuizItems();
                Collections.shuffle(quizItems);
                result.put("quiz", quizItems.stream().limit(5).toList());
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


    //학습 완료
    public void finishChapter(User user,StudySessionSummaryDto summaryDto){
        String chapterId=summaryDto.getChapterId();
        String userId= user.getId();
        StudySessionLog log;
        Chapter chapter=getChapterByString(chapterId);
        Book currentBook = getCurrentBook(summaryDto.getBookId(), chapter);
        Chapter nextChapter;

        //6단계까지 완료 -> 완료한 단원의 StudySessionLog 모두 삭제
        List<StudySessionLog> list = studySessionLogRepository.findByChapterIdAndUserId(chapterId, userId);
        try {
            studySessionLogRepository.deleteAll(list);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.SESSION_LOG_DELETE_ERROR);
        }

        //다음 챕터 탐색
        Optional<Chapter> nextChapterOpt = getNextChapterInBook(currentBook, chapter);

        //학습해야할 다음 단원을 담은 StudySessionLog 생성
        if(nextChapterOpt.isPresent()){
            nextChapter=nextChapterOpt.get();

            log=new StudySessionLog();
            log.setUserId(userId);
            log.setBookId(currentBook.getId());
            log.setChapterId(String.valueOf(nextChapter.getId()));
            log.setLevel(1);
            log.setStatus(Status.NOT_STARTED); //아직 학습 시작안했다는 상태 반영

            System.out.println("✔️ log 업데이트: "+ log);
        }else{ //이미 해당 교재의 모든 단원을 마무리함
            Optional<Book> nextBookOpt = getNextBook(currentBook.getId());
            log=new StudySessionLog();
            log.setUserId(userId);

            if (nextBookOpt.isPresent()) {
                Book nextBook = nextBookOpt.get();
                String nextChapterId = getFirstChapterId(nextBook.getId());
                log.setBookId(nextBook.getId());
                log.setChapterId(nextChapterId);
                log.setLevel(FIRST_STUDY_LEVEL);
                log.setStatus(Status.NOT_STARTED);
                System.out.println("✔️ 다음 교재로 이동: bookLevel=" + nextBook.getBookLevel()
                        + ", bookId=" + nextBook.getId()
                        + ", chapterId=" + nextChapterId);
            } else {
                log.setBookId(null);
                log.setChapterId(null);
                System.out.println("✔️ 모든 교재 학습 완료");
            }
        }
        StudySessionLog sessionLog=studySessionLogRepository.save(log);
        String id=sessionLog.getId();

        user.setStudySessionLogId(id);
        userRepository.save(user);
        System.out.println("✔️ 학습 완료 : 다음 진도 sessionLog 생성 완료");
    }



    //3단계 학습하기: AI와 상호작용 후 답변 저장 //수정 요망
    public AiResponseResponseDto getReaction(User user, ReactionRequestDto request) {
        String userId = user.getId();

        // AI에 유저의 질문 전달
        try {
            //현재 chapter진도 session으로부터 받아오기
            StudySession studySession=sessionFacade.getCurrentSession();
            String chapterId=studySession.getChapterId();
            //ai 답변 생성
            AiResponseResponseDto answer = askQuestionTemplate.reactionByAI(chapterId,request, userId);

            // AI의 답변 내용을 반환
            return answer;
        } catch (RestClientException e) {
            throw new RuntimeException("AI 서버 호출 중 오류 발생", e);
        }


    }

    //String chapterId로 chapter찾기
    public Chapter getChapterByString(String id){
        ObjectId realId=new ObjectId(id);
        Chapter chapter=chapterRepository.findById(realId)
                .orElseThrow(()->new IllegalArgumentException("해당 단원이 없음"));
        return chapter;
    }

    /**
     * 책 리스트 제공
     */
    public BookListResponseDto getBookList(User user){
        String currentBookId;
        Integer currentBookLevel;
        String currentSessionLogId = null;

        //모든 책 리스트
        List<Book> books=bookRepository.findAll(Sort.by(Sort.Order.asc("bookLevel")));

        //현재 진행 중인 교재
        String sessionLogId=user.getStudySessionLogId();
        if (sessionLogId == null) {
            System.out.println("첫 학습이어서 첫번째 교재 자동 설정");
            Book firstBook = getFirstBook(books);
            currentBookId = firstBook.getId();
            currentBookLevel = firstBook.getBookLevel();
        } else {
            Optional<StudySessionLog> optLog = studySessionLogRepository.findById(sessionLogId);
            if (optLog.isPresent() && optLog.get().getBookId() != null) {
                System.out.println("✔️ 현재 진행중인 교재가 있음");
                StudySessionLog log = optLog.get();
                currentSessionLogId = sessionLogId;
                currentBookId = log.getBookId();
                currentBookLevel = getBookByString(currentBookId).getBookLevel();
            } else if (optLog.isPresent()) {
                currentSessionLogId = sessionLogId;
                currentBookId = null;
                currentBookLevel = null;
            } else {
                Book firstBook = getFirstBook(books);
                currentBookId = firstBook.getId();
                currentBookLevel = firstBook.getBookLevel();
            }
        }
        List<Map<String,String>> bookList=BookListResponseDto.toDto(books, currentBookLevel);
        //dto로 변환
        BookListResponseDto result=new BookListResponseDto(currentSessionLogId,currentBookId,currentBookLevel,bookList);
        return result;
    }

    /**
     * 단원 리스트 제공
     */
    public ChapterListResponseDto getChapterList(User user, String bookId){
        String currentChapterId = null;
        Integer currentLevel = null;
        String currentSessionLogId = null;

        //해당 교재의 모든 chapter List
        List<ChapterListResponseDto.ChapterResponseDto> chapters=getChaptersByBook(bookId);

        //현재 진행 중인 chapter
        String sessionLogId=user.getStudySessionLogId();
        if (sessionLogId == null) {
            System.out.println("첫 학습이어서 첫번째 챕터로 자동 설정");
            Book firstBook = getFirstBook();
            if (firstBook.getId().equals(bookId)) {
                currentChapterId = getFirstChapterId(bookId);
                currentLevel = FIRST_STUDY_LEVEL;
            }
        } else {
            Optional<StudySessionLog> optLog = studySessionLogRepository.findById(sessionLogId);
            if (optLog.isPresent() && bookId.equals(optLog.get().getBookId())) {
                StudySessionLog log = optLog.get();
                System.out.println("currentChapterId 가져오기");
                currentSessionLogId = sessionLogId;
                currentChapterId = log.getChapterId();
                currentLevel = log.getLevel();
            } else if (optLog.isEmpty()) {
                Book firstBook = getFirstBook();
                if (firstBook.getId().equals(bookId)) {
                    currentChapterId = getFirstChapterId(bookId);
                    currentLevel = FIRST_STUDY_LEVEL;
                }
            }
        }
        //dto로 변환
        ChapterListResponseDto result=new ChapterListResponseDto(currentSessionLogId,currentChapterId,currentLevel,chapters);
        return result;
    }

    public List<ChapterListResponseDto.ChapterResponseDto> getChaptersByBook(String bookId) {
        Book book = getBookByString(bookId);
        List<Chapter> chapters;

        if (hasChapterList(book)) {
            chapters = book.getChapters().stream()
                    .map(this::getChapterByString)
                    .toList();
        } else {
            chapters = chapterRepository.findByBookId(bookId, Sort.by("order").ascending());
        }

        return chapters.stream()
                .map(ChapterListResponseDto.ChapterResponseDto::fromEntity)
                .toList();
    }

    private Book getFirstBook() {
        return getFirstBook(bookRepository.findAll(Sort.by(Sort.Order.asc("bookLevel"), Sort.Order.asc("id"))));
    }

    private Book getFirstBook(List<Book> books) {
        return books.stream()
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    }

    private Book getBookByString(String bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new CustomException(ErrorCode.BOOK_NOT_FOUND));
    }

    private Book getCurrentBook(String requestBookId, Chapter chapter) {
        String currentBookId = Optional.ofNullable(requestBookId)
                .filter(id -> !id.isBlank())
                .orElse(chapter.getBookId());
        Book currentBook = getBookByString(currentBookId);

        if (hasChapterList(currentBook) && !currentBook.getChapters().contains(chapter.getId().toString())) {
            throw new CustomException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        if (!hasChapterList(currentBook) && !currentBookId.equals(chapter.getBookId())) {
            throw new CustomException(ErrorCode.CHAPTER_NOT_FOUND);
        }
        return currentBook;
    }

    private Optional<Chapter> getNextChapterInBook(Book currentBook, Chapter currentChapter) {
        if (hasChapterList(currentBook)) {
            List<String> chapterIds = currentBook.getChapters();
            int currentIndex = chapterIds.indexOf(currentChapter.getId().toString());
            if (currentIndex < 0 || currentIndex + 1 >= chapterIds.size()) {
                return Optional.empty();
            }
            return Optional.of(getChapterByString(chapterIds.get(currentIndex + 1)));
        }

        return chapterRepository.findByBookIdAndOrder(currentBook.getId(), currentChapter.getOrder() + 1)
                .filter(nextChapter -> currentBook.getId().equals(nextChapter.getBookId()));
    }

    private boolean hasChapterList(Book book) {
        return book.getChapters() != null && !book.getChapters().isEmpty();
    }

    private String getFirstChapterId(String bookId) {
        Book book = getBookByString(bookId);
        if (hasChapterList(book)) {
            return book.getChapters().get(0);
        }

        return chapterRepository.findByBookId(bookId, Sort.by("order").ascending()).stream()
                .findFirst()
                .map(chapter -> chapter.getId().toString())
                .orElseThrow(() -> new CustomException(ErrorCode.CHAPTER_NOT_FOUND));
    }

    private Optional<Book> getNextBook(String currentBookId) {
        Book currentBook = getBookByString(currentBookId);
        return bookRepository.findAll(Sort.by(Sort.Order.asc("bookLevel"), Sort.Order.asc("id"))).stream()
                .filter(book -> book.getBookLevel() > currentBook.getBookLevel())
                .min(Comparator.comparingInt(Book::getBookLevel));
    }

}
