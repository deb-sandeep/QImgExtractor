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

    private final TabCloseImageUI closeUI ;
    
    private final List<TabCloseListener> listeners = new ArrayList<>() ;
    
    public CloseableTabbedPane() {
        this.closeUI = new TabCloseImageUI( this ) ;
        super.setUI( new BasicTabbedPaneUI() {
            protected int calculateTabWidth( int tabPlacement, int tabIndex, 
                                             FontMetrics metrics ) {
                return super.calculateTabWidth( tabPlacement, tabIndex, metrics ) + TAB_WIDTH_EXTENSION ;
            }
        }) ;
        super.setForeground( Color.DARK_GRAY ) ;
    }
    
    public void addTabCloseListener( TabCloseListener l ) {
        if( !this.listeners.contains( l ) ) {
            this.listeners.add( l ) ;
        }
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
