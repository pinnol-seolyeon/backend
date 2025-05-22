package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.dto.study.ChapterDto;
import jpabasic.pinnolbe.dto.study.ChaptersDto;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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


    //학습하기
    public ChapterDto getChapterContents(Study study) {

        // 전까지 완료했던 데부터 시작..
        Chapter chapter = study.getChapter();
        String chapterId=chapter.getId().toString();

        //Dto로 변환해서 리턴
        ChapterDto dto=ChapterDto.convertDto(chapterId,chapter);
        System.out.println("✅"+dto.getChapterId()); ///objectId

        // 본격적인 학습 시작
        return dto;
    }



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

        Book book=bookRepository.findById(bookId)
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

        user.setStudy(study);
        userRepository.save(user);

        return study;
    }

    //책 선택 후 단원 선택 시, 해당 책의 단원 리스트 제공
    //수정 : chapterId+chapterTitle만 반환
    public List<ChaptersDto> getChapterTitles(String bookId){
        Book book=bookRepository.findById(bookId)
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


    //학습 완료

    //String chapterId로 chapter찾기
    public Chapter getChapterByString(String id){
        ObjectId realId=new ObjectId(id);
        Chapter chapter=chapterRepository.findById(realId)
                .orElseThrow(()->new IllegalArgumentException("해당 단원이 없음"));
        return chapter;
    }
}
