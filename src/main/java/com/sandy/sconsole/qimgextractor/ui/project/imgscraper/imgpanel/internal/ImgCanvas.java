package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgCanvasListener;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.SelectedRegionMetadata;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.imgpanel.ImgExtractorPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getProjectModel;

@Slf4j
public class ImgCanvas extends JLabel {
    
    public enum OpMode { EDITOR, COMMAND }
    
    private final ImgExtractorPanel parent ;
    private final ImgCanvasListener listener ;
    private final RegionSelector regionSelector;
    private final WhiteBrush whiteBrush ;
    
    private BufferedImage originalImage = null ;
    private BufferedImage scaledImg = null ;
    
    @Getter private OpMode opMode = OpMode.COMMAND ;
    
    @Getter private double scaleFactor = 1.0f ;

    public ImgCanvas( ImgExtractorPanel parent, ImgCanvasListener listener ) {
        
        this.parent = parent ;
        this.listener = listener ;
        
        setBackground( Color.WHITE ) ;
        
        regionSelector = new RegionSelector( this, listener ) ;
        addMouseListener( regionSelector ) ;
        addMouseMotionListener( regionSelector ) ;
        
        whiteBrush = new WhiteBrush( this ) ;
        addMouseListener( whiteBrush ) ;
        addMouseMotionListener( whiteBrush ) ;
        addEventListeners() ;
    }
    
    private void addEventListeners() {
        super.addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
                
                ProjectContext ctx = getProjectModel().getContext() ;
                
                int keyCode = e.getKeyCode() ;
                if( opMode == OpMode.EDITOR ) {
                    switch ( keyCode ) {
                        case KeyEvent.VK_ESCAPE -> setOpMode( OpMode.COMMAND ) ;
                        case KeyEvent.VK_BACK_SPACE -> regionSelector.clearActiveSelection() ;
                        case KeyEvent.VK_P -> ctx.setPartSelectionModeEnabled( !ctx.isPartSelectionModeEnabled() ) ;
                        case KeyEvent.VK_E -> ctx.setForceNextImgFlag( true ) ;
                    }
                }
                else if( opMode == OpMode.COMMAND ) {
                    switch ( keyCode ) {
                        case KeyEvent.VK_ESCAPE -> setOpMode( OpMode.EDITOR ) ;
                        case KeyEvent.VK_ENTER -> {
                            whiteBrush.setActive( false ) ;
                            regionSelector.enterFreshGuideEditMode() ;
                        }
                        case KeyEvent.VK_B -> {
                            whiteBrush.setActive( false ) ;
                            regionSelector.toggleVMarkSelectionMode() ;
                        }
                        case KeyEvent.VK_C -> regionSelector.clearVerticalMarkers() ;
                        case KeyEvent.VK_W -> toggleWhiteBrush() ;
                        // '/' shrinks the brush, Shift+'/' grows it.
                        case KeyEvent.VK_SLASH -> {
                            if( whiteBrush.isActive() ) {
                                whiteBrush.changeSize( e.isShiftDown() ? 1 : -1 ) ;
                            }
                            else {
                                listener.processImgCanvasCommandKey( keyCode ) ;
                            }
                        }
                        default -> listener.processImgCanvasCommandKey( keyCode ) ;
                    }
                }
            }
        } ) ;
    }
    
    void setOpMode( OpMode opMode ) {
        this.opMode = opMode ;
        if( opMode == OpMode.EDITOR ) {
            regionSelector.exitGuideEditMode() ;
            whiteBrush.setActive( false ) ;
        }
        parent.setModeStatus( opMode.toString() + " MODE" ) ;
        regionSelector.clearActiveSelection() ;
        // Region edges can only be grabbed in COMMAND mode.
        setCursor( Cursor.getDefaultCursor() ) ;
    }

    void setGuideModeStatus( boolean active ) {
        parent.setGuideModeStatus( active ) ;
    }
    
    private void toggleWhiteBrush() {
        boolean activate = !whiteBrush.isActive() ;
        if( activate ) {
            regionSelector.clearActiveSelection() ;
            regionSelector.exitGuideEditMode() ;
        }
        whiteBrush.setActive( activate ) ;
    }
    
    public boolean isBrushActive() {
        return whiteBrush.isActive() ;
    }
    
    void brushStatusChanged() {
        parent.setBrushModeStatus( whiteBrush.isActive(), whiteBrush.getSize() ) ;
    }
    
    // Paints a white line of the given (view pixel) width between two view
    // points on both the original and the displayed image. Returns the area
    // affected in original image coordinates, or null if nothing was painted.
    Rectangle paintWhite( Point from, Point to, int viewWidth ) {
        if( originalImage == null ) return null ;
        
        float modelWidth = (float)( viewWidth / scaleFactor ) ;
        int mx1 = (int)( from.x / scaleFactor ), my1 = (int)( from.y / scaleFactor ) ;
        int mx2 = (int)( to.x / scaleFactor ),   my2 = (int)( to.y / scaleFactor ) ;
        
        drawWhiteLine( originalImage, mx1, my1, mx2, my2, modelWidth ) ;
        if( scaledImg != originalImage ) {
            drawWhiteLine( scaledImg, from.x, from.y, to.x, to.y, viewWidth ) ;
        }
        
        Rectangle viewRect = new Rectangle( from ) ;
        viewRect.add( to ) ;
        viewRect.grow( viewWidth/2 + 2, viewWidth/2 + 2 ) ;
        repaint( viewRect ) ;
        
        Rectangle modelRect = new Rectangle( mx1, my1, 0, 0 ) ;
        modelRect.add( mx2, my2 ) ;
        int pad = (int)Math.ceil( modelWidth/2 ) + 2 ;
        modelRect.grow( pad, pad ) ;
        modelRect = modelRect.intersection( new Rectangle( 0, 0, originalImage.getWidth(),
                                                           originalImage.getHeight() ) ) ;
        return modelRect.isEmpty() ? null : modelRect ;
    }
    
    private static void drawWhiteLine( BufferedImage img, int x1, int y1, int x2, int y2, float width ) {
        Graphics2D g = img.createGraphics() ;
        try {
            g.setColor( Color.WHITE ) ;
            g.setStroke( new BasicStroke( width, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND ) ) ;
            g.drawLine( x1, y1, x2, y2 ) ;
        }
        finally {
            g.dispose() ;
        }
    }
    
    // Called once a brush stroke is complete. The bounds are in original
    // image coordinates.
    void brushStrokeEnded( Rectangle modelBounds ) {
        parent.pageImageBrushed( originalImage,
                                 regionSelector.getRegionsIntersecting( modelBounds ) ) ;
    }
    
    public void setOriginalImage( BufferedImage img,
                                  List<SelectedRegionMetadata> selRegionMetadataList ) {
        originalImage = img ;
        scaledImg = img ;
        scaleFactor = 1.0f ;
        regionSelector.clearActiveSelection() ;
        regionSelector.setSelectedRegions( selRegionMetadataList ) ;
    }
    
    public boolean isInEditMode() {
        return opMode == OpMode.EDITOR ;
    }
    
    public boolean isInCommandMode() {
        return opMode == OpMode.COMMAND ;
    }
    
    public void scaleImage( double factor ) {
        
        regionSelector.scaleSelectedRegions( factor ) ;
        
        scaleFactor = factor ;
        scaledImg = SwingUtils.getScaledImage( originalImage, scaleFactor ) ;
        
        super.setPreferredSize( new Dimension( scaledImg.getWidth(),
                                               scaledImg.getHeight() ) ) ;
        super.repaint() ;
    }
    
    public void paintComponent( Graphics g ){
        
        super.paintComponent( g ) ;
        g.drawImage( this.scaledImg, 0, 0, null ) ;
        regionSelector.paintSelectedRegions( (Graphics2D)g ) ;
        whiteBrush.paintCursor( (Graphics2D)g ) ;
    }
    
    String subImageSelected( Rectangle viewRect, int selectionEndAction ) {
        
        // Reverse scale the view bounds to model bounds.
        log.debug( "  Scaling the view rectangle to model coordinates" ) ;
        Rectangle modelRect = new Rectangle( viewRect ) ;
        modelRect.x = (int)( viewRect.x / scaleFactor ) ;
        modelRect.y = (int)( viewRect.y / scaleFactor ) ;
        modelRect.width = (int)( viewRect.width / scaleFactor ) ;
        modelRect.height = (int)( viewRect.height / scaleFactor ) ;
        log.debug( "    Model rectangle: {}. Scale factor: {}", modelRect, scaleFactor ) ;
        
        log.debug( "  Extracting the sub-image" ) ;
        BufferedImage subImg = originalImage.getSubimage( modelRect.x, modelRect.y,
                                                          modelRect.width, modelRect.height ) ;
        
        return parent.subImageSelected( subImg, modelRect, selectionEndAction ) ;
    }
    
    Dimension getOriginalImageSize() {
        return new Dimension( originalImage.getWidth(), originalImage.getHeight() ) ;
    }
    
    // Called when an existing region has been resized. The bounds are in
    // original image coordinates. Returns true if the question image has been
    // re-cut with the new bounds, false if the resize should be reverted.
    boolean regionResized( String tag, Rectangle modelRect ) {
        BufferedImage subImg = originalImage.getSubimage( modelRect.x, modelRect.y,
                                                          modelRect.width, modelRect.height ) ;
        return parent.subImageResized( tag, subImg, modelRect ) ;
    }
    
    public void destroy() {
        if( originalImage != null ) {
            originalImage.flush() ;
            if( scaledImg != originalImage ) {
                scaledImg.flush() ;
            }
        }
    }
    
    public boolean containsTag( String tagName ) {
        return regionSelector.containsTag( tagName ) ;
    }
    
    // Scrolls the enclosing viewport (only if required) so that the region
    // with the given tag is visible. Returns false if the tag is not found.
    public boolean scrollToRegion( String tag ) {
        Rectangle bounds = regionSelector.getRegionBounds( tag ) ;
        if( bounds == null ) {
            return false ;
        }
        bounds.grow( 20, 20 ) ;
        scrollRectToVisible( bounds ) ;
        return true ;
    }

    public void deleteSelectedRegion( String tag ) {
        regionSelector.deleteSelectedRegion( tag ) ;
    }
    
    public void renameSelectedRegion( String oldTag, String newTag ) {
        regionSelector.renameSelectedRegion( oldTag, newTag ) ;
    }
    
    public void selectedRegionAdded( SelectedRegionMetadata newRegionInfo ) {
        // The region info returned is in the image coordinates of the view. The view
        // is/might be scaled. So before returning information which is relative to original
        // image coordinates, we have to apply a reverse scaling factor.
        newRegionInfo.scale( 1/scaleFactor ) ;
        parent.selectedRegionAdded( newRegionInfo ) ;
    }
    
    public void logActiveRegionSize( Rectangle regionBounds ) {
        if( regionBounds != null ) {
            parent.setSelectedRegionSize( (int)(regionBounds.width/scaleFactor),
                                          (int)(regionBounds.height/scaleFactor) ) ;
        }
        else {
            parent.setSelectedRegionSize( 0, 0 ) ;
        }
    }
    
    public void logMousePosition( Point point ) {
        parent.logMousePosition( (int)(point.x/scaleFactor),
                                 (int)(point.y/scaleFactor) ) ;
    }
}
