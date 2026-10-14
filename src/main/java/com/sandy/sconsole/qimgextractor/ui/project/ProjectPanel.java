package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class ProjectPanel extends JPanel {
    
    public enum EditorMode {
        IMAGE_SCRAPER,
        ANSWER_MAPPER,
        TOPIC_MAPPER,
        QUESTION_SYNC
    }
    
    @Getter
    private final MainFrame mainFrame ;
    
    @Getter
    private final ProjectModel projectModel ;

    private final CardLayout cardLayout ;
    
    private final ImageScraperUI imgScraperUI ;
    private final AnswerMapperUI answerMapperUI ;
    private final TopicMapperUI topicMapperUI ;
    private final QSyncUI qSyncUI ;
    
    private EditorMode currentMode = null ;
    
    public ProjectPanel( MainFrame mainFrame, ProjectModel model ) {
        this.projectModel = model ;
        this.mainFrame = mainFrame ;
        this.cardLayout = new CardLayout() ;
        this.imgScraperUI = new ImageScraperUI( this ) ;
        this.answerMapperUI = new AnswerMapperUI( this ) ;
        this.topicMapperUI = new TopicMapperUI( this ) ;
        this.qSyncUI = new QSyncUI( this ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( cardLayout ) ;
        add( imgScraperUI, EditorMode.IMAGE_SCRAPER.name() ) ;
        add( answerMapperUI, EditorMode.ANSWER_MAPPER.name() ) ;
        add( topicMapperUI, EditorMode.TOPIC_MAPPER.name() ) ;
        add( qSyncUI, EditorMode.QUESTION_SYNC.name() ) ;
        
        activateQuestionScraperUI() ;
    }
    
    // This is called once the project panel is successfully attached to the
    // main frame. This is where any long-running operations like loading of
    // page images etc should be done.
    public void performPostInitOperations() {
        imgScraperUI.loadPageImages() ;
    }
    
    public void showQuestionImage( String qImgFileName ) {
        activateQuestionScraperUI() ;
        imgScraperUI.showQuestionImage( qImgFileName ) ;
    }
    
    public void destroy() {
        this.imgScraperUI.destroy() ;
    }
    
    public void activateQuestionScraperUI() {
        log.debug( "Activating QuestionScraperUI" ) ;
        mainFrame.clearAISuggestionMsg() ;
        imgScraperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.IMAGE_SCRAPER.name() ) ;
        currentMode = EditorMode.IMAGE_SCRAPER ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.IMAGE_SCRAPER ) ;
    }

    public void activateAnswerMapperUI() {
        log.debug( "Activating AnswerMapperUI" ) ;
        mainFrame.clearAISuggestionMsg() ;
        answerMapperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.ANSWER_MAPPER.name() ) ;
        currentMode = EditorMode.ANSWER_MAPPER ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.ANSWER_MAPPER ) ;
    }
    
    public void activateTopicMapperUI() {
        log.debug( "Activating TopicMapperUI" ) ;
        topicMapperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.TOPIC_MAPPER.name() ) ;
        currentMode = EditorMode.TOPIC_MAPPER ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.TOPIC_MAPPER ) ;
    }
    
    public void activateQSyncUI() {
        log.debug( "Activating QSyncUI" ) ;
        mainFrame.clearAISuggestionMsg() ;
        qSyncUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.QUESTION_SYNC.name() ) ;
        currentMode = EditorMode.QUESTION_SYNC ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.QUESTION_SYNC ) ;
    }
    
    public void toggleAnswerKeyMarkerForActivePage() {
        imgScraperUI.toggleAnswerKeyMarkerForActivePage() ;
    }

    public void extractAnswers() {
        answerMapperUI.extractAnswers() ;
    }

    public void closeActiveImageScraperTab() {
        imgScraperUI.closeCurrentTab() ;
    }
    
    public void zoomAllPagesToFitPage() {
        imgScraperUI.zoomAllToFitPage() ;
    }
    
    public void zoomAllPagesToWidth() {
        imgScraperUI.zoomAllToWidth() ;
    }
    
    // Shows the question sync view (if not already visible) so that the
    // sync progress can be seen, and syncs all the sync pending questions.
    public void syncAllPending() {
        if( currentMode != EditorMode.QUESTION_SYNC ) {
            activateQSyncUI() ;
        }
        qSyncUI.handleMenuAction( QSyncUI.AC_SYNC_ALL_PENDING ) ;
    }
    
    public void handleMenuAction( String actionCmd ) {
        Component[] components = getComponents() ;
        for( Component component : components ) {
            if( component instanceof QSyncUI ) {
                ((QSyncUI)component).handleMenuAction( actionCmd ) ;
            }
        }
    }
    
    public void reloadAISuggestions() {
        topicMapperUI.reloadAISuggestions() ;
    }

    public void autoAssociateTopics() {
        topicMapperUI.autoAssociateTopics() ;
    }
}
