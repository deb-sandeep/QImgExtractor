package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier.ClassifierPanel;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.tree.TopicTreePanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class TopicMapperUI extends JPanel {
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final TopicTreePanel  topicTreePanel ;
    private final ClassifierPanel classifierPanel ;
    
    public TopicMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        
        this.topicTreePanel = new TopicTreePanel( this ) ;
        this.classifierPanel = new ClassifierPanel() ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( this.topicTreePanel, BorderLayout.WEST ) ;
        add( this.classifierPanel, BorderLayout.CENTER ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        this.topicTreePanel.refreshTree() ;
    }
    
    public void questionSelected( Question question ) {
        this.classifierPanel.displayQuestion( question ) ;
    }
}
