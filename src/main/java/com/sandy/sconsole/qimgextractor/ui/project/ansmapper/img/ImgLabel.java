package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.img ;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.* ;
import java.awt.image.BufferedImage;

@Slf4j
public class ImgLabel extends JLabel {

    private final JPanel parentPanel ;
    private final PageImage pageImg ;
    
    public ImgLabel( JPanel parentPanel, PageImage pageImg ) {
        this.parentPanel = parentPanel ;
        this.pageImg = pageImg ;
        
        try {
            renderImage() ;
        }
        catch( Exception e ) {
            log.error( "Error loading image: {}", pageImg.getImgFile().getName(), e ) ;
        }
    }
    
    private void renderImage() throws Exception {
        BufferedImage img = ImageIO.read( pageImg.getImgFile() ) ;
        double sf = ( double )(parentPanel.getWidth()-50) / img.getWidth() ;
        
        BufferedImage scaledImg = SwingUtils.getScaledImage( img, sf ) ;
        setIcon( new ImageIcon( scaledImg ) ) ;
    }
}
