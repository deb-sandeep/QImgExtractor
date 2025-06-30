package com.sandy.sconsole.qimgextractor.ui.core.tabbedpane;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;

/**
 * This is an extension of {@link JTabbedPane} that includes a closing button in
 * the tabs. The close button behavior is similar to how tabs work in the Eclipse
 * editor.
 */
@Slf4j
public class CloseableTabbedPane extends HighlightableTabbedPane {

    /** Pixels added to each tab for accommodating close image */
    private static final int TAB_WIDTH_EXTENSION = 50 ;

    public static final int TAB_CLOSING = 1 ;
    
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
        addCtrlWListenerForTabClose() ;
        super.setForeground( Color.WHITE ) ;
    }
    
    private void addCtrlWListenerForTabClose() {
        
        this.addKeyListener( new KeyAdapter() {
            public void keyTyped( KeyEvent e ) {
                if( e.getKeyChar() == 'w' && 
                    ( e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK ) == KeyEvent.CTRL_DOWN_MASK ) {

                    int selIndex = CloseableTabbedPane.this.getSelectedIndex() ;
                    if( selIndex == -1 ) return ;
                    
                    boolean okToCloseTab = true ;
                    Component comp = getComponentAt( selIndex ) ;
                    if( comp instanceof CloseableTab tab ) {
                        if( !tab.isOkToCloseTab() ) {
                            okToCloseTab = false ;
                        }
                    }
                    
                    if( okToCloseTab ) {
                        CloseableTabbedPane.this.notifyListeners( selIndex, TAB_CLOSING ) ;
                        CloseableTabbedPane.this.remove( selIndex ) ;
                    }
                }
            }
        } ) ;
    }
    
    public void addTabCloseListener( TabCloseListener l ) {
        if( !this.listeners.contains( l ) ) {
            this.listeners.add( l ) ;
        }
    }
    
    public void removeTabCloseListener( TabCloseListener l ) {
        this.listeners.remove( l ) ;
    }
    
    void notifyListeners( int tabIndex, int eventId ) {
        Component comp = getComponentAt( tabIndex ) ;
        ActionEvent evt = new ActionEvent( comp, eventId, null ) ;
        for( TabCloseListener l : listeners ) {
            l.tabClosing( evt ) ;
        }
    }
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        closeUI.paint( g ) ;
    }
}
