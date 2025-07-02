package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.internal.ImageCanvas;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

@Slf4j
public class ImageExtractorPanel extends JPanel implements ChangeListener {
    
    private static final double MAX_SCALE = 2.5 ;
    
    private ImageCanvas imgCanvas;
    private JSlider     imgScaleSlider = null ;
    private JScrollPane imgScrollPane = null ;
    
    private File curImgFile = null ;
    
    private ExtractedImageListener listener = null ;
    
    public ImageExtractorPanel( ExtractedImageListener listener ) {
        super( new BorderLayout() ) ;
        setUpUI() ;
        this.listener = listener ;
    }
    
    private void setUpUI() {
        
        imgCanvas = new ImageCanvas( this ) ;
        imgCanvas.setOpaque( true ) ;
        imgCanvas.setBackground( new Color( 240, 240, 240 ) ) ;
        imgCanvas.setHorizontalTextPosition( JLabel.LEFT ) ;
        imgCanvas.setHorizontalAlignment( SwingConstants.CENTER ) ;
        
        imgScrollPane = new JScrollPane( imgCanvas ) ;
        imgScrollPane.setBackground( Color.WHITE ) ;
        imgScrollPane.getVerticalScrollBar().setUnitIncrement( 10 ) ;
        imgScrollPane.getHorizontalScrollBar().setUnitIncrement( 10 ) ;
        imgScrollPane.setVerticalScrollBarPolicy( JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED ) ;
        
        imgScaleSlider = new JSlider( JSlider.VERTICAL ) ;
        imgScaleSlider.setMinorTickSpacing( 5 ) ;
        imgScaleSlider.setPaintTicks( true ) ;
        imgScaleSlider.addChangeListener( this ) ;
        imgScaleSlider.setSnapToTicks( true ) ;
        
        add( imgScaleSlider, BorderLayout.WEST ) ;
        add( imgScrollPane, BorderLayout.CENTER ) ;
    }
    
    public void setImage( File pngFile ) {
        
        try {
            BufferedImage img = ImageIO.read( pngFile ) ;
            curImgFile = pngFile ;
            imgCanvas.setOriginalImage( img ) ;
            
            double sf = ( double )imgCanvas.getWidth() / img.getWidth() ;
            int sliderVal = convertScaleFactorToSliderValue( sf ) ;
            this.imgScaleSlider.setValue( sliderVal ) ;
        }
        catch( IOException e ) {
            log.error( "Error setting image." , e ) ;
        }
    }
    
    public void stateChanged( ChangeEvent c ) {
        
        if( !this.imgScaleSlider.getValueIsAdjusting() ) {
            
            double scaleFactor = convertSliderValueToScaleFactor( this.imgScaleSlider.getValue() ) ;
            this.imgCanvas.scaleImage( scaleFactor ) ;
            this.imgScrollPane.setViewportView( imgCanvas ) ;
        }
    }
    
    private int convertScaleFactorToSliderValue( double scaleFactor ) {
        int delta = (int)(( scaleFactor - 1 )*(50/MAX_SCALE)) ;
        int sv = delta + 50 ;
        while( convertSliderValueToScaleFactor( sv ) > scaleFactor ) {
            sv -= 1 ;
        }
        return sv ;
    }
    
    private double convertSliderValueToScaleFactor( int sliderVal ) {
        double delta = Math.abs(sliderVal-50) ;
        double scaleFactor = 1 + ( (MAX_SCALE/50)*delta ) ;
        if( sliderVal < 50 ) {
            scaleFactor = 1/scaleFactor ;
        }
        return scaleFactor ;
    }
    
    public void setToolTipText( String text ) {
        this.imgCanvas.setToolTipText( text ) ;
    }
    
    public void destroy() {
        this.imgCanvas.destroy() ;
    }
    
    public String subImageSelected( BufferedImage subImg, int selectionFlag ) {
        if( listener != null ) {
            return listener.subImageSelected( subImg, selectionFlag ) ;
        }
        else {
            log.warn( "No extracted image listener available." ) ;
        }
        return null ;
    }
}
