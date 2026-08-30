package com.sandy.sconsole.qimgextractor.ui.core.statusbar;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StatusBar extends JPanel {

    public enum Direction { WEST, CENTER, EAST }

    private final JPanel westPanel = new JPanel() ;
    private final JPanel centerPanel = new JPanel() ;
    private final JPanel eastPanel = new JPanel() ;

    private final List<BaseStatusBarComponent> westPanelComponents = new ArrayList<>() ;
    private final List<BaseStatusBarComponent> centerPanelComponents = new ArrayList<>() ;
    private final List<BaseStatusBarComponent> eastPanelComponents = new ArrayList<>() ;

    public void addStatusBarComponent( BaseStatusBarComponent comp, Direction dir ) {
        switch( dir ) {
            case WEST   -> westPanelComponents.add( comp ) ;
            case CENTER -> centerPanelComponents.add( comp ) ;
            case EAST   -> eastPanelComponents.add( comp ) ;
        }
    }

    public void initialize() {

        setUpPanel( Direction.WEST ) ;
        setUpPanel( Direction.CENTER ) ;
        setUpPanel( Direction.EAST ) ;

        setLayout( new BorderLayout() ) ;
        add( this.westPanel, BorderLayout.WEST ) ;
        add( this.centerPanel, BorderLayout.CENTER ) ;
        add( this.eastPanel, BorderLayout.EAST ) ;
    }

    private void setUpPanel( Direction dir ) {

        JPanel panel ;
        int layoutDir ;
        List<BaseStatusBarComponent> components ;

        switch( dir ) {
            case WEST -> {
                panel      = this.westPanel ;
                layoutDir  = FlowLayout.LEFT ;
                components = this.westPanelComponents ;
            }
            case CENTER -> {
                panel      = this.centerPanel ;
                layoutDir  = FlowLayout.CENTER ;
                components = this.centerPanelComponents ;
            }
            default -> {
                panel      = this.eastPanel ;
                layoutDir  = FlowLayout.RIGHT ;
                components = this.eastPanelComponents ;
            }
        }

        panel.setLayout( new FlowLayout( layoutDir, 0, 0 ) );
        for( int i=0; i<components.size(); i++ ) {
            panel.add( components.get( i ) ) ;
            if( i < (components.size()-1) ) {
                panel.add( getSeparator() ) ;
            }
        }
    }

    private Component getSeparator() {
        final JPanel label = new JPanel() ;
        label.setPreferredSize( new Dimension( 2, 25 ) ) ;
        return label ;
    }
}
