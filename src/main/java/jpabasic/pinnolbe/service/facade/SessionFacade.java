package jpabasic.pinnolbe.service.facade;

import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.domain.redis.StudySession;
import jpabasic.pinnolbe.service.login.UserService;
import jpabasic.pinnolbe.service.study.StudySessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SessionFacade {

    private final UserService userService;
    private final StudySessionService studySessionService;

    //현재 로그인한 유저의 학습 세션을 Redis에서 조회
    public StudySession getCurrentSession(){
        User user=userService.getUserInfo();
        return studySessionService.getSessionByUser(user);
    }

    //특정 유저의 학습 세션 조회
    public StudySession getSessionByUser(User user){
        return studySessionService.getSessionByUser(user);
    }
}
