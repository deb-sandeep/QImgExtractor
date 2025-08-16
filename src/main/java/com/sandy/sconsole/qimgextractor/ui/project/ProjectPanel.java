package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class ProjectPanel extends JPanel {
    
    public enum EditorMode {
        IMAGE_SCRAPER,
        ANSWER_MAPPER,
        TOPIC_MAPPER
    }
    
    @Getter
    private final MainFrame mainFrame ;
    
    @Getter
    private final ProjectModel projectModel ;

    private final CardLayout cardLayout ;
    private final ImageScraperUI imgScraperUI ;
    private final AnswerMapperUI answerMapperUI ;
    
    public ProjectPanel( MainFrame mainFrame, ProjectModel model ) {
        this.projectModel = model ;
        this.mainFrame = mainFrame ;
        this.cardLayout = new CardLayout() ;
        this.imgScraperUI = new ImageScraperUI( this ) ;
        this.answerMapperUI = new AnswerMapperUI( this ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( cardLayout ) ;
        add( imgScraperUI, EditorMode.IMAGE_SCRAPER.name() ) ;
        add( answerMapperUI, EditorMode.ANSWER_MAPPER.name() ) ;
        
        activateQuestionScraperUI() ;
    }
    
    // This is called once the project panel is successfully attached to the
    // main frame. This is where any long-running operations like loading of
    // page images etc should be done.
    public void performPostInitOperations() {
        imgScraperUI.loadPageImages() ;
    }
    
    public void destroy() {
        this.imgScraperUI.destroy() ;
    }
    
    public void activateQuestionScraperUI() {
        log.debug( "Activating QuestionScraperUI" ) ;
        imgScraperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.IMAGE_SCRAPER.name() ) ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.IMAGE_SCRAPER ) ;
    }
    
    public void activateAnswerMapperUI() {
        log.debug( "Activating AnswerMapperUI" ) ;
        answerMapperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.ANSWER_MAPPER.name() ) ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.ANSWER_MAPPER ) ;
    }
    
    public void toggleAnswerKeyMarkerForActivePage() {
        imgScraperUI.toggleAnswerKeyMarkerForActivePage() ;
    }
}
