package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier;

import com.sandy.sconsole.qimgextractor.ui.project.model.Question;

import javax.swing.*;
import java.awt.*;

public class ClassifierPanel extends JPanel {

    private final QImgPanel qImgPanel ;
    
    public ClassifierPanel() {
        this.qImgPanel = new QImgPanel() ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( qImgPanel, BorderLayout.NORTH ) ;
    }
    
    public void displayQuestion( Question question ) {
        this.qImgPanel.displayQuestion( question ) ;
    }
}
