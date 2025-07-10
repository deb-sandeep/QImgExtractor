package com.sandy.sconsole.qimgextractor.ui.core.tabbedpane;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.BufferedImage;

/**
 * This is an inner private class that does two primary functions:
 * <p>
 * 1. Paints the close image (gray or red) as necessary<br>
 * 2. Handle various mouse events for repainting and closing tabs
 */
@Slf4j
class TabCloseImageUI implements MouseListener, MouseMotionListener {
    
    /** Close button size */
    private static final int BUTTON_SIZE = 16 ;
    /** Spacing between right margin and close button */
    private static final int SPACING = 5 ;
    
    /** Handle to the parent Tabbed Pane */
    private final CloseableTabbedPane parent ;
    
    /** Buffered images for close buttons */
    private BufferedImage greyClose ;
    private BufferedImage redClose ;
    
    /** Mouse pointer coordinates */
    private int mX = 0 ;
    private int mY = 0 ;
    
    /** Mouse over tab and close buttons statuses */
    private int mouseOverTabIndex = -1 ;
    private boolean mouseOverClose = false ;
    
    /**
     * Constructor with a handle to it's parent TabbedPane. This adds the
     * mouse handlers to its parent and buffers up the images.
     *
     * @param parent The instance of {@link CloseableTabbedPane}
     */
    public TabCloseImageUI( CloseableTabbedPane parent ) {
        this.parent = parent ;
        this.parent.addMouseMotionListener( this );
        this.parent.addMouseListener( this );
        try {
            greyClose = SwingUtils.getIconImage( "close_grey" ) ;
            redClose  = SwingUtils.getIconImage( "close_red" ) ;
            
        } catch ( Exception e ) {
            log.error( "Error while loading close images for tab", e ) ;
        }
    }
    
    /**
     * The paint method that is called from the parent to paint the close
     * buttons.
     *
     * @param g the instance of Graphic Context
     */
    public void paint( Graphics g ) {
        //Draw close image on all tabs
        for ( int i = 0 ; i < parent.getTabCount() ; i++ ) {
            boolean red = ( i == mouseOverTabIndex  && mouseOverClose ) ;
            drawCloseImage( g, i, red ) ;
        }
    }
    
    /** Unused mouse events */
    public void mouseClicked( MouseEvent e ) {}
    public void mouseEntered( MouseEvent e ) {}
    public void mouseExited( MouseEvent e )  {}
    public void mousePressed( MouseEvent e ) {}
    public void mouseDragged( MouseEvent e ) {}
    
    /**
     * This mouse released method is used for capturing the event of a mouse
     * release over a close button. If the mouse release is over a close
     * button, then the tab is closed.
     * <p>
     * This method removes the utility tab if and only if the utility
     * assures that it is ok for it to be removed.
     * <p>
     * See {@link MouseListener#mouseReleased(MouseEvent)}
     */
    public void mouseReleased( MouseEvent e ) {
        
        mX = e.getX() ;
        mY = e.getY() ;
        
        //check if mouse is released over close image
        if ( getMouseOverTabIndex() != -1  && isMouseOnClose() ) {
            int tabIndex = getMouseOverTabIndex() ;
            
            parent.notifyTabCloseListeners( tabIndex ) ;
            parent.remove( tabIndex ) ;
        }
    }
    
    /**
     * This method is used for tracking the mouse movement, and if the state
     * is changed, then repaint the tabs.
     */
    public void mouseMoved( MouseEvent e ) {
        mX = e.getX() ;
        mY = e.getY() ;
        //Re-paint if the status has changed
        if ( ( mouseOverTabIndex != getMouseOverTabIndex() )
                || ( mouseOverClose != isMouseOnClose() )) {
            mouseOverTabIndex = getMouseOverTabIndex() ;
            mouseOverClose = isMouseOnClose() ;
            paint( parent.getGraphics() ) ;
            setCursor() ;
        }
    }
    
    /**
     * This is a private method to change the cursor to a hand in case of
     * mouse over close button.
     */
    private void setCursor() {
        if ( mouseOverClose ) {
            parent.setCursor( new Cursor( Cursor.HAND_CURSOR ) ) ;
        } else {
            parent.setCursor( new Cursor( Cursor.DEFAULT_CURSOR ) ) ;
        }
    }
    
    /**
     * This utility method returns the index of tab on which the mouse is
     * hovering.
     *
     * @return Index of tab mouse of over or -1 if mouse is not over any
     *         tab.
     */
    private int getMouseOverTabIndex() {
        int index = -1 ;
        for ( int i = 0 ; i < parent.getTabCount() ; i++ ) {
            Rectangle rect = parent.getBoundsAt( i ) ;
            if ( rect.contains( mX, mY ) ) {
                index = i ;
                break ;
            }
        }
        return index ;
    }
    
    /**
     * This method determines if the mouse is over the close button.
     *
     * @return True if mouse of over a close button
     */
    private boolean isMouseOnClose() {
        boolean ret = false ;
        int index = getMouseOverTabIndex() ;
        if ( index != -1 ) {
            Rectangle rect = parent.getBoundsAt( index ) ;
            int dx = rect.x + rect.width - BUTTON_SIZE - SPACING ;
            int dy = ( rect.y + ( rect.height / 2 ) )  - 6 ;
            Rectangle imgRect = new Rectangle( dx, dy,
                    BUTTON_SIZE, BUTTON_SIZE ) ;
            ret =  imgRect.contains( mX, mY ) ;
        }
        return ret ;
    }
    
    
    /**
     * This method draws the image at the given tab index.
     *
     * @param g - Graphic context
     * @param index - Index of tab on which image is to be drawn
     * @param red - If a red image is to be drawn or gray
     */
    private void drawCloseImage( Graphics g, int index, boolean red ) {
        if ( index != -1 && index < parent.getTabCount() ) {
            Rectangle rect = parent.getBoundsAt( index ) ;
            int dx = rect.x + rect.width - BUTTON_SIZE - SPACING ;
            int dy = ( rect.y + ( rect.height / 2 ) )  - 6 ;
            g.drawImage( red ? redClose : greyClose, dx, dy, null ) ;
        }
    }
}
