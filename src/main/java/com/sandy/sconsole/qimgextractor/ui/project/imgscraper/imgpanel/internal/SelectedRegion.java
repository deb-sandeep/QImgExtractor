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

    // Bit flags identifying the edges being dragged during a resize.
    static final int EDGE_LEFT   = 1 ;
    static final int EDGE_RIGHT  = 2 ;
    static final int EDGE_TOP    = 4 ;
    static final int EDGE_BOTTOM = 8 ;

    // Distance (in view pixels) from an edge within which it can be grabbed.
    private static final int EDGE_GRAB_TOLERANCE = 4 ;

    // Smallest width/height (in original image pixels) a region can be resized to.
    private static final int MIN_MODEL_SIZE = 5 ;

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
    
    Rectangle getModelRegionBounds() {
        return new Rectangle( modelRegionBounds ) ;
    }

    // Returns the EDGE_* flags of the edges within grabbing distance of the
    // given view point, or 0 if the point is not on the region's border.
    int getEdgesAt( Point p ) {
        Rectangle r = regionBounds ;
        int t = EDGE_GRAB_TOLERANCE ;

        boolean inXSpan = p.x >= r.x - t && p.x <= r.x + r.width + t ;
        boolean inYSpan = p.y >= r.y - t && p.y <= r.y + r.height + t ;

        int edges = 0 ;
        if( inYSpan ) {
            if( Math.abs( p.x - r.x ) <= t ) edges |= EDGE_LEFT ;
            else if( Math.abs( p.x - ( r.x + r.width ) ) <= t ) edges |= EDGE_RIGHT ;
        }
        if( inXSpan ) {
            if( Math.abs( p.y - r.y ) <= t ) edges |= EDGE_TOP ;
            else if( Math.abs( p.y - ( r.y + r.height ) ) <= t ) edges |= EDGE_BOTTOM ;
        }
        return edges ;
    }

    // Moves the given edges to the view point, displayed at the given scale.
    // The change is made in model coordinates and only for the dragged edges,
    // so the edges which are not dragged don't drift due to rounding. The
    // region is kept within the image bounds and above a minimum size.
    // Returns the view area to be repainted.
    Rectangle resize( int edges, Point viewPt, double scaleFactor, Dimension modelImgSize ) {

        Rectangle oldRepaintBounds = new Rectangle( repaintBounds ) ;

        int mx = (int)Math.round( viewPt.x / scaleFactor ) ;
        int my = (int)Math.round( viewPt.y / scaleFactor ) ;

        int left   = modelRegionBounds.x ;
        int top    = modelRegionBounds.y ;
        int right  = modelRegionBounds.x + modelRegionBounds.width ;
        int bottom = modelRegionBounds.y + modelRegionBounds.height ;

        if( ( edges & EDGE_LEFT ) != 0 ) {
            left = Math.max( 0, Math.min( mx, right - MIN_MODEL_SIZE ) ) ;
        }
        else if( ( edges & EDGE_RIGHT ) != 0 ) {
            right = Math.min( modelImgSize.width, Math.max( mx, left + MIN_MODEL_SIZE ) ) ;
        }

        if( ( edges & EDGE_TOP ) != 0 ) {
            top = Math.max( 0, Math.min( my, bottom - MIN_MODEL_SIZE ) ) ;
        }
        else if( ( edges & EDGE_BOTTOM ) != 0 ) {
            bottom = Math.min( modelImgSize.height, Math.max( my, top + MIN_MODEL_SIZE ) ) ;
        }

        setModelRegionBounds( new Rectangle( left, top, right - left, bottom - top ), scaleFactor ) ;
        return repaintBounds.union( oldRepaintBounds ) ;
    }

    // Replaces the region in model coordinates and refreshes the display
    // bounds for the given scale factor. The anchor is reset to the top left
    // corner, since the corner the region was drawn from is no longer relevant.
    void setModelRegionBounds( Rectangle modelBounds, double scaleFactor ) {
        modelRegionBounds = new Rectangle( modelBounds ) ;
        modelAnchorPoint = new Point( modelBounds.x, modelBounds.y ) ;
        scaleTo( scaleFactor ) ;
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
