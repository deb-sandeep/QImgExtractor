package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import lombok.Data;
import lombok.ToString;

import java.awt.*;
import java.io.Serializable;

@Data
@ToString
public class ExtractedImgInfo implements Serializable {

    private Point anchorPoint ;
    private String tag ;
    private int selectionFlag ;
    private Rectangle regionBounds ;
    
    public void scale( double scaleFactor ) {
        anchorPoint = SwingUtils.scale( anchorPoint, scaleFactor ) ;
        regionBounds = SwingUtils.scale( regionBounds, scaleFactor ) ;
    }
}
