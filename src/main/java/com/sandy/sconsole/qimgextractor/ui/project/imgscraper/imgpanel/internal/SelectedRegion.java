package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.SelectedRegionMetadata;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
class SelectedRegion {
    
    private static final Color TRANSLUCENT_GREEN = new Color( 0, 255, 0, 20 ) ;
    
    private Point  anchorPoint ;
    
    @Getter @Setter
    private String tag ;
    
    @Getter private Rectangle regionBounds ;
    @Getter private Rectangle repaintBounds ;
    
    // The region in original image coordinates. Display bounds are always
    // derived from these and the absolute scale factor, never by rescaling
    // the previous display bounds: repeated rescaling truncates to whole
    // pixels each time, and the error accumulates as the boxes drift up and
    // left of the content they enclose.
    private Point     modelAnchorPoint ;
    private Rectangle modelRegionBounds ;
    
    SelectedRegion( Point anchorPoint ) {
        this.anchorPoint = anchorPoint ;
        
        regionBounds = new Rectangle( anchorPoint ) ;
        repaintBounds = new Rectangle( anchorPoint ) ;
    }
    
    SelectedRegion( SelectedRegionMetadata regionInfo ) {
        anchorPoint = regionInfo.getAnchorPoint() ;
        tag = regionInfo.getTag() ;
        regionBounds = regionInfo.getRegionBounds() ;
        modelAnchorPoint = new Point( anchorPoint ) ;
        modelRegionBounds = new Rectangle( regionBounds ) ;
        repaintBounds = new Rectangle( regionBounds.x - 1, regionBounds.y - 1,
                                       regionBounds.width + 2, regionBounds.height + 2 ) ;
    }
    
    public SelectedRegionMetadata getRegionInfo() {
        SelectedRegionMetadata info = new SelectedRegionMetadata() ;
        info.setAnchorPoint( anchorPoint ) ;
        info.setTag( tag ) ;
        info.setRegionBounds( regionBounds ) ;
        return info ;
    }

    // Records the original image coordinates of a region drawn on the view,
    // which is displayed at the given scale factor.
    void setModelCoordinates( double viewScaleFactor ) {
        modelAnchorPoint = SwingUtils.scale( anchorPoint, 1/viewScaleFactor ) ;
        modelRegionBounds = SwingUtils.scale( regionBounds, 1/viewScaleFactor ) ;
    }
    
    // Sets the display bounds for the given absolute scale factor.
    public void scaleTo( double scaleFactor ) {
        anchorPoint = new Point( (int)Math.round( modelAnchorPoint.x * scaleFactor ),
                                 (int)Math.round( modelAnchorPoint.y * scaleFactor ) ) ;
        regionBounds = new Rectangle( (int)Math.round( modelRegionBounds.x * scaleFactor ),
                                      (int)Math.round( modelRegionBounds.y * scaleFactor ),
                                      (int)Math.round( modelRegionBounds.width * scaleFactor ),
                                      (int)Math.round( modelRegionBounds.height * scaleFactor ) ) ;
        repaintBounds = new Rectangle( regionBounds.x - 1, regionBounds.y - 1,
                                       regionBounds.width + 2, regionBounds.height + 2 ) ;
    }
    
    Rectangle updateRegion( Point curPt   ) {

        Rectangle oldRect = new Rectangle( regionBounds ) ;
        
        if( curPt.x < anchorPoint.x ) {
            regionBounds.x = curPt.x ;
            regionBounds.width = anchorPoint.x - curPt.x ;
        }
        else {
            regionBounds.x = anchorPoint.x ;
            regionBounds.width = curPt.x - anchorPoint.x ;
        }
        
        if( curPt.y < anchorPoint.y ) {
            regionBounds.y = curPt.y ;
            regionBounds.height = anchorPoint.y - curPt.y ;
        }
        else {
            regionBounds.y = anchorPoint.y ;
            regionBounds.height = curPt.y - anchorPoint.y ;
        }
        
        repaintBounds = regionBounds.union( oldRect ) ;
        repaintBounds.setRect( repaintBounds.x-1, repaintBounds.y-1, repaintBounds.width+2, repaintBounds.height+2 ) ;
        
        return repaintBounds ;
    }
    
    void paintAsOldRegion( Graphics2D g ) {
        g.setColor( TRANSLUCENT_GREEN );
        g.fillRect( regionBounds.x, regionBounds.y, regionBounds.width, regionBounds.height ) ;
        
        g.setColor( Color.GREEN ) ;
        g.drawRect( regionBounds.x, regionBounds.y, regionBounds.width, regionBounds.height ) ;
        
        if( this.tag != null ) {
            printTag( g ) ;
        }
    }
    
    private void printTag( Graphics2D g ) {
        FontMetrics fm = g.getFontMetrics() ;
        int tagWidth = fm.stringWidth( tag ) ;
        
        g.setColor( Color.BLUE ) ;
        g.drawString( tag,
                      regionBounds.x + regionBounds.width - tagWidth - 2,
                      regionBounds.y + regionBounds.height - 2 ) ;
    }

    void paint( Graphics2D g ) {
        g.setColor( Color.RED ) ;
        g.drawRect( regionBounds.x, regionBounds.y, regionBounds.width, regionBounds.height ) ;
    }
}
