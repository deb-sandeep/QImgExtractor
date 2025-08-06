package com.sandy.sconsole.qimgextractor.ui.project.imgpanel;

import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public interface ImgCanvasListener {
    /**
     * Returns a not null tag for the image if the image is successfully
     * processed, else null.
     *
     * @param imgFile The image file.
     * @param image The raster data for the selected image. This is an
     *        unscaled extract from the original image raster data.
     * @param subImgBounds The bounds of the selected image as per the
     *        original image coordinate frame.
     * @param selectionEndAction The mouse button that was pressed to
     *        end the selection.
     */
    String subImageSelected( File imgFile, BufferedImage image,
                             Rectangle subImgBounds, int selectionEndAction );
    
    /**
     * Called with meta-data of all the selected regions every time
     * a newly selected region is successfully processed.
     */
    void selectedRegionAdded( PageImage pageImg,
                              SelectedRegionMetadata regionMeta ) ;
    
    /**
     * If the image extractor canvas is in COMMAND mode and a key other than
     * Escape is pressed, it is propagated to the listener as a command key.
     */
    void processCommandKey( int keyCode ) ;
    
    /** Called once the image selection is initiated. */
    void selectionStarted() ;
    
    /**
     * Called once the image selection is completed. Note that this method is
     * called even if the selection is canceled. The subImageSelected()
     * method is also called at the completion of selection but only if
     * the selection was not canceled by the user.
     */
    void selectionEnded() ;
}
