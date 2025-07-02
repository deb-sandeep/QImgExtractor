package com.sandy.sconsole.qimgextractor.ui.core.statusbar;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import java.awt.*;

@Slf4j
public class MessageStatusComponent extends BaseStatusBarComponent {

    private static final Font FONT = new Font( Font.SANS_SERIF, Font.PLAIN, 10 ) ;
    private JLabel msgLabel = null ;

    public MessageStatusComponent() {
        setUpUI() ;
    }
    
    private void setUpUI() {
        msgLabel = new JLabel() ;
        setFont( FONT ) ;
        setForeground( Color.BLUE ) ;
        setBorder( BorderFactory.createBevelBorder( BevelBorder.LOWERED ) ) ;
        clear() ;
        
        super.add( this.msgLabel, BorderLayout.CENTER ) ;
    }
    
    public void setFont( Font font ) {
        if( msgLabel != null ) {
            msgLabel.setFont( font ) ;
        }
    }
    
    public void setForeground( Color color ) {
        if( msgLabel != null ) {
            msgLabel.setForeground( color ) ;
        }
    }
    
    public void setBorder( Border border ) {
        super.setBorder( border ) ;
    }
    
    public void log( String text ) {
        msgLabel.setText( text ) ;
    }
    
    public void clear() {
        msgLabel.setText( " " ) ;
    }
}
