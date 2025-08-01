package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgInfo;

import java.util.ArrayList;
import java.util.List;

public class Question {

    private List<SubImgInfo> subImgInfoList = new ArrayList<>() ;
    
    public List<SubImgInfo> getSubImgInfoList() {
        return subImgInfoList ;
    }
    
    public void addSubImgInfo( SubImgInfo subImgInfo ) {
        this.subImgInfoList.add( subImgInfo ) ;
    }
}
