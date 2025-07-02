package com.sandy.sconsole.qimgextractor.ui.core.imgpanel.internal;

import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.List;

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
    
    private SelectedRegion activeRegion = null ;
    
    private final List<SelectedRegion> oldRegions = new ArrayList<>() ;
    
    RegionSelector( ImageCanvas canvas ) {
        this.canvas = canvas ;
    }
    
    public void mousePressed( MouseEvent event ) {
        
        if( !inRegionSelectMode ) {
            inRegionSelectMode = true ;
            activeRegion = new SelectedRegion( event.getPoint() ) ;
        }
        else {
            handleAreaSelectedEnded( event ) ;
        }
        canvas.requestFocus() ;
    }
    
    @Override
    public void mouseMoved( MouseEvent event ) {
        if( inRegionSelectMode ) {
            Rectangle paintArea = activeRegion.updateRegion( event.getPoint() ) ;
            this.canvas.repaint( paintArea ) ;
        }
    }
    
    private void handleAreaSelectedEnded( MouseEvent event ) {
        
        try {
            if( activeRegion != null ) {
                String tag = canvas.subImageSelected( activeRegion.getRegionBounds(), event.getButton() ) ;
                if( tag != null ) {
                    activeRegion.setTag( tag ) ;
                    oldRegions.add( activeRegion ) ;
                }
                clearActiveSelection() ;
            }
        }
        catch( Exception e1 ) {
            log.error( "Raster exception.", e1 ) ;
        }
    }
    
    public void paintSelectedRegions( Graphics2D g ) {
        
        oldRegions.forEach( region -> region.paintAsOldRegion( g ) ) ;
        
        if( activeRegion != null ) {
            activeRegion.paint( g ) ;
        }
    }
    
    public void scaleSelectedRegions( double scaleFactor) {
        oldRegions.forEach( region -> region.scale( scaleFactor ) ) ;
        clearActiveSelection() ;
    }
    
    public void clearActiveSelection() {
        
        inRegionSelectMode = false ;
        if( activeRegion != null ) {
            canvas.repaint( activeRegion.getRepaintBounds() ) ;
            activeRegion = null ;
        }
    }
}
