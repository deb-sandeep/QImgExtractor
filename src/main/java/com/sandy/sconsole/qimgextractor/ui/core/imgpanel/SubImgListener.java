package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

public interface SubImgListener {
    /**
     * Returns a not null tag for the image if the image is successfully
     * processed, else null.
     *
     * @param imgFile The image file.
     * @param image The raster data for the selected image. This is an
     *        unscaled extract from the original image raster data.
     * @param subImgBounds The bounds of the selected image as per the
     *        original image coordinate frame.
     * @param selectionModifier The mouse button that was pressed to
     *        end the selection.
     */
    String subImageSelected( File imgFile, BufferedImage image,
                             Rectangle subImgBounds, int selectionModifier );
    
    /**
     * Called with meta-data of all the selected regions every time
     * a newly selected region is successfully processed.
     */
    void selectedRegionAdded( File imgFile,
                              SubImgInfo newRegionInfo ) ;
    
    /**
     * If the image extractor canvas is in COMMAND mode and a key other than
     * Escape is pressed, it is propagated to the listener as command key.
     */
    void processCommandKey( int keyCode ) ;
    
    /** Called once the image selection is initiated. */
    void selectionStarted() ;
    
    /**
     * Called once the image selection is completed. Note that this method is
     * called even if the selection is cancelled. The subImageSelected()
     * method is also called at the completion of selection but only if
     * the selection was not cancelled by the user.
     */
    void selectionEnded() ;
}
