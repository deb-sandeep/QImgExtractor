package com.sandy.sconsole.qimgextractor.ui.core.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ImageExtractorPanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;

@Slf4j
public class ImageCanvas extends JLabel {
    
    private final ImageExtractorPanel parent ;
    private final RegionSelector      regionSelector;
    
    private BufferedImage originalImage = null ;
    private BufferedImage scaledImg = null ;
    
    @Getter private double scaleFactor = 1.0f ;

    public ImageCanvas( ImageExtractorPanel parent ) {
        
        this.parent = parent ;
        
        setBackground( Color.WHITE ) ;
        
        regionSelector = new RegionSelector( this ) ;
        addMouseListener( regionSelector ) ;
        addMouseMotionListener( regionSelector ) ;
        addEventListeners() ;
    }
    
    private void addEventListeners() {
        super.addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
            if( e.getKeyCode() == KeyEvent.VK_ESCAPE ) {
                regionSelector.clearActiveSelection() ;
            }
            }
        } ) ;
    }
    
    public void setOriginalImage( BufferedImage img ) {
        originalImage = img ;
        scaledImg = img ;
        scaleFactor = 1.0f ;
        regionSelector.clearActiveSelection() ;
    }
    
    public void scaleImage( double factor ) {
        
        double regionScaleFactor = factor/scaleFactor ;
        regionSelector.scaleSelectedRegions( regionScaleFactor ) ;
        
        scaleFactor = factor ;
        scaledImg = getScaledImage() ;
        
        super.setPreferredSize( new Dimension( scaledImg.getWidth(),
                                               scaledImg.getHeight() ) ) ;
        super.repaint() ;
    }
    
    public void paintComponent( Graphics g ){
        
        super.paintComponent( g ) ;
        g.drawImage( this.scaledImg, 0, 0, null ) ;
        regionSelector.paintSelectedRegions( (Graphics2D)g ) ;
    }
    
    private BufferedImage getScaledImage() {
        
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
    
    String subImageSelected( Rectangle viewRect, int selectionFlag ) {
        
        Rectangle modelRect = new Rectangle( viewRect ) ;
        modelRect.x      = (int)( viewRect.x / scaleFactor ) ;
        modelRect.y      = (int)( viewRect.y / scaleFactor ) ;
        modelRect.width  = (int)( viewRect.width / scaleFactor ) ;
        modelRect.height = (int)( viewRect.height / scaleFactor ) ;
        
        BufferedImage subImg = originalImage.getSubimage( modelRect.x, modelRect.y,
                                                          modelRect.width, modelRect.height ) ;
        
        return parent.subImageSelected( subImg, selectionFlag ) ;
    }
    
    public void destroy() {
        if( originalImage != null ) {
            originalImage.flush() ;
            if( scaledImg != originalImage ) {
                scaledImg.flush() ;
            }
        }
    }
}
