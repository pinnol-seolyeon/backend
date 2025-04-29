package jpabasic.pinnolbe.dto.question;


import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class QAs {

    private int part;
    List<QuestionAnswer> questionAnswers;
}
