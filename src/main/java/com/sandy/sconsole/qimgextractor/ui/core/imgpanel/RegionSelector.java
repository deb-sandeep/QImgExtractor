package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

@Slf4j
class RegionSelector
        extends MouseAdapter implements MouseMotionListener {
    
    private final ImageCanvas canvas ;
    
    // Region select mode is when the canvas is in the process of drawing a bounding
    // rectangle. If the mouse is pressed when the canvas is not in marking
    // mode, the canvas goes into marking mode -> any mouse movement hereafter
    // will rubber band the marking boundary. If the canvas is in the marking
    // mode and the mouse is clicked, the canvas ends the mark and notifies
    // that a sub-image has been marked.
    private boolean inRegionSelectMode = false ;
    
    private Point anchorPoint = null ;
    
    @Getter private Rectangle selectedRect = null ;
    
    RegionSelector( ImageCanvas canvas ) {
        this.canvas = canvas ;
    }
    
    public void mousePressed( MouseEvent event ) {
        
        if( !inRegionSelectMode ) {
            inRegionSelectMode = true ;
            this.anchorPoint = event.getPoint() ;
            this.selectedRect = new Rectangle( anchorPoint.x, anchorPoint.y, 0, 0 ) ;
        }
        else {
            handleAreaSelectedEnded( event ) ;
        }
        this.canvas.requestFocus() ;
    }
    
    @Override
    public void mouseMoved( MouseEvent event ) {
        if( inRegionSelectMode ) {
            
            Rectangle oldRect = new Rectangle( selectedRect ) ;
            
            Point curPt = event.getPoint() ;
            if( curPt.x < anchorPoint.x ) {
                selectedRect.x = curPt.x ;
                selectedRect.width = anchorPoint.x - curPt.x ;
            }
            else {
                selectedRect.x = anchorPoint.x ;
                selectedRect.width = curPt.x - anchorPoint.x ;
            }
            
            if( curPt.y < anchorPoint.y ) {
                selectedRect.y = curPt.y ;
                selectedRect.height = anchorPoint.y - curPt.y ;
            }
            else {
                selectedRect.y = anchorPoint.y ;
                selectedRect.height = curPt.y - anchorPoint.y ;
            }
            
            Rectangle paintArea = selectedRect.union( oldRect ) ;
            paintArea.setRect( paintArea.x-1, paintArea.y-1, paintArea.width+2, paintArea.height+2 ) ;
            
            this.canvas.repaint( paintArea ) ;
        }
    }
    
    private void handleAreaSelectedEnded( MouseEvent event ) {
        
        try {
            if( selectedRect != null ) {
                canvas.subImageSelected( selectedRect, event.getButton() ) ;
                clear() ;
            }
        }
        catch( Exception e1 ) {
            log.error( "Raster exception.", e1 ) ;
        }
    }
    
    public void clear() {
        
        this.inRegionSelectMode = false ;
        if( this.selectedRect != null ) {
            Rectangle paintArea = new Rectangle( this.selectedRect ) ;
            paintArea.setRect( paintArea.x-1, paintArea.y-1, paintArea.width+2, paintArea.height+2 ) ;
            this.canvas.repaint( paintArea ) ;
            this.selectedRect = null ;
        }
    }
    
    public void paintSelectedRegion( Graphics2D g ) {
        if( selectedRect != null ) {
            g.setColor( Color.RED ) ;
            g.drawRect( selectedRect.x, selectedRect.y, selectedRect.width, selectedRect.height ) ;
        }
    }
}
