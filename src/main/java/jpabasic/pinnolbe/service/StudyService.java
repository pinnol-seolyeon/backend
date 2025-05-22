package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
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
    public Chapter getChapterContents(String bookId) {
        Study study=studyRepository.findByBookId(bookId)
                .orElseThrow(()->new IllegalArgumentException("이 책에 해당하는 Study 엔티티가 존재하지 않음."));

        // 전까지 완료했던 데부터 시작..
        Chapter chapter = study.getChapter();
        System.out.println("✅"+chapter.getChapterTitle());

        // 본격적인 학습 시작
        return chapter;
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


    //책에 대한 처음 시작
    public Study startBook(User user,String bookId){

        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없음."));

        List<Chapter> chapters = book.getChapters();

        if (chapters == null || chapters.isEmpty()) {
            throw new IllegalStateException("책에 단원이 존재하지 않습니다: " + bookId);
        }

        Chapter firstChapter = chapters.get(0);

        Study study=new Study(user.getId(),bookId,firstChapter);
        studyRepository.save(study);

        user.setStudy(study);
        userRepository.save(user);

        return study;
    }

    //책 선택 후 단원 선택 시, 해당 책의 단원 리스트 제공
    public List<Chapter> getChapterTitles(String bookId){
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없음."));

        List<Chapter> chapters = book.getChapters();
        List<ChaptersDto> chapterDtos = new ArrayList<>();

        for (Chapter chapter : chapters) {
            String id = chapter.getId();
            String idString = (id != null) ? id : "null";

            chapterDtos.add(new ChaptersDto(idString, chapter.getChapterTitle()));
        }

        for (Chapter chapter : chapters) {
            System.out.println("chapter raw = " + chapter); // toString 확인
            System.out.println("chapter id = " + chapter.getId());
            System.out.println("chapter title = " + chapter.getChapterTitle());
        }



        System.out.println("✏️✏️" + chapterDtos);
        return chapters;
    }

    public String getChapterTitle(String chapterId){
        Chapter chapter=chapterRepository.findById(chapterId)
                .orElseThrow(()->new IllegalArgumentException("해당 단원이 없음"));
        return chapter.getChapterTitle();
    }



    ///chapterRepository에 S3 image url 저장
    public void saveImgUrl(String chapterId,String fileUrl){
        Chapter chapter=chapterRepository.findById(chapterId)
                .orElseThrow(()->new IllegalArgumentException("해당 단원이 없음"));
        chapter.setImgUrl(fileUrl);


        chapterRepository.save(chapter);
        //StudyRepository, BookRepository도 수정해야함..
    }


    //학습 완료
}
