package com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient;

import lombok.Data;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Data
public class QuestionVO {
    
    private Integer id;
    private String  questionId;
    private String  syllabusName;
    private Integer topicId;
    private String  sourceId;
    private String  problemType;
    private Integer lctSequence;
    private Integer questionNumber;
    private String  answer;
    private Date    serverSyncTime;
    private List<QuestionImageVO> questionImages = new ArrayList<>();
}
