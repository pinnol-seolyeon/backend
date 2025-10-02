//package jpabasic.pinnolbe.service;
//
//import jpabasic.pinnolbe.dto.study.heartbeat.HeartBeatRequest;
//import jpabasic.pinnolbe.dto.study.heartbeat.HeartBeatResponse;
//import jpabasic.pinnolbe.dto.study.heartbeat.Status;
//import org.springframework.stereotype.Service;
//
//import java.time.Duration;
//import java.time.LocalDateTime;
//
//@Service
//public class HeartBeatService {
//
//    public HeartBeatResponse handleHeartbeat(HeartBeatRequest req, String userId){
//
//        //해당 chapter가 completed인 경우
//        if(req.getStatus()== Status.COMPLETED){
//            //분석 로직
//
//            //기존 해당 chapter 세션 로그 DB에서 삭제
//
//        }else{
//            //세션 로그 DB에 저장
//        }
//
//    }
//
//    //학습 지속 시간 측정
//    private long getDurationTime(HeartBeatRequest req) {
//        LocalDateTime startTime = req.getStartTime();
//        LocalDateTime endTime = req.getEndTime();
//        return Duration.between(startTime, endTime).toMinutes();
//    }
//
//    //학습 완료한 단원 (DB 수정 후)
//    private void getCompletedChapter(){
//
//    }
//}
