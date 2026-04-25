package com.sandy.sconsole.qimgextractor.util;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.codec.digest.DigestUtils;

import javax.swing.*;
import java.awt.*;
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
        showErrorMsg( frame, msg ) ;
    }

    public static void showErrorMsg( Component parent, String msg ) {
        JOptionPane.showMessageDialog( parent, msg, "Error", JOptionPane.ERROR_MESSAGE ) ;
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
    
    // Page images are generated for two types of projects:
    // - first one in which pages are digitally scanned. These pages are of the
    //   format AITS-13-A-FT1P1-02.png. In this case, the last segment of the
    //   file name is the page sequence number.
    //
    // - the second type is where questions are manually typed into md files and
    //   exported as images. These are manual projects. These images are
    //   of the format 01-Avasthi-NVT.png. In this case, the first sequence of
    //   the file name is the page number.
    //
    public static int extractPageNumber( File pageImgFile, boolean isManualProject ) {
        String fileName = stripFileExtension( pageImgFile ) ;
        // Tokenize the file name and extract the last token, that will
        // be the page number
        String[] tokens = fileName.split( "-" ) ;
        return isManualProject ?
                Integer.parseInt( tokens[0] ) :
                Integer.parseInt( tokens[tokens.length-1] ) ;
    }
    
    public static String getFQFileName( String srcId, int pageNumber, String fileName ) {
        return srcId + "." + String.format( "%03d", pageNumber ) + "." + fileName;
    }
    
    public static String stripFileExtension( File file ) {
        String fileName = file.getName() ;
        return fileName.substring( 0, fileName.lastIndexOf( '.' ) ) ;
    }
    
    public static boolean isValidProjectDir( File projectDir ) {
        File pagesDir = new File( projectDir, "pages" ) ;
        if( pagesDir.exists() ) {
            File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
            return null != files && files.length > 0 ;
        }
        return false ;
    }
    
    public static String getHash( String input ) {
        return new String( Hex.encodeHex( DigestUtils.md5( input ) ) ) ;
    }
}
