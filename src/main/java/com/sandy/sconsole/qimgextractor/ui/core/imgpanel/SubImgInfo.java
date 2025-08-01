package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import lombok.Data;
import lombok.ToString;

import java.awt.*;
import java.io.Serializable;

@Data
@ToString
public class SubImgInfo implements Serializable {

    private Point anchorPoint ;
    private String tag ;
    private int selectionFlag ;
    private Rectangle regionBounds ;
    
    private transient QuestionImage questionImage ;
    
    public void scale( double scaleFactor ) {
        anchorPoint = SwingUtils.scale( anchorPoint, scaleFactor ) ;
        regionBounds = SwingUtils.scale( regionBounds, scaleFactor ) ;
    }
}
