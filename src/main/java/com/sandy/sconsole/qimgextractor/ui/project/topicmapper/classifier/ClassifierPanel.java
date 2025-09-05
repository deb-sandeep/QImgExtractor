package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier;

import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;

import javax.swing.*;
import java.awt.*;

public class ClassifierPanel extends JPanel {

    private final QImgPanel qImgPanel ;
    private final TopicSelectionPanel topicSelectionPanel ;
    private final JLabel qIdLabel ;
    
    public ClassifierPanel( TopicMapperUI parent ) {
        this.qIdLabel = new JLabel() ;
        this.qImgPanel = new QImgPanel() ;
        this.topicSelectionPanel = new TopicSelectionPanel( parent ) ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        
        qIdLabel.setFont( new Font( "Courier New", Font.BOLD, 20 ) ) ;
        qIdLabel.setForeground( Color.BLUE ) ;
        qIdLabel.setBorder( BorderFactory.createEmptyBorder( 5, 0, 5, 0 ) ) ;
        
        setLayout( new BorderLayout() ) ;
        add( createNorthPanel(), BorderLayout.NORTH ) ;
        add( topicSelectionPanel, BorderLayout.CENTER ) ;
    }
    
    private JPanel createNorthPanel() {
        JPanel panel = new JPanel() ;
        panel.setLayout( new BorderLayout() ) ;
        panel.add( qIdLabel, BorderLayout.NORTH ) ;
        panel.add( qImgPanel, BorderLayout.CENTER ) ;
        return panel ;
    }
    
    public void displayQuestion( Question question ) {
        if( question == null ) {
            this.qIdLabel.setText( "" ) ;
        }
        else {
            String qId = question.getQID().toString() ;
            qId = qId.replaceAll( "/", " " ) ;
            String topicName = question.getTopic() != null ? question.getTopic().getName() : "" ;
            this.qIdLabel.setText( "<html>" + qId + " [<span style='color:red'>" + topicName + "</span>]</html>" ) ;
        }
        this.qImgPanel.displayQuestion( question ) ;
        this.topicSelectionPanel.showTopics( question ) ;
    }
}
