package com.sandy.sconsole.qimgextractor.ui.core.imgpanel;

import java.awt.*;
import java.awt.image.BufferedImage;

public interface ImagePanelListener {
    void subImageSelected( BufferedImage image, int selectionModifier );
    void subImageBoundResized( Point anchor, Point hook );
}
