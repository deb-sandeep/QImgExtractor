package com.sandy.sconsole.qimgextractor.ui.core;

import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

@Slf4j
public class SwingUtils {
    
    private static URL getResource( String path ) {
        return SwingUtils.class.getResource( path ) ;
    }

    public static void setMaximized( JFrame frame ) {
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ; 
        frame.setBounds( 0, 0, screenSz.width, screenSz.height ) ;
    }
    
    public static int getScreenWidth() {
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ; 
        return screenSz.width ;
    }
    
    public static int getScreenHeight() {
        Dimension screenSz = Toolkit.getDefaultToolkit().getScreenSize() ;
        return screenSz.height ;
    }
    
    public static ImageIcon getIcon( String iconName ) {
        URL url = getResource( "/icons/" + iconName + ".png" ) ;
        if( url == null ) {
            log.error( "Icon not found: {}", iconName ) ;
        }
        Image image = Toolkit.getDefaultToolkit().getImage( url ) ;
        return new ImageIcon( image ) ;
    }
    
    public static BufferedImage getIconImage(  String iconName ) {
        BufferedImage img ;
        try {
            URL url = getResource( "/icons/" + iconName + ".png" ) ;
            img = ImageIO.read( url ) ;
        }
        catch( IOException e ) {
            throw new RuntimeException( e ) ;
        }
        return img ;
    }
    
    public static int scale( int ordinate, double scaleFactor ) {
        return (int)( ordinate * scaleFactor ) ;
    }
    
    public static Point scale( Point p, double scaleFactor ) {
        return new Point( scale( p.x, scaleFactor ),
                scale( p.y, scaleFactor ) ) ;
    }
    
    public static Rectangle scale( Rectangle r, double scaleFactor ) {
        return new Rectangle( scale( r.x, scaleFactor ),
                scale( r.y, scaleFactor ),
                scale( r.width, scaleFactor ),
                scale( r.height, scaleFactor ) ) ;
    }
}
