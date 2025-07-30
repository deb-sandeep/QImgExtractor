package com.sandy.sconsole.qimgextractor.util;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.MainFrame;

import javax.swing.*;
import java.io.File;

public class AppUtil {
    
    public static String stripExtension( File file ) {
        String fileName = file.getName() ;
        if( fileName.lastIndexOf( '.' ) != -1 ) {
            fileName = fileName.substring( 0, fileName.lastIndexOf( '.' ) ) ;
        }
        return fileName ;
    }
    
    public static void showErrorMsg( String msg ) {
        MainFrame frame = QImgExtractor.getBean( MainFrame.class ) ;
        JOptionPane.showMessageDialog( frame, msg, "Error", JOptionPane.ERROR_MESSAGE ) ;
    }

    public static void showErrorMsg( String msg, Throwable t ) {
        JTextArea textArea = new JTextArea() ;
        textArea.setEditable( false ) ;
        textArea.setOpaque( false ) ;  // optional for transparent background
        textArea.setColumns(30) ;
        textArea.setRows(10) ;
        
        StringBuilder sb = new StringBuilder() ;
        sb.append( msg ).append( "\n" ) ;
        sb.append( "Caused by: " ).append( t.getMessage() ).append( "\n" ) ;
        sb.append( "Stack trace:" ).append( "\n" ) ;
        sb.append( getStackTraceAsString( t ) ) ;
        
        JOptionPane.showMessageDialog(null, textArea, "Multiline Message", JOptionPane.INFORMATION_MESSAGE);
    }
    
    private static String getStackTraceAsString( Throwable t ) {
        StringBuilder sb = new StringBuilder() ;
        sb.append( t.getMessage() ).append( "\n" ) ;
        for( StackTraceElement ste : t.getStackTrace() ) {
            sb.append( ste.toString() ).append( "\n" ) ;
        }
        return sb.toString() ;
    }
    
    public static int extractPageNumber( File pageImgFile ) {
        String fileName = pageImgFile.getName() ;
        
        // Strip the file name of its extension
        fileName = fileName.substring( 0, fileName.lastIndexOf( '.' ) ) ;
        
        // Tokenize the file name and extract the last token, that will
        // be the page number
        String[] tokens = fileName.split( "-" ) ;
        return Integer.parseInt( tokens[tokens.length-1] ) ;
    }
}
