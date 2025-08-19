package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.img ;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.* ;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;

@Slf4j
public class ImgLabel extends JLabel {

    private final AnswerMapperUI answerMapperUI ;
    private final JPanel parentPanel ;
    private final PageImage pageImg ;
    
    private BufferedImage originalImage ;
    private double scaleFactor = 1.0 ;
    
    private Point startPoint;
    private Point endPoint;
    private boolean selecting = false;
    
    public ImgLabel( JPanel parentPanel, AnswerMapperUI answerMapperUI, PageImage pageImg ) {
        this.answerMapperUI = answerMapperUI ;
        this.parentPanel = parentPanel ;
        this.pageImg = pageImg ;
        
        try {
            renderImage() ;
            addMouseListener( createMouseAdapter() );
            addMouseMotionListener( createMouseAdapter() );
            addKeyListener( createKeyAdapter() ) ;
        }
        catch( Exception e ) {
            log.error( "Error loading image: {}", pageImg.getImgFile().getName(), e ) ;
        }
    }
    
    private void renderImage() throws Exception {
        originalImage = ImageIO.read( pageImg.getImgFile() ) ;
        scaleFactor = ( double )(parentPanel.getWidth()-50) / originalImage.getWidth() ;
        
        BufferedImage displayedImage = SwingUtils.getScaledImage( originalImage, scaleFactor );
        setIcon( new ImageIcon( displayedImage ) );
    }
    
    private MouseAdapter createMouseAdapter() {
        return new MouseAdapter() {
            @Override
            public void mousePressed( MouseEvent e ) {
                if( !selecting ) {
                    selecting = true ;
                    startPoint = e.getPoint();
                }
                else {
                    handleSelectionEnd( e ) ;
                }
            }
            
            @Override
            public void mouseMoved( MouseEvent e ) {
                if( selecting ) {
                    endPoint = e.getPoint();
                    requestFocus() ;
                    repaint();
                }
            }
            
            private void handleSelectionEnd( MouseEvent e ) {
                selecting = false ;
                try {
                    BufferedImage selectedImage = getSelectedImage();
                    if( selectedImage != null ) {
                        selectedImage = cleanGridLines( selectedImage ) ;
                        File outputFile = new File( System.getProperty( "user.home" ) + "/temp/temp.png" ) ;
                        ImageIO.write( selectedImage, "png", outputFile ) ;

                        String text = SwingUtils.getOCRText( selectedImage ) ;
                        answerMapperUI.setOCRGeneratedAnswers( text ) ;
                    }
                }
                catch( Exception ex ) {
                    log.error( "Error saving selected image", ex );
                }
                repaint();
            }
        };
    }
    
    private KeyAdapter createKeyAdapter() {
        return new KeyAdapter() {
            @Override
            public void keyPressed( KeyEvent e ) {
                if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                    selecting = false ;
                    repaint();
                }
            }
        } ;
    }
    
    private BufferedImage cleanGridLines( BufferedImage img ) {
        int w = img.getWidth() ;
        int h = img.getHeight() ;
        
        BufferedImage newImg = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB ) ;
        
        Graphics2D g2d = newImg.createGraphics() ;
        g2d.drawImage( img, 0, 0, null ) ;
        g2d.dispose() ;
        
        for( int y = 0; y < h; y++ ) {
            if( isPixelNotWhite( 0, y, newImg ) ) {
                for( int x = 0; x < w; x++ ) {
                    newImg.setRGB( x, y, Color.WHITE.getRGB() );
                }
            }
        }
        
        for( int x = 0; x < w; x++ ) {
            if( isPixelNotWhite( x, 0, newImg ) ) {
                for( int y = 0; y < h; y++ ) {
                    newImg.setRGB( x, y, Color.WHITE.getRGB() );
                }
            }
        }
        
        return newImg ;
    }
    
    private boolean isPixelNotWhite( int x, int y, BufferedImage img ) {
        int rgb = img.getRGB( x, y );
        int r   = ( rgb >> 16 ) & 0xFF;
        int g   = ( rgb >> 8 ) & 0xFF;
        int b   = rgb & 0xFF;
        
        final int WHITE_THRESHOLD = 250;
        return ( r < WHITE_THRESHOLD || g < WHITE_THRESHOLD || b < WHITE_THRESHOLD ) ;
    }
    
    @Override
    protected void paintComponent( Graphics g ) {
        super.paintComponent( g ) ;
        if( selecting && startPoint != null && endPoint != null ) {
            
            float[] dash = { 3.0f };
            Graphics2D g2d = ( Graphics2D )g;
            g2d.setColor( Color.RED );
            g2d.setStroke( new BasicStroke( 1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, dash, 0.0f ) );
            Rectangle rect = getRectangle();
            g2d.drawRect( rect.x, rect.y, rect.width, rect.height );
        }
    }
    
    private Rectangle getRectangle() {
        return new Rectangle( Math.min( startPoint.x, endPoint.x ),
                              Math.min( startPoint.y, endPoint.y ),
                              Math.abs( endPoint.x - startPoint.x ),
                              Math.abs( endPoint.y - startPoint.y ) ) ;
    }
    
    public BufferedImage getSelectedImage() {
        if( startPoint == null || endPoint == null ) return null;
        
        Rectangle rect = getRectangle() ;
        rect.x = (int)( rect.x / scaleFactor ) ;
        rect.y = (int)( rect.y / scaleFactor ) ;
        rect.width = (int)( rect.width / scaleFactor ) ;
        rect.height = (int)( rect.height / scaleFactor ) ;
        
        return originalImage.getSubimage( rect.x, rect.y, rect.width, rect.height ) ;
    }
}
