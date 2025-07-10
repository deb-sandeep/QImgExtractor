package com.sandy.sconsole.qimgextractor.ui.core.statusbar;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;

@Slf4j
public class CustomWidgetStatusComponent extends BaseStatusBarComponent {

    private final Component widget ;
    
    public CustomWidgetStatusComponent( Component widget ) {
        this.widget = widget;
        setUpUI() ;
    }
    
    private void setUpUI() {
        setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) ) ;
        super.add( this.widget, BorderLayout.CENTER ) ;
    }
    
    public void setBorder( Border border ) {
        super.setBorder( border ) ;
    }
}
