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
        String text = msg + "\n" +
                    "Caused by: " + t.getMessage() + "\n" +
                    "Stack trace:" + "\n" + getStackTraceAsString( t ) ;
        
        JTextArea textArea = new JTextArea() ;
        textArea.setEditable( false ) ;
        textArea.setOpaque( false ) ;  // optional for transparent background
        textArea.setColumns(80) ;
        textArea.setRows(20) ;
        textArea.setText( text ) ;
        
        JScrollPane sp = new JScrollPane( textArea ) ;
        
        MainFrame frame = QImgExtractor.getBean( MainFrame.class ) ;
        JOptionPane.showMessageDialog( frame, sp, "Message",
                                       JOptionPane.ERROR_MESSAGE ) ;
    }
    
    private static String getStackTraceAsString( Throwable t ) {
        StringBuilder sb = new StringBuilder() ;
        sb.append( t.getMessage() ).append( "\n" ) ;
        for( StackTraceElement ste : t.getStackTrace() ) {
            sb.append( ste.toString() ).append( "\n" ) ;
        }
        return sb.toString() ;
    }
    
    // Page scans are of the format AITS-13-A-FT1P1-02.png. The last
    // segment of the file name is the page sequence number. This method
    // extracts the page sequence number, given a page image file.
    public static int extractPageNumber( File pageImgFile ) {
        String fileName = pageImgFile.getName() ;
        
        // Strip the file name of its extension
        fileName = fileName.substring( 0, fileName.lastIndexOf( '.' ) ) ;
        
        // Tokenize the file name and extract the last token, that will
        // be the page number
        String[] tokens = fileName.split( "-" ) ;
        return Integer.parseInt( tokens[tokens.length-1] ) ;
    }
    
    public static String getFQFileName( String srcId, int pageNumber, String fileName ) {
        return srcId + "." + String.format( "%03d", pageNumber ) + "." + fileName;
    }
}
