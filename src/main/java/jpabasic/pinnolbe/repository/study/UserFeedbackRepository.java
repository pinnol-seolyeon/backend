package jpabasic.pinnolbe.repository.study;

import jpabasic.pinnolbe.domain.study.UserFeedback;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserFeedbackRepository extends CrudRepository<UserFeedback, String> {
}
