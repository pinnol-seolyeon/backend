package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Book;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.study.BookRepository;
import jpabasic.pinnolbe.repository.study.ChapterRepository;
import jpabasic.pinnolbe.repository.study.StudyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

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
    public String getChapterContents(int bookId, Study study) {

        // 전까지 완료했던 데부터 시작..
        Chapter chapter = study.getChapter();

        // 본격적인 학습 시작
        return chapter.getContent();
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
    public List<String> getChapterTitles(String bookId){
        Book book=bookRepository.findById(bookId)
                .orElseThrow(()->new IllegalArgumentException("해당 책이 없음."));

        List<Chapter> chapters = book.getChapters();
        List<String> titles = new ArrayList<>();

        for (Chapter chapter : chapters) {
             titles.add(chapter.getChapterTitle());
        }

        System.out.println("✏️✏️" + titles);
        return titles;
    }

    //학습 완료
}
