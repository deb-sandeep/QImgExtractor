package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

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
    
    private final ImagePanel parent ;
    private final RegionSelector regionSelector;
    
    private BufferedImage originalImage = null ;
    private BufferedImage scaledImg = null ;
    
    @Getter private double scaleFactor = 1.0f ;

    public ImageCanvas( ImagePanel parent ) {
        
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
                    regionSelector.clear() ;
                }
            }
        } ) ;
    }
    
    public void setOriginalImage( BufferedImage img ) {
        this.originalImage = img ;
        this.scaledImg = img ;
        this.scaleFactor = 1.0f ;
        this.regionSelector.clear() ;
    }
    
    public void scaleImage( double scaleFactor ) {
        this.scaleFactor = scaleFactor ;
        this.scaledImg = getScaledImage() ;
        this.regionSelector.clear() ;
        super.setPreferredSize( new Dimension( scaledImg.getWidth(),
                                               scaledImg.getHeight() ) ) ;
        super.repaint() ;
    }
    
    public void paintComponent( Graphics g ){
        
        super.paintComponent( g ) ;
        g.drawImage( this.scaledImg, 0, 0, null ) ;
        regionSelector.paintSelectedRegion( (Graphics2D)g ) ;
    }
    
    private BufferedImage getScaledImage() {
        
        if( this.scaleFactor == 1.0f ) {
            return this.originalImage ;
        }
        
        BufferedImage scaledImg ;
        
        int w = (int)(originalImage.getWidth()*scaleFactor) ;
        int h = (int)(originalImage.getHeight()*scaleFactor) ;
        
        scaledImg = new BufferedImage( w, h, BufferedImage.TYPE_INT_ARGB ) ;
        
        AffineTransform at = new AffineTransform() ;
        at.scale( this.scaleFactor, this.scaleFactor ) ;
        
        AffineTransformOp scaleOp = new AffineTransformOp( at, AffineTransformOp.TYPE_BICUBIC ) ;
        scaledImg = scaleOp.filter( originalImage, scaledImg ) ;
        
        return scaledImg ;
    }
    
    public void subImageSelected( Rectangle viewRect, int selectionFlag ) {
        
        Rectangle modelRect = new Rectangle( viewRect ) ;
        modelRect.x      = (int)( viewRect.x / scaleFactor ) ;
        modelRect.y      = (int)( viewRect.y / scaleFactor ) ;
        modelRect.width  = (int)( viewRect.width / scaleFactor ) ;
        modelRect.height = (int)( viewRect.height / scaleFactor ) ;
        
        BufferedImage subImg = originalImage.getSubimage( modelRect.x, modelRect.y,
                                                          modelRect.width, modelRect.height ) ;
        
        parent.subImageSelected( subImg, selectionFlag ) ;
    }
    
    public void destroy() {
        if( this.originalImage != null ) {
            this.originalImage.flush() ;
            if( this.scaledImg != this.originalImage ) {
                this.scaledImg.flush() ;
            }
        }
    }
}
