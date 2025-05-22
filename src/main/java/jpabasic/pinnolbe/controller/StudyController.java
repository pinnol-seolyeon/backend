package jpabasic.pinnolbe.controller;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.constraints.Positive;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.study.Chapter;
import jpabasic.pinnolbe.domain.study.Study;
import jpabasic.pinnolbe.dto.study.ChapterDto;
import jpabasic.pinnolbe.dto.study.ChaptersDto;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.study.StudyRepository;
import jpabasic.pinnolbe.service.StudyService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/study")
@Slf4j
public class StudyController {
    private final StudyRepository studyRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final StudyService studyService;
    private final AmazonS3 amazonS3;
    private final AmazonS3Client amazonS3Client;

    public StudyController(StudyRepository studyRepository, UserRepository userRepository, UserService userService, StudyService studyService, AmazonS3 amazonS3, AmazonS3Client amazonS3Client) {
        this.studyRepository = studyRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.studyService = studyService;
        this.amazonS3 = amazonS3;
        this.amazonS3Client = amazonS3Client;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    //실제 학습
    // getStudy 방식 고쳐야 함 -> 현재는 책이 어차피 한 권이므로 상관X
    // user-study-chapter 관계 수정 필요
    
    @GetMapping("/start")
    @Operation(summary="해당 단원 학습하기") //문장 단위로 끊어서 보여주기..
    public ResponseEntity<ChapterDto> getChapterContents(@RequestParam String bookId){
        User user=userService.getUserInfo();
        Study study=user.getStudy();
        System.out.println("🐛🐛"+study);

        ChapterDto chapter=studyService.getChapterContents(study);
        return ResponseEntity.ok(chapter);
    }



    // 어떤 책으로 공부할지 선택
    @GetMapping("")
    @Operation(summary="새로운 책의 학습 시작")
    public ResponseEntity<List<ChaptersDto>> startBook(@RequestParam String bookId){
        User user=userService.getUserInfo();
        if(user.getStudy()==null) {
            Study study = studyService.startBook(user, bookId);
        }

        //책 선택 후 단원이 보이는 화면 get
        List<ChaptersDto> chapterList=studyService.getChapterTitles(bookId);

        return ResponseEntity.ok(chapterList);
    }

    @GetMapping("/chapter")
    @Operation(summary="단원리스트에서 단원 선택후 단원명 GET")
    public ResponseEntity<String> getChapterTitle(@RequestParam String chapterId){
        String title=studyService.getChapterTitle(chapterId);
        return ResponseEntity.ok(title);
    }



    @PostMapping(value="/upload-image",consumes="multipart/form-data")
    @Operation(summary="S3에 학습하기 이미지 업로드+db에 fileURl 저장")
    //chapterId에 대한 로직 검토 필요.. 현재 프론트에서 chatperId 매핑이 안되는 문제 있음
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam String chapterId){
        /// memberId : 파일과 멤버키값(파일이름)을 전달하여 저장 작업 진행
        try{

            //확장자 추출
            String extension=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
            String keyName="test/"+chapterId+"."+extension; //키를 chapterId로

            ObjectMetadata metadata=new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());


            PutObjectRequest putObjectRequest=new PutObjectRequest(bucket,keyName,file.getInputStream(),metadata);
            
            amazonS3Client.putObject(putObjectRequest);
            String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/"+ keyName;


            studyService.saveImgUrl(chapterId,fileUrl); //DB에 이미지 url 저장
            return ResponseEntity.ok(fileUrl);
        }catch(Exception e){
            e.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
    }

  
}
