package com.sandy.sconsole.qimgextractor.ui.project.ansmapper;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.img.ImgPanel;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table.AnswerTable;
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
    
    private final AnswerTable answerTable ;
    private final ImgPanel imgPanel ;

    public AnswerMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.answerTable = new AnswerTable( projectModel ) ;
        this.imgPanel = new ImgPanel( projectModel ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( new QuestionTreePanel( this ), BorderLayout.WEST ) ;
        add( new JScrollPane( answerTable ), BorderLayout.CENTER ) ;
        add( imgPanel, BorderLayout.EAST ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        imgPanel.refreshAnswerKeyPages() ;
        answerTable.refreshTable() ;
    }
}
