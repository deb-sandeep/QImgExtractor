package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class QuestionImageCluster {

    protected final List<QuestionImage> qImgList = new ArrayList<>() ;
    
    @Getter
    protected final QID qID ;
    
    protected QuestionImageCluster( QID qID ) {
        this.qID = qID ;
    }
    
    public void addQImg( QuestionImage qImg ) {
        qImgList.add( qImg ) ;
        qImgList.sort( Comparator.comparing( QuestionImage::getPartNumber ) ) ;
    }
}
