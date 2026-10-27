package com.sandy.sconsole.qimgextractor.ui.core.tabbedpane;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an extension of {@link JTabbedPane} that includes a closing button in
 * the tabs. The close button behavior is similar to how tabs work in the Eclipse
 * editor.
 */
@Slf4j
public class CloseableTabbedPane extends HighlightableTabbedPane {

    // Pixels added to each tab for accommodating close image
    private static final int TAB_WIDTH_EXTENSION = 50 ;

    // Pixels added around the rotated title of LEFT/RIGHT placed tabs
    private static final int VERTICAL_TAB_PADDING = 8 ;

    private final TabCloseImageUI closeUI ;
    
    private final List<TabCloseListener> listeners = new ArrayList<>() ;
    
    public CloseableTabbedPane() {
        this.closeUI = new TabCloseImageUI( this ) ;
        super.setUI( new BasicTabbedPaneUI() {

            // For LEFT/RIGHT placement the title is drawn rotated, so the
            // tab width is driven by the font height and the tab height by
            // the title length (plus room for the close image at the bottom).
            protected int calculateTabWidth( int tabPlacement, int tabIndex,
                                             FontMetrics metrics ) {
                if( isVertical( tabPlacement ) ) {
                    return metrics.getHeight() + tabInsets.top + tabInsets.bottom + VERTICAL_TAB_PADDING ;
                }
                return super.calculateTabWidth( tabPlacement, tabIndex, metrics ) + TAB_WIDTH_EXTENSION ;
            }

            protected int calculateTabHeight( int tabPlacement, int tabIndex,
                                              int fontHeight ) {
                if( isVertical( tabPlacement ) ) {
                    String title = tabPane.getTitleAt( tabIndex ) ;
                    int textWidth = getFontMetrics().stringWidth( title ) ;
                    return textWidth + tabInsets.left + tabInsets.right + TAB_WIDTH_EXTENSION ;
                }
                return super.calculateTabHeight( tabPlacement, tabIndex, fontHeight ) ;
            }

            // Draws the title reading top to bottom, starting near the top
            // of the tab so that the close image fits below it.
            protected void paintText( Graphics g, int tabPlacement, Font font,
                                      FontMetrics metrics, int tabIndex,
                                      String title, Rectangle textRect,
                                      boolean isSelected ) {
                if( !isVertical( tabPlacement ) ) {
                    super.paintText( g, tabPlacement, font, metrics, tabIndex,
                                     title, textRect, isSelected ) ;
                    return ;
                }

                Rectangle tabRect = rects[tabIndex] ;
                int textHeight = metrics.getAscent() + metrics.getDescent() ;
                int x = tabRect.x + ( tabRect.width - textHeight ) / 2 + metrics.getDescent() ;
                int y = tabRect.y + tabInsets.left + VERTICAL_TAB_PADDING / 2 ;

                Graphics2D g2 = ( Graphics2D )g.create() ;
                g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING,
                                     RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
                g2.setFont( font ) ;
                g2.setColor( tabPane.isEnabledAt( tabIndex ) ?
                             tabPane.getForegroundAt( tabIndex ) :
                             Color.GRAY ) ;
                g2.translate( x, y ) ;
                g2.rotate( Math.PI / 2 ) ;
                g2.drawString( title, 0, 0 ) ;
                g2.dispose() ;
            }
        }) ;
        super.setForeground( Color.DARK_GRAY ) ;
    }

    static boolean isVertical( int tabPlacement ) {
        return tabPlacement == LEFT || tabPlacement == RIGHT ;
    }
    
    public void addTabCloseListener( TabCloseListener l ) {
        if( !this.listeners.contains( l ) ) {
            this.listeners.add( l ) ;
        }
    }
    
    @Override
    public void remove( int index ) {
        notifyTabCloseListeners( index ) ;
        super.remove( index ) ;
    }
    
    public void removeTabCloseListener( TabCloseListener l ) {
        this.listeners.remove( l ) ;
    }
    
    void notifyTabCloseListeners( int tabIndex ) {
        Component comp = getComponentAt( tabIndex ) ;
        for( TabCloseListener l : listeners ) {
            l.tabClosing( tabIndex, comp ) ;
        }
    }
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        closeUI.paint( g ) ;
    }
}
