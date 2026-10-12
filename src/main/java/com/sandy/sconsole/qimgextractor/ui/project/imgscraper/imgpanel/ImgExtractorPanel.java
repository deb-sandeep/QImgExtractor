package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.internal.ImgCanvas;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.CustomWidgetStatusComponent;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.MessageStatusComponent;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.StatusBar;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModelListener;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;

@Slf4j
public class ImgExtractorPanel extends JPanel
        implements ChangeListener, ProjectModelListener {
    
    private static final double MAX_SCALE = 2.5 ;
    
    private ImgCanvas imgCanvas ;
    private JSlider imgScaleSlider = null ;
    private JLabel sliderValueLabel = null ;
    private JScrollPane imgScrollPane = null ;
    private StatusBar statusBar = null ;
    
    private MessageStatusComponent modeStatus = null ;
    private MessageStatusComponent regionSizeStatus = null ;
    private MessageStatusComponent fileNameStatus = null ;
    private MessageStatusComponent mousePosStatus = null ;
    private MessageStatusComponent curSelTagNameStatus = null ;
    private MessageStatusComponent partModeSBComponent = null ;
    private MessageStatusComponent guideModeSBComponent = null ;
    
    private final ImgCanvasListener listener ;
    
    @Getter private PageImage pageImg = null ;
    
    private int origImgHeight = 0 ;
    private int defaultScaleSliderVal = 50 ;
    
    // Set while the slider is being moved programmatically to reflect a
    // scale factor which has already been applied to the canvas.
    private boolean suppressSliderEvents = false ;
    
    public ImgExtractorPanel( ImgCanvasListener listener ) {
        super( new BorderLayout() ) ;
        this.listener = listener ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        
        imgCanvas = new ImgCanvas( this, listener ) ;
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
        modeStatus.log( imgCanvas.getOpMode() + " MODE" ) ;

        partModeSBComponent = new MessageStatusComponent() ;

        guideModeSBComponent = new MessageStatusComponent() ;

        fileNameStatus = new MessageStatusComponent() ;
        fileNameStatus.setForeground( Color.GRAY ) ;
        
        regionSizeStatus = new MessageStatusComponent() ;
        setSelectedRegionSize( 0, 0 ) ;
        
        mousePosStatus = new MessageStatusComponent() ;
        logMousePosition( 0, 0 ) ;
        
        curSelTagNameStatus = new MessageStatusComponent() ;
        
        CustomWidgetStatusComponent zoomStatusWidget = new CustomWidgetStatusComponent( getSliderWidget() );
        
        statusBar.addStatusBarComponent( modeStatus, StatusBar.Direction.WEST ) ;
        statusBar.addStatusBarComponent( partModeSBComponent, StatusBar.Direction.WEST ) ;
        statusBar.addStatusBarComponent( guideModeSBComponent, StatusBar.Direction.WEST ) ;
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
    
    public void setImage( PageImage pageImg, int preferredImgWidth ) {
        
        try {
            this.pageImg = pageImg ;
            
            BufferedImage img = ImageIO.read( pageImg.getImgFile() ) ;
            imgCanvas.setOriginalImage( img, pageImg.getSelectedRegionsMetadataList() ) ;
            
            origImgHeight = img.getHeight() ;
            
            double sf = ( double )preferredImgWidth / img.getWidth() ;
            defaultScaleSliderVal = convertScaleFactorToSliderValue( sf ) ;
            this.imgScaleSlider.setValue( defaultScaleSliderVal ) ;
            
            fileNameStatus.log( pageImg.getImgFile().getName() ) ;
        }
        catch( IOException e ) {
            log.error( "Error setting image." , e ) ;
        }
    }
    
    public void stateChanged( ChangeEvent c ) {
        
        if( !suppressSliderEvents && !this.imgScaleSlider.getValueIsAdjusting() ) {
            applyScaleFactor( convertSliderValueToScaleFactor( this.imgScaleSlider.getValue() ) ) ;
        }
    }
    
    private void applyScaleFactor( double scaleFactor ) {
        this.imgCanvas.scaleImage( scaleFactor ) ;
        this.imgScrollPane.setViewportView( imgCanvas ) ;
        this.sliderValueLabel.setText( String.format( "%.0f%%", scaleFactor*100 ) ) ;
    }
    
    /**
     * Height available to the image inside the scroll pane, excluding the
     * scroll pane border. Returns 0 if this panel has not been laid out yet.
     */
    public int getImgViewportHeight() {
        Insets insets = imgScrollPane.getInsets() ;
        return Math.max( 0, imgScrollPane.getHeight() - insets.top - insets.bottom ) ;
    }
    
    /**
     * Zooms the image such that its full height fits the given viewport
     * height. The slider can only represent discrete zoom levels, so the
     * exact scale factor is applied to the canvas and the slider is moved
     * to the nearest level without triggering a rescale.
     */
    public void zoomToFitHeight( int viewportHeight ) {
        if( origImgHeight <= 0 || viewportHeight <= 0 ) return ;
        
        double scaleFactor = ( double )viewportHeight / origImgHeight ;
        applyScaleFactor( scaleFactor ) ;
        
        suppressSliderEvents = true ;
        try {
            imgScaleSlider.setValue( convertScaleFactorToSliderValue( scaleFactor ) ) ;
        }
        finally {
            suppressSliderEvents = false ;
        }
    }
    
    public void zoomToDefault() {
        if( imgScaleSlider.getValue() == defaultScaleSliderVal ) {
            // Slider won't fire a change event, but the canvas may be at a
            // fit-to-height scale which doesn't map exactly to the slider.
            applyScaleFactor( convertSliderValueToScaleFactor( defaultScaleSliderVal ) ) ;
        }
        else {
            imgScaleSlider.setValue( defaultScaleSliderVal ) ;
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
    
    public String subImageSelected( BufferedImage subImg, Rectangle subImgBounds, int selectionEndAction ) {
        if( listener != null ) {
            return listener.subImageSelected( pageImg.getImgFile(), subImg,
                                              subImgBounds, selectionEndAction ) ;
        }
        else {
            log.warn( "No extracted image listener available." ) ;
        }
        return null ;
    }
    
    public void selectedRegionAdded( SelectedRegionMetadata newRegionInfo ) {
        listener.selectedRegionAdded( pageImg, newRegionInfo ) ;
    }
    
    public boolean scrollToRegion( String tag ) {
        validate() ;
        return imgCanvas.scrollToRegion( tag ) ;
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
    
    public void setCurSelTagName( String tagName ) {
        curSelTagNameStatus.log( tagName ) ;
    }
    
    public void clearCurSelTagName() {
        curSelTagNameStatus.clear() ;
    }
    
    // Ignore this - this class is the one updating the model on addition of question images.
    @Override
    public void newQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {}
    
    @Override
    public void questionTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        if( pageImg.getQImgList().contains( qImg ) ) {
            imgCanvas.renameSelectedRegion( oldTagName, newTagName ) ;
        }
    }
    
    @Override
    public void questionImgDeleted( QuestionImage qImg ) {
        String tagName = qImg.getImgRegionMetadata().getTag() ;
        if( imgCanvas.containsTag( tagName ) ) {
            imgCanvas.deleteSelectedRegion( qImg.getImgRegionMetadata().getTag() ) ;
        }
    }
    
    @Override
    public void partSelectionModeUpdated( boolean newMode ) {
        if( newMode ) {
            partModeSBComponent.log( "PART MODE" ) ;
        }
        else {
            partModeSBComponent.clear() ;
        }
    }

    public void setGuideModeStatus( boolean active ) {
        if( active ) {
            guideModeSBComponent.log( "GUIDE MODE" ) ;
        }
        else {
            guideModeSBComponent.clear() ;
        }
    }
    
    @Override
    public void requestFocus() {
        super.requestFocus() ;
        if( imgCanvas != null ) {
            SwingUtilities.invokeLater( () -> imgCanvas.requestFocus() ) ;
        }
    }
}
