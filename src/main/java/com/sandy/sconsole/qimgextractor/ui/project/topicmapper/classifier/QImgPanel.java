package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class QImgPanel extends JPanel {

    private static class QImgLabel extends JLabel {
        
        private final JPanel        parentPanel ;
        private final QuestionImage qImg ;
        
        public QImgLabel( JPanel parentPanel, QuestionImage qImg ) {
            this.parentPanel = parentPanel ;
            this.qImg = qImg ;
            renderImage() ;
        }
        
        private void renderImage() {
            try {
                BufferedImage originalImage = ImageIO.read( qImg.getImgFile() ) ;
                double        scaleFactor   = ( double )( parentPanel.getWidth() - 50 ) / originalImage.getWidth() ;
                
                BufferedImage displayedImage = SwingUtils.getScaledImage( originalImage, scaleFactor ) ;
                setIcon( new ImageIcon( displayedImage ) ) ;
            }
            catch( Exception e ) {
                log.error( "Error loading image: {}", qImg.getImgFile().getName(), e ) ;
            }
        }
    }
    
    private final JPanel imgContainer = new JPanel() ;
    private final JScrollPane scrollPane ;
    
    public QImgPanel() {
        
        setPreferredSize( new Dimension( 1000, 300 ) ) ;
        setLayout( new BorderLayout() ) ;
        
        imgContainer.setLayout( new BoxLayout( imgContainer, BoxLayout.Y_AXIS ) ) ;
        
        scrollPane = new JScrollPane( imgContainer ) ;
        scrollPane.getVerticalScrollBar().setUnitIncrement( 10 ) ;
        scrollPane.getHorizontalScrollBar().setUnitIncrement( 10 ) ;
        
        add( scrollPane, BorderLayout.CENTER ) ;
    }
    
    public void displayQuestion( Question question ) {
        imgContainer.removeAll() ;
        question.getQImgList().forEach( qImg -> {
            QImgLabel imgLabel = new QImgLabel( this, qImg ) ;
            imgLabel.setAlignmentX( LEFT_ALIGNMENT ) ;
            imgContainer.add( imgLabel ) ;
        } ) ;
        imgContainer.revalidate() ;
        scrollPane.revalidate() ;
    }
}
