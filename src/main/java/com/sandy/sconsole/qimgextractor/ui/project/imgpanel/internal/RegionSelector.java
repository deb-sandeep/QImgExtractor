package com.sandy.sconsole.qimgextractor.ui.project.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Slf4j
class RegionSelector extends MouseAdapter implements MouseMotionListener {
    
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
            canvas.selectionStarted() ;
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
            canvas.logActiveRegionSize( activeRegion.getRegionBounds() ) ;
        }
        canvas.logMousePosition( event.getPoint() ) ;
    }
    
    public void setSelectedRegions( List<SubImgInfo> infoList ) {
        oldRegions.clear() ;
        if( infoList != null ) {
            infoList.forEach( info -> oldRegions.add( new SelectedRegion( info ) ) ) ;
        }
    }
    
    public void deleteSelectedRegion( String tag ) {
        for( Iterator<SelectedRegion> iter = oldRegions.iterator(); iter.hasNext(); ) {
            SelectedRegion region = iter.next() ;
            if( region.getTag().equals( tag ) ) {
                iter.remove() ;
                canvas.repaint( region.getRepaintBounds() ) ;
                break ;
            }
        }
    }
    
    public void renameSelectedRegion( String oldTag, String newTag ) {
        for( SelectedRegion region : oldRegions ) {
            if( region.getTag().equals( oldTag ) ) {
                region.setTag( newTag ) ;
                canvas.repaint( region.getRepaintBounds() ) ;
                break ;
            }
        }
    }

    private void handleAreaSelectedEnded( MouseEvent event ) {
        try {
            if( activeRegion != null ) {
                int selectionFlag = event.getButton() ;
                String tag = canvas.subImageSelected( activeRegion.getRegionBounds(), selectionFlag ) ;
                if( tag != null ) {
                    activeRegion.setTag( tag ) ;
                    oldRegions.add( activeRegion ) ;
                    canvas.selectedRegionAdded( activeRegion.getRegionInfo() ) ;
                    canvas.logActiveRegionSize( null ) ;
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
    
    public void scaleSelectedRegions( double scaleFactor ) {
        oldRegions.forEach( region -> region.scale( scaleFactor ) ) ;
        clearActiveSelection() ;
    }
    
    public void clearActiveSelection() {
        inRegionSelectMode = false ;
        if( activeRegion != null ) {
            canvas.repaint( activeRegion.getRepaintBounds() ) ;
            activeRegion = null ;
            canvas.logActiveRegionSize( null ) ;
            canvas.selectionEnded() ;
        }
    }
}
