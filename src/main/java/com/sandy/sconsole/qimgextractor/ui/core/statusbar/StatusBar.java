package com.sandy.sconsole.qimgextractor.ui.core.statusbar;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class StatusBar extends JPanel {

    public enum Direction { WEST, EAST }
    
    private final JPanel westPanel = new JPanel() ;
    private final JPanel eastPanel = new JPanel() ;

    private final List<BaseStatusBarComponent> westPanelComponents = new ArrayList<>() ;
    private final List<BaseStatusBarComponent> eastPanelComponents = new ArrayList<>() ;
    
    public void addStatusBarComponent( BaseStatusBarComponent comp, Direction dir ) {
        if( dir == Direction.WEST ) {
            westPanelComponents.add( comp ) ;
        }
        else {
            eastPanelComponents.add( comp ) ;
        }
    }
    
    public void initialize() {

        setUpPanel( Direction.WEST ) ;
        setUpPanel( Direction.EAST ) ;
        
        setLayout( new BorderLayout() ) ;
        add( this.eastPanel, BorderLayout.CENTER ) ;
        add( this.westPanel, BorderLayout.WEST ) ;
    }
    
    private void setUpPanel( Direction dir ) {
        
        JPanel panel ;
        int layoutDir  = FlowLayout.LEFT ;
        List<BaseStatusBarComponent> components ;
        
        if( dir == Direction.WEST ) {
            panel      = this.westPanel ;
            components = this.westPanelComponents ;
        }
        else {
            panel      = this.eastPanel ;
            layoutDir  = FlowLayout.RIGHT ;
            components = this.eastPanelComponents ;
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
