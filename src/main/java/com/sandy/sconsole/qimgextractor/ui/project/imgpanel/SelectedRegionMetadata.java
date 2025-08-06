package com.sandy.sconsole.qimgextractor.ui.project.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import lombok.Data;
import lombok.ToString;

import java.awt.*;
import java.io.Serializable;

@Data
@ToString
public class SelectedRegionMetadata implements Serializable, Cloneable {

    private Point anchorPoint ;
    private String tag ;
    private Rectangle regionBounds ;
    
    public void scale( double scaleFactor ) {
        anchorPoint = SwingUtils.scale( anchorPoint, scaleFactor ) ;
        regionBounds = SwingUtils.scale( regionBounds, scaleFactor ) ;
    }
    
    @Override
    public SelectedRegionMetadata clone() {
        SelectedRegionMetadata clone = new SelectedRegionMetadata();
        clone.setTag( this.tag );
        clone.setAnchorPoint( new Point( this.anchorPoint ) );
        clone.setRegionBounds( new Rectangle( this.regionBounds ) );
        return clone;
    }
}
