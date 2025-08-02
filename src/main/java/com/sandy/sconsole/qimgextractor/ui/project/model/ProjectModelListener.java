package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgInfo;

public interface ProjectModelListener {
    
    void newSubImgAdded( PageImage pageImage, SubImgInfo newRegionInfo ) ;
}
