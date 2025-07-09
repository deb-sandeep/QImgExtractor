package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

public interface ExtractedImgListener {
    /**
     * Returns a not null tag for the image if the image is successfully
     * processed, else null.
     *
     * @param image The raster data for the selected image. This is an
     *        unscaled extract from the original image raster data.
     * @param subImgBounds The bounds of the selected image as per the
     *        original image coordinate frame.
     * @param selectionModifier The mouse button that was pressed to
     *        end the selection.
     */
    String subImageSelected( BufferedImage image, Rectangle subImgBounds, int selectionModifier );
    
    /**
     * Called with meta data of all the selected regions every time
     * a new selected region is successfully processed.
     */
    void selectedRegionsUpdated( List<ExtractedImgInfo> selectedRegionsInfo, File imgFile ) ;
    
    /**
     * If the image extractor canvas is in COMMAND mode and a key other than
     * Escape is pressed, it is propagated to the listener as command key.
     */
    void processCommandKey( int keyCode ) ;
}
