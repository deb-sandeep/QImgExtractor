package com.sandy.sconsole.qimgextractor.ui.project.ansmapper;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.img.ImgPanel;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table.AnswerTable;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree.QuestionTreePanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.Collections;
import java.util.Stack;

@Slf4j
public class AnswerMapperUI extends JPanel {
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final AnswerTable answerTable ;
    private final ImgPanel imgPanel ;
    private final QuestionTreePanel questionTreePanel ;

    public AnswerMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.answerTable = new AnswerTable( this.projectModel ) ;
        this.questionTreePanel = new QuestionTreePanel( this ) ;
        this.imgPanel = new ImgPanel( this ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( questionTreePanel, BorderLayout.WEST ) ;
        add( new JScrollPane( answerTable ), BorderLayout.CENTER ) ;
        add( imgPanel, BorderLayout.EAST ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        questionTreePanel.refreshTree() ;
        imgPanel.refreshAnswerKeyPages() ;
        answerTable.refreshTable() ;
    }
    
    public void setOCRGeneratedAnswers( String text ) {
        
        if( text == null || text.trim().isEmpty() ) {
            return;
        }
        
        try {
            Stack<String> answerStack = new Stack<>();
            String[] lines = text.split( "\n" );
            
            for( String line : lines ) {
                String trimmedLine = line.trim();
                if( !trimmedLine.isEmpty() ) {
                    answerStack.push( trimmedLine ) ;
                }
            }
            Collections.reverse( answerStack ) ;
            answerTable.setRawAnswers( answerStack ) ;
            projectModel.getQuestionRepo().save() ;
        }
        catch( Question.InvalidAnswerException e ) {
            AppUtil.showErrorMsg( "Invalid answer found in OCR generated text.\n" + e.getMessage() ) ;
            log.error( "Invalid answer found in OCR generated text.", e ) ;
            log.error( "OCR Text : {} ", text ) ;
        }
    }
}
