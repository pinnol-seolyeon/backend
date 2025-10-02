package jpabasic.pinnolbe.service;

import jpabasic.pinnolbe.domain.StudySession;
import jpabasic.pinnolbe.domain.User;
import jpabasic.pinnolbe.repository.redis.StudySessionRedisRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StudySessionService {

    @Autowired
    private StudySessionRedisRepository repo;

    public void startLevel(User user, int level, String chapterId) {
        String userId=user.getId();
        StudySession studySession = new StudySession(userId, level);
        repo.save(studySession); //redis에 저장
    }

}
