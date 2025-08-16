package com.sandy.sconsole.qimgextractor.ui.project.ansmapper;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree.QuestionTreePanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import lombok.Getter;

import javax.swing.*;
import java.awt.*;

public class AnswerMapperUI extends JPanel {
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected

    public AnswerMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( new QuestionTreePanel( this ), BorderLayout.WEST ) ;
    }
    
}
