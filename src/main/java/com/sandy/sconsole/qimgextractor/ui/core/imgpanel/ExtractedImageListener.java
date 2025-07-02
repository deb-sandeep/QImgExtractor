package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import java.awt.image.BufferedImage;

public interface ExtractedImageListener {
    /**
     * Returns a not null tag for the image if the image is successfully
     * processed, else null.
     */
    String subImageSelected( BufferedImage image, int selectionModifier );
}
