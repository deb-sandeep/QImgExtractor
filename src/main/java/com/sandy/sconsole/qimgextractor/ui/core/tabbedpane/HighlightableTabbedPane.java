package com.sandy.sconsole.qimgextractor.ui.core.tabbedpane;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class HighlightableTabbedPane extends JTabbedPane implements ChangeListener {

    // A map which contains a mapping of the child component versus the
    // number of active alerts at any point in time.
    private final Map<Component, Boolean> alertMap = new HashMap<>() ;

    public HighlightableTabbedPane() {
        super( JTabbedPane.TOP ) ;
        super.addChangeListener( this ) ;
        super.setFocusable( false ) ;
    }

    @Override
    public void addTab( String title, Component component ) {
        super.addTab( title, component ) ;
        alertMap.put( component, false ) ;
    }

    @Override
    public void remove( int index ) {
        Component comp = getComponentAt( index ) ;
        alertMap.remove( comp ) ;
        super.remove( index ) ;
    }

    public synchronized void raiseAlert( Component child ) {
        
        boolean curAlert = alertMap.get( child ) ;
        if( !curAlert ) {
            alertMap.put( child, true ) ;
            Component[] children = getComponents() ;
            for( int i=0; i<children.length; i++ ) {
                if( children[i] == child ) {
                    setForegroundAt( i, Color.RED ) ;
                }
            }
        }
    }
    
    public synchronized void lowerAlert( Component child ) {
        
        boolean curAlert = alertMap.get( child ) ;
        if( curAlert ) {
            alertMap.put( child, false ) ;
            Component[] children = getComponents() ;
            for( int i=0; i<children.length; i++ ) {
                if( children[i] == child ) {
                    setForegroundAt( i, Color.DARK_GRAY ) ;
                }
            }
        }
    }
    
    @Override
    public void stateChanged( ChangeEvent e ) {
        int selectedIndex = getSelectedIndex() ;
        Component[] children = getComponents() ;
        for( int i=0; i<children.length; i++ ) {
            if( i == selectedIndex ) {
                setBackgroundAt( i, Color.YELLOW ) ;
            }
            else {
                setBackgroundAt( i, null ) ;
            }
        }
    }
}
