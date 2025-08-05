package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;

public interface ProjectModelListener {
    
    void newSubImgAdded( PageImage pageImage, SubImgInfo newRegionInfo ) ;
}
