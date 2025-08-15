package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

class HelpManual extends JPanel {
    
    private JTextArea textArea = null ;

    public HelpManual( List<String> helpContent ) {
        setUpUI() ;
        addHelpContent( helpContent ) ;
    }
    
    private void setUpUI() {
        
        setPreferredSize( new Dimension( 300, 300 ) ) ;
        setLayout( new BorderLayout() ) ;
        
        textArea = new JTextArea( 20, 40 ) ;
        textArea.setFont( new Font( "Courier New", Font.PLAIN, 11 ) ) ;
        textArea.setForeground( Color.BLACK ) ;
        textArea.setEditable( false ) ;
        
        JScrollPane sp = new JScrollPane( textArea ) ;
        sp.setHorizontalScrollBarPolicy( HORIZONTAL_SCROLLBAR_AS_NEEDED ) ;
        sp.setVerticalScrollBarPolicy( VERTICAL_SCROLLBAR_AS_NEEDED ) ;
        
        add( sp, BorderLayout.CENTER ) ;
    }
    
    private void addHelpContent( List<String> helpContent ) {
        StringBuilder sb = new StringBuilder() ;
        for( String content : helpContent ) {
            sb.append( content ).append( "\n" );
        }
        textArea.setText( sb.toString() ) ;
    }
}
