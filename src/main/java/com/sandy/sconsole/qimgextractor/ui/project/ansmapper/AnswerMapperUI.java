package com.sandy.sconsole.qimgextractor.ui.project.ansmapper;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import lombok.Getter;

import javax.swing.*;

public class AnswerMapperUI extends JPanel {
    
    private final MainFrame mainFrame ; // Injected
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected

    public AnswerMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.mainFrame = projectPanel.getMainFrame() ;
    }
}
