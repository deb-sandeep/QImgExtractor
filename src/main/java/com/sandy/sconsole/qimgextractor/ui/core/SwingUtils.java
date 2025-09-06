package com.sandy.sconsole.qimgextractor.ui.core;

import lombok.extern.slf4j.Slf4j;
import net.sourceforge.tess4j.Tesseract;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
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
    
    public static Object getUserObject( TreePath path ) {
        DefaultMutableTreeNode node = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        return node.getUserObject() ;
    }
    
    public static JButton getToolbarButton( String iconName ) {
        JButton btn = new JButton() ;
        btn.setIcon( getIcon( iconName ) ) ;
        btn.setFocusPainted( false ) ;
        btn.setBorder( BorderFactory.createEmptyBorder() ) ;
        btn.setContentAreaFilled( false ) ;
        btn.setOpaque( false ) ;
        btn.setMargin( new Insets( 5, 0, 5, 0 ) ) ;
        return btn ;
    }
    
    public static BufferedImage getScaledImage( BufferedImage originalImage, double scaleFactor ) {
        
        if( scaleFactor == 1.0f ) {
            return originalImage ;
        }
        
        BufferedImage scaledImg ;
        
        int w = (int)(originalImage.getWidth()*scaleFactor) ;
        int h = (int)(originalImage.getHeight()*scaleFactor) ;
        
        scaledImg = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB ) ;
        
        AffineTransform at = new AffineTransform() ;
        at.scale( scaleFactor, scaleFactor ) ;
        
        AffineTransformOp scaleOp = new AffineTransformOp( at, AffineTransformOp.TYPE_BICUBIC ) ;
        scaledImg = scaleOp.filter( originalImage, scaledImg ) ;
        
        return scaledImg ;
    }
    
    private static BufferedImage increaseResolution( BufferedImage sourceImage ) {
        
        BufferedImage outputImage = new BufferedImage( sourceImage.getWidth() * 2, sourceImage.getHeight() * 2, BufferedImage.TYPE_INT_RGB );
        
        Graphics2D graphics = outputImage.createGraphics();
        graphics.setRenderingHint( RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC );
        graphics.setRenderingHint( RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY );
        graphics.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON );
        
        graphics.drawImage( sourceImage, 0, 0, sourceImage.getWidth() * 2, sourceImage.getHeight() * 2, null );
        graphics.dispose();
        
        return outputImage;
    }
    
    public static String getOCRText( BufferedImage img ) throws Exception {
        
        BufferedImage highResImage = increaseResolution( img );
        
        Tesseract tesseract = new Tesseract();
        tesseract.setDatapath( "/opt/homebrew/opt/tesseract/share/tessdata" );
        tesseract.setLanguage( "eng" );
        tesseract.setVariable( "tessedit_char_whitelist", "ABCDpqrst0123456789()- >,.-+" );
        tesseract.setOcrEngineMode( 1 );
        
        return tesseract.doOCR( highResImage );
    }
    
    public static void expandAll(JTree tree) {
        TreeModel m = tree.getModel();
        Object root = m.getRoot();
        if( root == null ) return;
        SwingUtilities.invokeLater(() -> expandNode(tree, m, root, new TreePath(root)));
    }
    
    private static void expandNode( JTree tree, TreeModel m, Object node, TreePath path) {
        tree.expandPath(path);                 // expand this node first
        int count = m.getChildCount(node);
        for( int i = 0; i < count; i++ ) {
            Object child = m.getChild(node, i);
            expandNode(tree, m, child, path.pathByAddingChild(child));
        }
    }
    
    public static void collapseAll(JTree tree) {
        TreeModel m = tree.getModel();
        Object root = m.getRoot();
        if( root == null ) return;
        
        SwingUtilities.invokeLater(() -> collapseNode(tree, m, root, new TreePath(root)));
    }
    
    private static void collapseNode(JTree tree, TreeModel m, Object node, TreePath path) {
        int count = m.getChildCount(node);
        for( int i = 0; i < count; i++ ) {
            Object child = m.getChild(node, i);
            collapseNode(tree, m, child, path.pathByAddingChild(child));
        }
        tree.collapsePath(path);
    }
}
