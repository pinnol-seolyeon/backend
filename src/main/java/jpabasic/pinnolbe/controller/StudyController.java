package jpabasic.pinnolbe.controller;

import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.study.StudyRepository;
import jpabasic.pinnolbe.service.StudyService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@Slf4j
public class StudyController {
    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final StudyService studyService;
    public StudyController(StudyRepository studyRepository,UserRepository userRepository,UserService userService,StudyService studyService) {
        this.studyRepository = studyRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.studyService = studyService;
    }


    //실제 학습
    @GetMapping("/start")
    @Operation(summary="해당 단원 학습하기") //문장 단위로 끊어서 보여주기..
    public ResponseEntity<String> getChapterContents(@RequestParam int bookId){
        User user=userService.getUserInfo();
        Study study=user.getStudy();

        String contents=studyService.getChapterContents(bookId,study);
        return ResponseEntity.ok(contents);
    }



    // 어떤 책으로 공부할지 선택
    @GetMapping("")
    @Operation(summary="새로운 책의 학습 시작")
    public ResponseEntity<List<String>> startBook(@RequestParam int bookId){
        User user=userService.getUserInfo();
        if(user.getStudy()==null) {
            Study study = studyService.startBook(user, bookId);
        }
            //책 선택 후 단원이 보이는 화면 get
            List<String> chapterList=studyService.getChapterTitles(bookId);

        return ResponseEntity.ok(chapterList);
    }
}
