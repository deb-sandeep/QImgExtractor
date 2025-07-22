package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.internal.ImageCanvas;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.CustomWidgetStatusComponent;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.MessageStatusComponent;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.StatusBar;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

@Slf4j
public class ImgExtractorPanel extends JPanel implements ChangeListener {
    
    private static final double MAX_SCALE = 2.5 ;
    
    private ImageCanvas imgCanvas ;
    private JSlider imgScaleSlider = null ;
    private JLabel sliderValueLabel = null ;
    private JScrollPane imgScrollPane = null ;
    private StatusBar statusBar = null ;
    
    private MessageStatusComponent modeStatus = null ;
    private MessageStatusComponent regionSizeStatus = null ;
    private MessageStatusComponent fileNameStatus = null ;
    private MessageStatusComponent mousePosStatus = null ;
    private MessageStatusComponent curSelTagNameStatus = null ;
    
    private File curImgFile = null ;
    
    private ExtractedImgListener listener = null ;
    
    public ImgExtractorPanel( ExtractedImgListener listener ) {
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
        
        imgScaleSlider = new JSlider( JSlider.HORIZONTAL ) ;
        imgScaleSlider.setMinorTickSpacing( 1 ) ;
        imgScaleSlider.setPaintTicks( false ) ;
        imgScaleSlider.addChangeListener( this ) ;
        imgScaleSlider.setSnapToTicks( true ) ;
        imgScaleSlider.setPreferredSize( new Dimension( 150, 10 ) ) ;
        imgScaleSlider.setMaximumSize( new Dimension( 150, 10 ) ) ;
        
        sliderValueLabel = new JLabel( "" ) ;
        sliderValueLabel.setHorizontalAlignment( SwingConstants.CENTER ) ;
        sliderValueLabel.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 10 ) ) ;
        sliderValueLabel.setForeground( Color.DARK_GRAY ) ;
        
        initStatusBar() ;
        
        add( imgScrollPane, BorderLayout.CENTER ) ;
        add( statusBar, BorderLayout.SOUTH ) ;
    }
    
    private void initStatusBar() {
        statusBar = new StatusBar() ;
        
        modeStatus = new MessageStatusComponent() ;
        modeStatus.log( "EDITOR MODE" ) ;
        
        fileNameStatus = new MessageStatusComponent() ;
        fileNameStatus.setForeground( Color.GRAY ) ;
        
        regionSizeStatus = new MessageStatusComponent() ;
        setSelectedRegionSize( 0, 0 ) ;
        
        mousePosStatus = new MessageStatusComponent() ;
        logMousePosition( 0, 0 ) ;
        
        curSelTagNameStatus = new MessageStatusComponent() ;
        
        CustomWidgetStatusComponent zoomStatusWidget = new CustomWidgetStatusComponent( getSliderWidget() );
        
        statusBar.addStatusBarComponent( modeStatus, StatusBar.Direction.WEST ) ;
        statusBar.addStatusBarComponent( fileNameStatus, StatusBar.Direction.WEST ) ;
        statusBar.addStatusBarComponent( curSelTagNameStatus, StatusBar.Direction.EAST ) ;
        statusBar.addStatusBarComponent( mousePosStatus, StatusBar.Direction.EAST ) ;
        statusBar.addStatusBarComponent( regionSizeStatus, StatusBar.Direction.EAST ) ;
        statusBar.addStatusBarComponent( zoomStatusWidget, StatusBar.Direction.EAST ) ;
        statusBar.initialize() ;
    }
    
    private JPanel getSliderWidget() {
        JPanel panel = new JPanel( new BorderLayout() ) ;
        panel.add( imgScaleSlider, BorderLayout.CENTER ) ;
        panel.add( sliderValueLabel, BorderLayout.EAST ) ;
        return panel ;
    }
    
    public void setImage( File pngFile, List<ExtractedImgInfo> imgInfoList,
                          int preferredImgWidth ) {
        
        try {
            BufferedImage img = ImageIO.read( pngFile ) ;
            curImgFile = pngFile ;
            imgCanvas.setOriginalImage( img, imgInfoList ) ;
            
            double sf = ( double )preferredImgWidth / img.getWidth() ;
            int sliderVal = convertScaleFactorToSliderValue( sf ) ;
            this.imgScaleSlider.setValue( sliderVal ) ;
            
            fileNameStatus.log( curImgFile.getAbsolutePath() );
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
            this.sliderValueLabel.setText( String.format( "%.0f%%", scaleFactor*100 ) ) ;
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
    
    public String subImageSelected( BufferedImage subImg, Rectangle subImgBounds, int selectionFlag ) {
        if( listener != null ) {
            return listener.subImageSelected( curImgFile, subImg, subImgBounds, selectionFlag ) ;
        }
        else {
            log.warn( "No extracted image listener available." ) ;
        }
        return null ;
    }
    
    public void selectedRegionsUpdated( List<ExtractedImgInfo> selectedRegionsInfo ) {
        listener.selectedRegionsUpdated( selectedRegionsInfo, curImgFile ) ;
    }
    
    public void setModeStatus( String mode) {
        modeStatus.log( mode ) ;
    }
    
    public void setSelectedRegionSize( int width, int height ) {
        regionSizeStatus.log( width + " x " + height ) ;
    }
    
    public void logMousePosition( int x, int y ) {
        mousePosStatus.log( x + ", " + y ) ;
    }
    
    public void emitCommandKey( int keyCode ) {
        listener.processCommandKey( keyCode ) ;
    }
    
    public void selectionStarted() {
        listener.selectionStarted() ;
    }
    
    public void selectionEnded() {
        listener.selectionEnded() ;
    }
    
    public void setCurSelTagName( String tagName ) {
        curSelTagNameStatus.log( tagName ) ;
    }
    
    public void clearCurSelTagName() {
        curSelTagNameStatus.clear() ;
    }
}
