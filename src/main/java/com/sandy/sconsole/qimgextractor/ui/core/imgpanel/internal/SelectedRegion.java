package com.sandy.sconsole.qimgextractor.ui.core.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;

@Slf4j
class SelectedRegion {
    
    private static final Color TRANSLUCENT_GREEN = new Color( 0, 255, 0, 20 ) ;
    
    private Point  anchorPoint ;
    
    @Setter private String tag ;
    @Setter private int selectionFlag ;
    
    @Getter private Rectangle regionBounds ;
    @Getter private Rectangle repaintBounds ;
    
    SelectedRegion( Point anchorPoint ) {
        this.anchorPoint = anchorPoint ;
        
        regionBounds = new Rectangle( anchorPoint ) ;
        repaintBounds = new Rectangle( anchorPoint ) ;
    }
    
    SelectedRegion( ExtractedImgInfo regionInfo ) {
        anchorPoint = regionInfo.getAnchorPoint() ;
        tag = regionInfo.getTag() ;
        selectionFlag = regionInfo.getSelectionFlag() ;
        regionBounds = regionInfo.getRegionBounds() ;
        repaintBounds = new Rectangle( regionBounds.x - 1, regionBounds.y - 1,
                                       regionBounds.width + 2, regionBounds.height + 2 ) ;
    }
    
    public ExtractedImgInfo getRegionInfo() {
        ExtractedImgInfo info = new ExtractedImgInfo() ;
        info.setAnchorPoint( anchorPoint ) ;
        info.setTag( tag ) ;
        info.setSelectionFlag( selectionFlag ) ;
        info.setRegionBounds( regionBounds ) ;
        return info ;
    }

    public void scale( double scaleFactor ) {
        anchorPoint = SwingUtils.scale( anchorPoint, scaleFactor ) ;
        regionBounds = SwingUtils.scale( regionBounds, scaleFactor ) ;
        repaintBounds = SwingUtils.scale( repaintBounds, scaleFactor ) ;
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
