package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.internal;

import lombok.Getter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

// A white brush to paint out (clear) unwanted areas of the page image. Only
// usable in COMMAND mode. Each stroke is painted onto the page image as the
// mouse is dragged and committed (page image saved, overlapping question
// images re-cut) when the mouse is released.
class WhiteBrush extends MouseAdapter {

    private static final int MIN_SIZE     = 4 ;
    private static final int MAX_SIZE     = 200 ;
    private static final int SIZE_STEP    = 4 ;
    private static final int DEFAULT_SIZE = 20 ;

    private final ImgCanvas canvas ;

    @Getter private boolean active = false ;

    // Brush diameter in view (screen) pixels, so that the brush covers the
    // same visual area irrespective of the zoom level.
    @Getter private int size = DEFAULT_SIZE ;

    private transient Point cursorPos = null ;
    private transient Point lastStrokePt = null ;

    // Union of the areas painted in the current stroke, in original image
    // coordinates. Null if no stroke is in progress.
    private transient Rectangle strokeModelBounds = null ;

    WhiteBrush( ImgCanvas canvas ) {
        this.canvas = canvas ;
    }

    void setActive( boolean active ) {
        if( this.active == active ) return ;
        if( !active ) {
            endStroke() ;
            clearCursor() ;
        }
        this.active = active ;
        canvas.setCursor( Cursor.getPredefinedCursor( active ? Cursor.CROSSHAIR_CURSOR
                                                             : Cursor.DEFAULT_CURSOR ) ) ;
        canvas.brushStatusChanged() ;
    }

    void changeSize( int steps ) {
        int newSize = Math.max( MIN_SIZE, Math.min( MAX_SIZE, size + steps*SIZE_STEP ) ) ;
        if( newSize != size ) {
            Rectangle oldCursorBounds = getCursorBounds() ;
            size = newSize ;
            repaintCursor( oldCursorBounds ) ;
            canvas.brushStatusChanged() ;
        }
    }

    @Override
    public void mousePressed( MouseEvent e ) {
        if( !active || !SwingUtilities.isLeftMouseButton( e ) ) return ;
        lastStrokePt = e.getPoint() ;
        paintSegment( lastStrokePt, lastStrokePt ) ;
    }

    @Override
    public void mouseDragged( MouseEvent e ) {
        if( !active ) return ;
        moveCursor( e.getPoint() ) ;
        if( lastStrokePt != null ) {
            Point p = e.getPoint() ;
            paintSegment( lastStrokePt, p ) ;
            lastStrokePt = p ;
        }
    }

    @Override
    public void mouseReleased( MouseEvent e ) {
        if( active && SwingUtilities.isLeftMouseButton( e ) ) {
            endStroke() ;
        }
    }

    @Override
    public void mouseMoved( MouseEvent e ) {
        if( active ) {
            moveCursor( e.getPoint() ) ;
        }
    }

    @Override
    public void mouseExited( MouseEvent e ) {
        clearCursor() ;
    }

    private void paintSegment( Point from, Point to ) {
        Rectangle modelRect = canvas.paintWhite( from, to, size ) ;
        if( modelRect != null ) {
            strokeModelBounds = ( strokeModelBounds == null ) ?
                                modelRect : strokeModelBounds.union( modelRect ) ;
        }
    }

    private void endStroke() {
        lastStrokePt = null ;
        if( strokeModelBounds != null ) {
            Rectangle bounds = strokeModelBounds ;
            strokeModelBounds = null ;
            canvas.brushStrokeEnded( bounds ) ;
        }
    }

    private void moveCursor( Point p ) {
        Rectangle oldBounds = getCursorBounds() ;
        cursorPos = p ;
        repaintCursor( oldBounds ) ;
    }

    private void clearCursor() {
        Rectangle oldBounds = getCursorBounds() ;
        cursorPos = null ;
        repaintCursor( oldBounds ) ;
    }

    private void repaintCursor( Rectangle oldBounds ) {
        Rectangle newBounds = getCursorBounds() ;
        if( oldBounds != null ) canvas.repaint( oldBounds ) ;
        if( newBounds != null ) canvas.repaint( newBounds ) ;
    }

    private Rectangle getCursorBounds() {
        if( cursorPos == null ) return null ;
        int r = size/2 + 2 ;
        return new Rectangle( cursorPos.x - r, cursorPos.y - r, 2*r + 1, 2*r + 1 ) ;
    }

    // Paints the brush outline at the mouse position.
    void paintCursor( Graphics2D g ) {
        if( !active || cursorPos == null ) return ;
        int r = size/2 ;
        g.setStroke( new BasicStroke( 1.0f ) ) ;
        g.setColor( Color.DARK_GRAY ) ;
        g.drawOval( cursorPos.x - r, cursorPos.y - r, size, size ) ;
        g.setColor( Color.WHITE ) ;
        g.drawOval( cursorPos.x - r + 1, cursorPos.y - r + 1, size - 2, size - 2 ) ;
    }
}
