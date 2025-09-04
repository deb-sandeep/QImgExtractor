package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
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
    
    public TopicMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        setBackground( Color.GREEN ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
    }
}
