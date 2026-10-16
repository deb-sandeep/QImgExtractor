package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgCanvasListener;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.SelectedRegionMetadata;
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
    
    private final ImgCanvas canvas ;
    
    // Region select mode is when the canvas is in the process of drawing a bounding
    // rectangle. If the mouse is pressed when the canvas is not in marking
    // mode, the canvas goes into marking mode -> any mouse movement hereafter
    // will rubber band the marking boundary. If the canvas is in the marking
    // mode and the mouse is clicked, the canvas ends the mark and notifies
    // that a sub-image has been marked.
    private boolean inRegionSelectMode = false ;
    
    private boolean inVMarkSelectionMode = false ;

    // Only true when guide mode was entered via enterFreshGuideEditMode()
    // (Enter key). Guides placed via the manual B toggle never auto-exit,
    // since that path is used for open-ended adjustment, not a fixed count.
    private boolean autoExitAfterTwoGuides = false ;
    
    private transient SelectedRegion activeRegion = null ;
    private transient int curVMarkPos = -1 ;
    private transient int curHMarkPos = -1 ;
    
    // An existing region whose edges are being dragged in COMMAND mode. The
    // resize is committed (image re-cut and metadata updated) on mouse
    // release, and reverted if the commit fails or is cancelled.
    private transient SelectedRegion resizingRegion = null ;
    private transient int resizeEdges = 0 ;
    private transient Rectangle resizeOrigModelBounds = null ;
    
    private final List<SelectedRegion> oldRegions = new ArrayList<>() ;
    private final ImgCanvasListener listener ;
    private final VerticalGuides verticalGuides ;
    
    RegionSelector( ImgCanvas canvas, ImgCanvasListener listener ) {
        this.canvas = canvas ;
        this.listener = listener ;
        this.verticalGuides = new VerticalGuides( canvas ) ;
    }
    
    public void mousePressed( MouseEvent event ) {
        if( canvas.isInEditMode() ) {
            if( !inRegionSelectMode ) {
                inRegionSelectMode = true ;
                activeRegion = new SelectedRegion( verticalGuides.getStartSnapPoint( event ) ) ;
                listener.selectionStarted() ;
            }
            else {
                handleRegionSelectedEnded( event ) ;
            }
        }
        else if( canvas.isInCommandMode() ) {
            if( inVMarkSelectionMode ) {
                if( curVMarkPos > 0 ) {
                    verticalGuides.addGuide( event.getX() ) ;
                    if( autoExitAfterTwoGuides && verticalGuides.getGuideCount() >= 2 ) {
                        canvas.setOpMode( ImgCanvas.OpMode.EDITOR ) ;
                    }
                }
            }
            else if( event.getButton() == MouseEvent.BUTTON1 ) {
                beginResize( event.getPoint() ) ;
            }
        }
        canvas.requestFocus() ;
    }
    
    @Override
    public void mouseDragged( MouseEvent event ) {
        if( resizingRegion != null ) {
            Rectangle paintArea = resizingRegion.resize( resizeEdges, event.getPoint(),
                                                         canvas.getScaleFactor(),
                                                         canvas.getOriginalImageSize() ) ;
            canvas.repaint( paintArea ) ;
            canvas.logActiveRegionSize( resizingRegion.getRegionBounds() ) ;
        }
        canvas.logMousePosition( event.getPoint() ) ;
    }
    
    @Override
    public void mouseReleased( MouseEvent event ) {
        if( resizingRegion != null && event.getButton() == MouseEvent.BUTTON1 ) {
            commitResize() ;
        }
    }
    
    private void beginResize( Point p ) {
        SelectedRegion region = null ;
        int edges = 0 ;
        // Regions added later are painted on top, so they get preference.
        for( int i=oldRegions.size()-1; i>=0 && edges == 0; i-- ) {
            region = oldRegions.get( i ) ;
            edges = region.getEdgesAt( p ) ;
        }
        if( edges != 0 ) {
            resizingRegion = region ;
            resizeEdges = edges ;
            resizeOrigModelBounds = region.getModelRegionBounds() ;
            canvas.repaint( region.getRepaintBounds() ) ;
            log.debug( "## Resizing region {} from {}", region.getTag(), resizeOrigModelBounds ) ;
        }
    }
    
    private void commitResize() {
        SelectedRegion region = resizingRegion ;
        Rectangle newModelBounds = region.getModelRegionBounds() ;
        resizingRegion = null ;
        
        if( !newModelBounds.equals( resizeOrigModelBounds ) ) {
            log.debug( "  Region {} resized to {}", region.getTag(), newModelBounds ) ;
            if( !canvas.regionResized( region.getTag(), newModelBounds ) ) {
                log.debug( "  Resize not accepted. Reverting region." ) ;
                Rectangle oldRepaintBounds = region.getRepaintBounds() ;
                region.setModelRegionBounds( resizeOrigModelBounds, canvas.getScaleFactor() ) ;
                canvas.repaint( oldRepaintBounds ) ;
            }
        }
        canvas.repaint( region.getRepaintBounds() ) ;
        canvas.logActiveRegionSize( null ) ;
        resizeOrigModelBounds = null ;
    }
    
    private void cancelResize() {
        if( resizingRegion != null ) {
            Rectangle oldRepaintBounds = resizingRegion.getRepaintBounds() ;
            resizingRegion.setModelRegionBounds( resizeOrigModelBounds, canvas.getScaleFactor() ) ;
            canvas.repaint( oldRepaintBounds.union( resizingRegion.getRepaintBounds() ) ) ;
            resizingRegion = null ;
            resizeOrigModelBounds = null ;
            canvas.logActiveRegionSize( null ) ;
        }
    }
    
    private void updateResizeCursor( Point p ) {
        int edges = 0 ;
        if( canvas.isInCommandMode() && !inVMarkSelectionMode ) {
            for( int i=oldRegions.size()-1; i>=0 && edges == 0; i-- ) {
                edges = oldRegions.get( i ).getEdgesAt( p ) ;
            }
        }
        canvas.setCursor( Cursor.getPredefinedCursor( getResizeCursorType( edges ) ) ) ;
    }
    
    private static int getResizeCursorType( int edges ) {
        boolean l = ( edges & SelectedRegion.EDGE_LEFT   ) != 0 ;
        boolean r = ( edges & SelectedRegion.EDGE_RIGHT  ) != 0 ;
        boolean t = ( edges & SelectedRegion.EDGE_TOP    ) != 0 ;
        boolean b = ( edges & SelectedRegion.EDGE_BOTTOM ) != 0 ;
        
        if( l && t ) return Cursor.NW_RESIZE_CURSOR ;
        if( r && t ) return Cursor.NE_RESIZE_CURSOR ;
        if( l && b ) return Cursor.SW_RESIZE_CURSOR ;
        if( r && b ) return Cursor.SE_RESIZE_CURSOR ;
        if( l )      return Cursor.W_RESIZE_CURSOR ;
        if( r )      return Cursor.E_RESIZE_CURSOR ;
        if( t )      return Cursor.N_RESIZE_CURSOR ;
        if( b )      return Cursor.S_RESIZE_CURSOR ;
        return Cursor.DEFAULT_CURSOR ;
    }
    
    @Override
    public void mouseMoved( MouseEvent event ) {
        
        redrawHMarkerOnMouseMove( event.getY() ) ;
        updateResizeCursor( event.getPoint() ) ;
        
        if( inRegionSelectMode ) {
            Rectangle paintArea = activeRegion.updateRegion( verticalGuides.getEndSnapPoint( event ) ) ;
            this.canvas.repaint( paintArea ) ;
            canvas.logActiveRegionSize( activeRegion.getRegionBounds() ) ;
        }
        else if( inVMarkSelectionMode ) {
            if( curVMarkPos != event.getX() ) {
                int oldX = curVMarkPos ;
                if( oldX >= 0 ) {
                    repaintVMarkerRegion( oldX ) ;
                }
                curVMarkPos = event.getX() ;
                repaintVMarkerRegion( curVMarkPos ) ;
            }
        }
        
        canvas.logMousePosition( event.getPoint() ) ;
    }
    
    private void redrawHMarkerOnMouseMove( int mouseY ) {
        if( canvas.isInEditMode() ) {
            if( curHMarkPos != mouseY ) {
                int oldY = curHMarkPos ;
                if( oldY >= 0 ) {
                    repaintHMarkerRegion( oldY ) ;
                }
                curHMarkPos = mouseY ;
                repaintHMarkerRegion( curHMarkPos ) ;
            }
        }
        else {
            if( curHMarkPos >= 0 ) {
                int oldY = curHMarkPos ;
                this.curHMarkPos = -1 ;
                repaintHMarkerRegion( oldY ) ;
            }
        }
    }
    
    public void setSelectedRegions( List<SelectedRegionMetadata> infoList ) {
        oldRegions.clear() ;
        if( infoList != null ) {
            infoList.forEach( info -> oldRegions.add( new SelectedRegion( info ) ) ) ;
        }
    }
    
    public void deleteSelectedRegion( String tag ) {
        for( Iterator<SelectedRegion> iter = oldRegions.iterator(); iter.hasNext(); ) {
            SelectedRegion region = iter.next() ;
            if( region.getTag().equals( tag ) ) {
                if( region == resizingRegion ) {
                    resizingRegion = null ;
                    resizeOrigModelBounds = null ;
                }
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

    private void handleRegionSelectedEnded( MouseEvent event ) {
        try {
            if( activeRegion != null ) {
                log.debug( "## Region selected: {}", activeRegion.getRegionBounds() ) ;
                
                int selectionEndAction = event.getButton() ;
                log.debug( "  Selection flag (mouse button): {}", selectionEndAction ) ;
                
                String tag = canvas.subImageSelected( activeRegion.getRegionBounds(), selectionEndAction ) ;
                log.debug( "  Question image tag: {}", tag ) ;
                
                if( tag != null ) {
                    log.debug( "  Adding active region to the list of selected regions." ) ;
                    activeRegion.setTag( tag ) ;
                    activeRegion.setModelCoordinates( canvas.getScaleFactor() ) ;
                    oldRegions.add( activeRegion ) ;
                    
                    log.debug( "  Informing the canvas that a new region has been selected." ) ;
                    canvas.selectedRegionAdded( activeRegion.getRegionInfo() ) ;
                    canvas.logActiveRegionSize( null ) ;
                }
                log.debug( "  Clearing the active region." ) ;
                clearActiveSelection() ;
            }
        }
        catch( Exception e1 ) {
            log.error( "Raster exception.", e1 ) ;
        }
        log.debug( "== Region selected ended." ) ;
    }
    
    public void paintSelectedRegions( Graphics2D g ) {
        oldRegions.forEach( region -> region.paintAsOldRegion( g ) ) ;
        verticalGuides.getGuidePositions().forEach( pos -> paintVMarker( pos, g, Color.GREEN ) ) ;
        
        if( activeRegion != null ) {
            activeRegion.paint( g ) ;
        }
        
        if( resizingRegion != null ) {
            resizingRegion.paint( g ) ;
        }
        
        if( inVMarkSelectionMode && curVMarkPos >= 0 ) {
            paintVMarker( curVMarkPos, g, Color.LIGHT_GRAY ) ;
        }
        
        if( curHMarkPos >= 0 ) {
            paintHMarker( curHMarkPos, g ) ;
        }
    }
    
    private void repaintVMarkerRegion( int vMarkPos ) {
        if( vMarkPos >= 0 ) {
            canvas.repaint( vMarkPos - 1, 0, 3, canvas.getHeight() ) ;
        }
    }
    
    private void paintVMarker( Integer xPos, Graphics2D g, Color color ) {
        float[] dash = { 3.0f };
        g.setColor( color ) ;
        g.setStroke( new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f ) );
        g.drawLine( xPos, 0, xPos, canvas.getHeight() ) ;
    }
    
    private void repaintHMarkerRegion( int hMarkPos ) {
        if( hMarkPos >= 0 ) {
            canvas.repaint( 0, hMarkPos - 1, canvas.getWidth(), 3 ) ;
        }
    }
    
    private void paintHMarker( Integer yPos, Graphics2D g ) {
        float[] dash = { 3.0f };
        g.setColor( Color.LIGHT_GRAY ) ;
        g.setStroke( new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f ) );
        g.drawLine( 0, yPos, canvas.getWidth(), yPos ) ;
    }
    
    // scaleFactor is absolute: display size relative to the original image
    public void scaleSelectedRegions( double scaleFactor ) {
        // Cleared first, so that a resize in progress is reverted at the
        // scale it was started in before all regions are rescaled.
        clearActiveSelection() ;
        oldRegions.forEach( region -> region.scaleTo( scaleFactor ) ) ;
    }
    
    public void clearActiveSelection() {
        cancelResize() ;
        inRegionSelectMode = false ;
        if( activeRegion != null ) {
            canvas.repaint( activeRegion.getRepaintBounds() ) ;
            activeRegion = null ;
            canvas.logActiveRegionSize( null ) ;
            listener.selectionEnded() ;
        }
    }
    
    public boolean containsTag( String tagName ) {
        for( SelectedRegion region : oldRegions ) {
            if( region.getTag().equals( tagName ) ) {
                return true ;
            }
        }
        return false ;
    }

    // Returns the bounds of the region in the (scaled) canvas coordinates,
    // or null if no region with the given tag exists.
    public Rectangle getRegionBounds( String tagName ) {
        for( SelectedRegion region : oldRegions ) {
            if( region.getTag().equals( tagName ) ) {
                return new Rectangle( region.getRegionBounds() ) ;
            }
        }
        return null ;
    }
    
    public void toggleVMarkSelectionMode() {
        setVMarkSelectionMode( !inVMarkSelectionMode ) ;
    }

    // Clears existing guides and immediately enters guide placement mode,
    // collapsing what used to be a clear + explicit toggle-on into one step.
    public void enterFreshGuideEditMode() {
        clearVerticalMarkers() ;
        setVMarkSelectionMode( true ) ;
        autoExitAfterTwoGuides = true ;
    }

    // Called when the canvas leaves COMMAND mode for EDITOR mode, so the
    // user doesn't have to explicitly toggle guide mode off before escaping.
    public void exitGuideEditMode() {
        setVMarkSelectionMode( false ) ;
    }

    private void setVMarkSelectionMode( boolean on ) {
        if( on == inVMarkSelectionMode ) {
            return ;
        }
        if( !on ) {
            autoExitAfterTwoGuides = false ;
            if( curVMarkPos >= 0 ) {
                // Remove the vMark position. This will erase the vertical
                // marker on the next paint.
                int oldX = curVMarkPos ;
                curVMarkPos = -1 ;
                repaintVMarkerRegion( oldX ) ;
            }
        }
        inVMarkSelectionMode = on ;
        canvas.setGuideModeStatus( on ) ;
    }

    public void clearVerticalMarkers() {
        verticalGuides.clear() ;
        canvas.repaint() ;
    }
}
