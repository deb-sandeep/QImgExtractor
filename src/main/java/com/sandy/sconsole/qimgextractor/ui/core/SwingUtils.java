package com.sandy.sconsole.qimgextractor.ui.core;

import com.sandy.sconsole.qimgextractor.ui.core.action.AbstractBaseAction;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.UIManager.LookAndFeelInfo;
import java.awt.*;
import java.awt.event.ActionListener;
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
    
    public static void centerOnScreen( Component component, int width, int height ) {
        
        int x = getScreenWidth()/2 - width/2 ;
        int y = getScreenHeight()/2 - height/2 ;
        
        component.setBounds( x, y, width, height ) ;
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
        Image image = Toolkit.getDefaultToolkit().getImage( url ) ;
        return new ImageIcon( image ) ;
    }
    
    public static BufferedImage getIconImage( Class<?> mainClass, String iconName ) {
        BufferedImage img = null ;
        try {
            URL url = getResource( "/icons/" + iconName + ".png" ) ;
            img = ImageIO.read( url ) ;
        }
        catch( IOException e ) {
            throw new RuntimeException( e ) ;
        }
        return img ;
    }
    
    public static void setNimbusLookAndFeel() {
        try {
            for( LookAndFeelInfo info : UIManager.getInstalledLookAndFeels() ) {
                if( "Nimbus".equals(info.getName() ) ) {
                    UIManager.setLookAndFeel( info.getClassName() ) ;
                    break;
                }
            }
        } 
        catch( Exception e ) {
            log.error( "Unable to set Nimbus Look and Feel", e ) ;
        }
    }
    
    public static JButton getActionBtn( AbstractBaseAction action ) {
        
        JButton button = new JButton() ;
        
        button.setIcon( action.getSmallIcon() ) ;
        button.setMargin( new Insets( 0, 0, 0, 0 ) ) ;
        button.setBorderPainted( false ) ;
        button.setFocusPainted( true ) ;
        button.setIconTextGap( 0 ) ;
        button.setPreferredSize( new Dimension( 30, 30 ) );
        button.setAction( action ) ;
        button.setText( null ) ;
        button.setOpaque( true ) ;
        button.setBackground( Color.GRAY ) ;
        
        return button ;
    }
    
    public static JButton getActionBtn( String iconName, String actionCmd,
                                        ActionListener listener ) {
        
        JButton button = new JButton() ;
        
        button.setIcon( getIcon( iconName ) ) ;
        button.setMargin( new Insets( 0, 0, 0, 0 ) ) ;
        button.setBorderPainted( false ) ;
        button.setFocusPainted( true ) ;
        button.setIconTextGap( 0 ) ;
        button.setPreferredSize( new Dimension( 30, 30 ) );
        button.setActionCommand( actionCmd ) ;
        button.addActionListener( listener ) ;
        button.setOpaque( true ) ;
        button.setBackground( Color.GRAY ) ;
        
        return button ;
    }
    
    public static void setPanelBackground( Color bgColor, JPanel panel ) {
        
        UIDefaults defaults = new UIDefaults();
        defaults.put( "Panel.background", bgColor ) ;
        setNibusOverridesProperty( panel, defaults ) ;
        panel.setBackground( bgColor ) ;
    }
    
    public static void setTextPaneBackground( Color bgColor, JTextPane textPane ) {
        
        UIDefaults defaults = new UIDefaults();
        defaults.put( "TextPane[Enabled].backgroundPainter", bgColor ) ;
        defaults.put( "TextPane[Disabled].backgroundPainter", bgColor ) ;
        setNibusOverridesProperty( textPane, defaults ) ;
        textPane.setBackground( bgColor ) ;
    }
    
    public static void setScrollBarBackground( Color bgColor, JScrollBar sb ) {
        
        UIDefaults defaults = new UIDefaults();
        defaults.put( "ScrollBar:ScrollBarTrack[Enabled].backgroundPainter", bgColor ) ;
        setNibusOverridesProperty( sb, defaults ) ;
        sb.setBackground( bgColor ) ;
    }
    
    public static void setSplitPaneBackground( Color bgColor, JSplitPane sp ) {
        
        UIDefaults defaults = new UIDefaults();
        defaults.put( "SplitPane:SplitPaneDivider[Enabled].backgroundPainter", bgColor ) ;
        setNibusOverridesProperty( sp, defaults ) ;
        sp.setBackground( bgColor ) ;
    }
    
    private static void setNibusOverridesProperty( JComponent comp, UIDefaults uid ) {
        
        comp.putClientProperty( "Nimbus.Overrides", uid ) ;
        comp.putClientProperty( "Nimbus.Overrides.InheritDefaults", true ) ;
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
