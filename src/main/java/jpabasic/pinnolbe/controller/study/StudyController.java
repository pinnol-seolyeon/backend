package jpabasic.pinnolbe.controller.study;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import io.swagger.v3.oas.annotations.Operation;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.analyze.WeeklyAnalysis;
import jpabasic.pinnolbe.dto.question.QuestionResponse;
import jpabasic.pinnolbe.dto.study.book.BookListResponseDto;
import jpabasic.pinnolbe.dto.study.chapter.ChapterListResponseDto;
import jpabasic.pinnolbe.dto.study.feedback.FeedBackRequestDto;
import jpabasic.pinnolbe.global.ApiResponse;
import jpabasic.pinnolbe.repository.UserRepository;
import jpabasic.pinnolbe.repository.study.StudyRepository;
import jpabasic.pinnolbe.service.analyze.WeeklyAnalysisService;
import jpabasic.pinnolbe.service.study.StudyService;
import jpabasic.pinnolbe.service.study.StudySessionService;
import jpabasic.pinnolbe.service.login.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    private final StudySessionService studySessionService;
    private final WeeklyAnalysisService weeklyAnalysisService;

    public StudyController(StudyRepository studyRepository, UserRepository userRepository, UserService userService, StudyService studyService, AmazonS3 amazonS3, AmazonS3Client amazonS3Client, StudySessionService studySessionService, WeeklyAnalysisService weeklyAnalysisService) {
        this.studyRepository = studyRepository;
        this.userRepository = userRepository;
        this.userService = userService;
        this.studyService = studyService;
        this.amazonS3 = amazonS3;
        this.amazonS3Client = amazonS3Client;
        this.studySessionService = studySessionService;
        this.weeklyAnalysisService = weeklyAnalysisService;
    }

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


//    @GetMapping("/start")
//    @Operation(summary="해당 단원 학습하기(AI 연동 전)") //문장 단위로 끊어서 보여주기..
//    public ResponseEntity<ChapterDto> getChapterContents(@RequestParam String chapterId){
//        User user=userService.getUserInfo();
//        String studyId=user.getStudyId();
//        System.out.println("🐛🐛"+studyId);
//
//        ChapterDto chapter=studyService.getChapterContents(user,chapterId);
//        return ResponseEntity.ok(chapter);
//    }

//    @PostMapping("/feedback")
//    @Operation(summary="유저가 대답하면 AI가 피드백/리액션(수정 전)")
//    public ResponseEntity<QuestionResponse> handleFeedback(@RequestBody FeedBackRequestDto request){
//        System.out.println("🎙선생님의 질문:"+request.getQuestion());
//        System.out.println("🎙사용자 답변:"+request.getUserAnswer());
//        User user=userService.getUserInfo();
//
//       String reaction="좋은 생각이야~";
//
//        Map<String,String> response=new HashMap<>();
//        response.put("reaction",reaction);
//        return ResponseEntity.ok(response);
//        QuestionResponse res=studyService.getFeedback(user,request);
//        return ResponseEntity.ok(res);
//    }

    
//    @PostMapping("/feedback/saveAll")
//    @Operation(summary="여태까지의 피드백 한꺼번에 DB에 저장(수정 전)")
//    public ResponseEntity<String> saveAllFeedBacks(@RequestParam String chapterId){
//        User user=userService.getUserInfo();
//        try {
//            studyService.saveAllFeedBacks(user, chapterId);
//        }catch(Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//        return ResponseEntity.ok("여태까지의 피드백이 DB에 무사히 저장되었습니다.");
//    }


    @GetMapping("/book-select")
    @Operation(summary="교재 리스트 제공")
    public ApiResponse<BookListResponseDto> getBookList(){
        User user=userService.getUserInfo();
        BookListResponseDto result=studyService.getBookList(user);
        return ApiResponse.success(null,result);
    }


    @GetMapping("/chapter-select")
    @Operation(summary="교재 선택 후, 해당 교재의 챕터 리스트 및 현재 학습 중인 챕터/레벨 제공",
                description = " isAvailable=false는 이번 주 할당량 학습 완료를 뜻한다. ")
    public ApiResponse<ChapterListResponseDto> getChapterTitle(
            @RequestParam String bookId,
            @RequestParam(defaultValue="0") int page){
        User user=userService.getUserInfo();

        WeeklyAnalysis analysis=weeklyAnalysisService.findThisWeekAnalysis(user.getId());
        int size=analysis.getCompletedChapters().size();
        ChapterListResponseDto result=studyService.getChapterList(user,bookId,page);
        if(size>=2){
            result.setIsAvailable(false);
            return ApiResponse.success("이미 이번 주 할당량 학습을 모두 완료하였어요.", result);
        }
        return ApiResponse.success("챕터 목록 조회 성공",result);
    }



//    @PostMapping("/finish")
//    @Operation(summary="학습완료(수정 전)")
//    public ResponseEntity<String> finishChapter(@RequestParam String chapterId){
//        User user=userService.getUserInfo();
//        String studyId=user.getStudyId();
//        String chapterTitle=studyService.getChapterTitle(chapterId);
//
//        try {
//            studyService.finishChapter(chapterId, studyId);
//        }catch(Exception e) {
//            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
//        }
//        return ResponseEntity.ok(chapterTitle+"학습이 완료되었습니다!");
//    }



//    @PostMapping(value="/upload-image",consumes="multipart/form-data")
//    @Operation(summary="S3에 학습하기1단계 이미지 업로드+db에 fileURl 저장(수정 전)")
//    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam String chapterId){
//        /// memberId : 파일과 멤버키값(파일이름)을 전달하여 저장 작업 진행
//        try{
//
//            //확장자 추출
//            String extension=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
//            String keyName="test/"+chapterId+"."+extension; //키를 chapterId로
//
//            ObjectMetadata metadata=new ObjectMetadata();
//            metadata.setContentType(file.getContentType());
//            metadata.setContentLength(file.getSize());
//
//
//            PutObjectRequest putObjectRequest=new PutObjectRequest(bucket,keyName,file.getInputStream(),metadata);
//
//            amazonS3Client.putObject(putObjectRequest);
//            String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/"+ keyName;
//
//
//            studyService.saveImgUrl(chapterId,fileUrl); //DB에 이미지 url 저장
//            return ResponseEntity.ok(fileUrl);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }
//



//    @PostMapping(value="/upload-summaryImg",consumes="multipart/form-data")
//    @Operation(summary="S3에 학습하기1단계 이미지 업로드+db에 fileURl 저장(수정 전)")
//    public ResponseEntity<String> uploadSummaryImgFile(@RequestParam("file") MultipartFile file, @RequestParam String chapterId){
//        /// memberId : 파일과 멤버키값(파일이름)을 전달하여 저장 작업 진행
//        try{
//
//            //확장자 추출
//            String extension=file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".")+1);
//            String keyName="summary/"+chapterId+"."+extension; //키를 chapterId로
//
//            ObjectMetadata metadata=new ObjectMetadata();
//            metadata.setContentType(file.getContentType());
//            metadata.setContentLength(file.getSize());
//
//
//            PutObjectRequest putObjectRequest=new PutObjectRequest(bucket,keyName,file.getInputStream(),metadata);
//
//            amazonS3Client.putObject(putObjectRequest);
//            String fileUrl = "https://" + bucket + ".s3.ap-northeast-2.amazonaws.com/"+ keyName;
//
//
//            studyService.saveSummaryImgUrl(chapterId,fileUrl); //DB에 이미지 url 저장
//            return ResponseEntity.ok(fileUrl);
//        }catch(Exception e){
//            e.printStackTrace();
//        }
//        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
//    }




  
}
