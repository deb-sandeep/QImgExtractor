package com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient;

import lombok.Data;

@Data
public class QuestionImageVO {
    private Integer sequence;
    private Integer pageNumber;
    private String  fileName;
    private Boolean lctCtxImage;
    private Integer partNumber;
    private Integer imgWidth;
    private Integer imgHeight;
    private String  imgData;
}
