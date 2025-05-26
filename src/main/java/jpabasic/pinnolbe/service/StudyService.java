package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.dto.study.*;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.study.BookRepository;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import jpabasic.pinnolbe.repository.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import retrofit2.http.Multipart;

import java.io.File;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudyService {

  private final BookRepository bookRepository;
  private final ChapterRepository chapterRepository;
  private final StudyRepository studyRepository;
  private final UserRepository userRepository;

//    public StudyService(BookRepository bookRepository, ChapterRepository chapterRepository, StudyRepository studyRepository, UserRepository userRepository) {
//        this.bookRepository = bookRepository;
//        this.chapterRepository = chapterRepository;
//        this.studyRepository = studyRepository;
//        this.userRepository = userRepository;
//    }


    //이미 학습했던 단원 다시 클릭
    public ChapterDto getOnceLearned(String studyId) {

        ObjectId objectId=new ObjectId(studyId);
        Study study=studyRepository.findById(objectId).orElseThrow(()-> new IllegalArgumentException("Study documentation 조회 오류"));

        // 전까지 완료했던 데부터 시작..
        Chapter chapter = study.getChapter();
        String chapterId=chapter.getId().toString();

        //Dto로 변환해서 리턴
        ChapterDto dto=ChapterDto.convertDto(chapterId,chapter);
        System.out.println("✅"+dto.getChapterId()); ///objectId

        // 본격적인 학습 시작
        return dto;
    }

    //학습하고 싶은 단원 선택
    public ChapterDto getChapterContents(User user,String chapterId) {
        //유저의 Study Document 찾기
        String studyId=user.getStudyId();
        ObjectId objectId=new ObjectId(studyId);
        Study study=studyRepository.findById(objectId)
                .orElseThrow(()-> new IllegalArgumentException("Study documentation 조회 오류"));

        Set<CompletedChapter> completedChapters=study.getCompleteChapter();

        //해당 챕터 공부한 적 있는지 확인
        boolean isAlreadyCompleted=completedChapters.stream()
                .anyMatch(c->c.getChapterId().equals(chapterId));

        if(isAlreadyCompleted){
            Chapter chapter=getChapterByString(chapterId);
            ChapterDto dto=ChapterDto.convertDto(chapterId,chapter);
            System.out.println("✅이미 학습한 단원이예요:"+dto.getChapterId());
            return dto;
        }else {
            ChapterDto dto = getOnceLearned(studyId);
            System.out.println("✅이제 진도를 나가볼까요?" + dto.getChapterId()); ///objectId
            return dto;

        }

    }


    //ch



//    //학습목표
//    public String getChapterObjective(int bookId, Study study){
//        String chapterId = study.getChapterId();
//        Optional<Chapter> chapterOpt = chapterRepository.findById(chapterId);
//
//        if (chapterOpt.isPresent()) {
//            return chapterOpt.get().getObjective();
//        } else {
//            throw new IllegalArgumentException("해당 chapterId에 대한 챕터가 존재하지 않습니다: " + chapterId);
//        }
//    }


    //책에 대한 처음 시작 //수정완료
    public Study startBook(User user,String bookId){
        ObjectId objectId=new ObjectId(bookId);
        Book book=bookRepository.findById(objectId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없음."));

        List<String> chapters = book.getChapters();

        if (chapters == null || chapters.isEmpty()) {
            throw new IllegalStateException("책에 단원이 존재하지 않습니다: " + bookId);
        }

        String firstChapterID = chapters.get(0);
        ObjectId firstChapter=new ObjectId(firstChapterID);

        Chapter nowChapter=chapterRepository.findById(firstChapter)
                .orElseThrow(()->new IllegalArgumentException("해당 chapter를 찾을 수 없음"));

        Study study=new Study(user.getId(),bookId,nowChapter);
        studyRepository.save(study);
        String studyId=study.getId().toString();


        user.setStudyId(studyId);
        userRepository.save(user);

        return study;
    }

    //책 선택 후 단원 선택 시, 해당 책의 단원 리스트 제공
    //수정 : chapterId+chapterTitle만
    //2차 수정 : 현재 진도 + 학습 완료한 단원 Boolean 값 반환
    public List<ChaptersDto> getChapterTitles(String bookId){
        ObjectId objectId=new ObjectId(bookId);
        Book book=bookRepository.findById(objectId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없음."));

        List<String> chapterIds = book.getChapters();
        List<ChaptersDto> chapterDtos = new ArrayList<>();

        for (String chapterId : chapterIds) {
            ObjectId realId=new ObjectId(chapterId);
            Chapter chapter=chapterRepository.findById(realId)
                    .orElseThrow(()->new IllegalArgumentException("해당 단원 없음"+chapterId));
            chapterDtos.add(new ChaptersDto(chapter.getId().toString(),chapter.getChapterTitle()));

        }

        System.out.println("✏️✏️" + chapterDtos);
        return chapterDtos;
    }

    //현재진도 + 학습 완료한 단원 Boolean 값 추가
    public List<ChaptersDto> getCurrentProgress(List<ChaptersDto> chapterDtos,String studyId){
        Study study=getStudyByString(studyId);

        //completeChapter 리스트의 chapterId에 없는 단원들은 잠금 상태
        //완료된 단원들의 id 목록
        Set<CompletedChapter> completed=study.getCompleteChapter();
        /// completed가 null이면 빈 Set 리턴 ///아니면 스트림 처리 -> Set<String> 생성
        Set<String> completedIds=completed==null?Set.of():
                completed.stream().map(CompletedChapter::getChapterId).collect(Collectors.toSet()); //CompletedChapter에서 chapterId만 추출 //스트림 결과를 Set<String>으로 수집

        //현재 진도 단원 ID
        String currentId=study.getChapter()!=null?study.getChapter().getId().toString():null;

        //각 chapterDto에 상태 반영
        for(ChaptersDto dto:chapterDtos){
            String dtoId=dto.getId();

            dto.setIsCompleted(completedIds.contains(dtoId)); //완료 여부
            dto.setIsCurrent(currentId!=null&&currentId.equals(dtoId)); //현재 진도 여부

        }

        return chapterDtos;
    }



    public String getChapterTitle(String chapterId){
        Chapter chapter=getChapterByString(chapterId);
        return chapter.getChapterTitle();
    }



    ///chapterRepository에 S3 image url 저장
    public void saveImgUrl(String chapterId,String fileUrl){
        Chapter chapter=getChapterByString(chapterId);
        chapter.setImgUrl(fileUrl);


        chapterRepository.save(chapter);
        //StudyRepository, BookRepository도 수정해야함..
    }


    //학습 완료 //학습 완료 시 나오는 화면 or 나오는 로직 설정해야..
    public void finishChapter(String chapterId,String studyId){
        Chapter chapter=getChapterByString(chapterId);
        ObjectId objectId=new ObjectId(studyId);
        Study study=studyRepository.findById(objectId)
                .orElseThrow(()-> new IllegalArgumentException("해당 user의 Study를 찾을 수 없어요"));

        if(study.getCompleteChapter()==null){
            study.setCompleteChapter(new HashSet<>());
        }

        //완료된 단원 리스트에 추가
        study.getCompleteChapter().add(new CompletedChapter(chapterId,LocalDateTime.now()));

        //현재 학습중인 단원 제거 or 다음 단원으로 교체
//        study.setChapter(null);
        study.setChapter(getNextChapter(study));

        studyRepository.save(study);
    }

    public StudyStatsDto getStudyStats(String userId) {
        Study study = studyRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("해당 user의 Study를 찾을 수 없어요"));

        Set<CompletedChapter> completed = study.getCompleteChapter();
        if (completed == null) return new StudyStatsDto(0, 0);

        int total = completed.size();

        LocalDate now = LocalDate.now();
        LocalDate weekStart = now.with(DayOfWeek.MONDAY);

        int weekly = (int) completed.stream()
                .filter(c -> c.getCompletedAt().toLocalDate().isAfter(weekStart.minusDays(1)))
                .count();

        return new StudyStatsDto(total, weekly);
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


    //3단계 학습하기: AI와 상호작용 후 답변 저장
    public void saveFeedback(User user,FeedBackRequest request){
        String userId=user.getId();


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
    }

