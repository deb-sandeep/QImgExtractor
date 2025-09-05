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
        
        qIdLabel.setFont( new Font( "Courier New", Font.PLAIN, 20 ) ) ;
        
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
            this.qIdLabel.setText( question.getQRef() ) ;
        }
        this.qImgPanel.displayQuestion( question ) ;
        this.topicSelectionPanel.showTopics( question ) ;
    }
}
