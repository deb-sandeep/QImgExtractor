package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.List;

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
    
    // Time given to a newly activated view to render before an action is
    // launched in it.
    private static final long VIEW_SETTLE_DELAY_MS = 300 ;
    
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
        updateProjectStageFlags() ;
        mainFrame.clearAISuggestionMsg() ;
        imgScraperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.IMAGE_SCRAPER.name() ) ;
        currentMode = EditorMode.IMAGE_SCRAPER ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.IMAGE_SCRAPER ) ;
    }

    public void activateAnswerMapperUI() {
        log.debug( "Activating AnswerMapperUI" ) ;
        updateProjectStageFlags() ;
        mainFrame.clearAISuggestionMsg() ;
        answerMapperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.ANSWER_MAPPER.name() ) ;
        currentMode = EditorMode.ANSWER_MAPPER ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.ANSWER_MAPPER ) ;
    }
    
    public void activateTopicMapperUI() {
        log.debug( "Activating TopicMapperUI" ) ;
        updateProjectStageFlags() ;
        topicMapperUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.TOPIC_MAPPER.name() ) ;
        currentMode = EditorMode.TOPIC_MAPPER ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.TOPIC_MAPPER ) ;
    }
    
    public void activateQSyncUI() {
        log.debug( "Activating QSyncUI" ) ;
        updateProjectStageFlags() ;
        mainFrame.clearAISuggestionMsg() ;
        qSyncUI.handlePreActivation() ;
        cardLayout.show( this, EditorMode.QUESTION_SYNC.name() ) ;
        currentMode = EditorMode.QUESTION_SYNC ;
        mainFrame.getAppMenuBar().setEditorMode( EditorMode.QUESTION_SYNC ) ;
    }
    
    public void toggleAnswerKeyMarkerForActivePage() {
        imgScraperUI.toggleAnswerKeyMarkerForActivePage() ;
    }

    // Extracting answers means the page review is over. Shows the answer
    // mapper (if not already visible) and launches the extraction once it
    // has rendered.
    public void extractAnswers() {
        ProjectState state = projectModel.getState() ;
        if( !state.isImgCuttingComplete() ) {
            state.setImgCuttingComplete( true ) ;
        }
        runInMode( EditorMode.ANSWER_MAPPER, this::activateAnswerMapperUI,
                   answerMapperUI::extractAnswers ) ;
    }
    
    // Called after answers have been imported into the project.
    public void answersImported() {
        updateProjectStageFlags() ;
    }
    
    // Called when the last open page with question images is closed. Moves
    // on to extracting the answers, or past it if all the questions already
    // have answers.
    public void questionPagesReviewed() {
        if( mainFrame.getCurrentProjectPanel() != this ) {
            return ;
        }
        if( allQuestionsAnswered() ) {
            continueWorkflowAfterAnswers() ;
        }
        else {
            extractAnswers() ;
        }
    }
    
    // Moves on from the answers to mapping topics if any question has no
    // topic, else to syncing the questions. Nothing is done while any
    // question has no answer. Syncing, when done, offers the next project
    // if all the questions are synced (even if none needed syncing).
    public void continueWorkflowAfterAnswers() {
        if( mainFrame.getCurrentProjectPanel() != this || !allQuestionsAnswered() ) {
            return ;
        }
        boolean unclassified = projectModel.getQuestionRepo().getQuestionList()
                .stream().anyMatch( q -> q.getTopic() == null ) ;
        if( unclassified ) {
            if( currentMode != EditorMode.TOPIC_MAPPER ) {
                activateTopicMapperUI() ;
            }
        }
        else {
            syncAllPending() ;
        }
    }
    
    private boolean allQuestionsAnswered() {
        List<Question> questions = projectModel.getQuestionRepo().getQuestionList() ;
        return !questions.isEmpty() && questions.stream().allMatch( q -> q.getAnswer() != null ) ;
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
    // sync progress can be seen, and syncs all the sync pending questions
    // once the view has rendered.
    public void syncAllPending() {
        if( !confirmSyncWithUnassociatedQuestions() ) {
            return ;
        }
        runInMode( EditorMode.QUESTION_SYNC, this::activateQSyncUI,
                   () -> qSyncUI.handleMenuAction( QSyncUI.AC_SYNC_ALL_PENDING ) ) ;
    }
    
    // Questions without a topic are not synced. If there are any, the user can
    // go to the topic mapper or sync the rest anyway. Returns true if the
    // sync should go ahead.
    private boolean confirmSyncWithUnassociatedQuestions() {
        List<String> unassociated = projectModel.getQuestionRepo().getQuestionList()
                .stream()
                .filter( q -> q.getTopic() == null )
                .map( q -> q.getQID().toString() )
                .toList() ;
        if( unassociated.isEmpty() ) {
            return true ;
        }
        
        String[] options = { "Open Topic Mapper", "Sync Anyway" } ;
        int choice = JOptionPane.showOptionDialog( this,
                "There are " + unassociated.size() + " unassociated question(s) which won't be synced:\n" +
                String.join( ", ", unassociated ),
                "Unassociated Questions", JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                null, options, options[0] ) ;
        if( choice == 0 ) {
            if( currentMode != EditorMode.TOPIC_MAPPER ) {
                activateTopicMapperUI() ;
            }
            return false ;
        }
        return choice == 1 ;
    }
    
    // Called by the question sync view after a sync all pending run. If all
    // the questions of the project are now synced, the project is marked
    // as server synced and the user is offered the next project.
    public void syncAllPendingCompleted() {
        if( mainFrame.getCurrentProjectPanel() != this ) {
            return ;
        }
        List<Question> questions = projectModel.getQuestionRepo().getQuestionList() ;
        boolean allSynced = !questions.isEmpty() && questions.stream().allMatch(
                q -> q.isReadyForServerSync() && q.isSynced() && !q.isModifiedAfterSync() ) ;
        if( allSynced ) {
            mainFrame.projectSyncCompleted( questions.size() ) ;
        }
    }
    
    // Runs the action right away if the given mode is active. Else activates
    // the mode and runs the action after giving the view time to render.
    private void runInMode( EditorMode mode, Runnable activator, Runnable action ) {
        if( currentMode == mode ) {
            action.run() ;
            return ;
        }
        activator.run() ;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                Thread.sleep( VIEW_SETTLE_DELAY_MS ) ;
                return null ;
            }
            
            @Override
            protected void done() {
                // The project might have been closed or the view changed
                // during the delay.
                if( mainFrame.getCurrentProjectPanel() == ProjectPanel.this &&
                    currentMode == mode ) {
                    action.run() ;
                }
            }
        }.execute() ;
    }
    
    // Moves the project state forward when the answers or topics of all the
    // questions are in place. The state is never moved back as setting a
    // stage resets the later stages.
    private void updateProjectStageFlags() {
        ProjectState state = projectModel.getState() ;
        List<Question> questions = projectModel.getQuestionRepo().getQuestionList() ;
        if( questions.isEmpty() ) {
            return ;
        }
        if( !state.isAnswersMapped() &&
            questions.stream().allMatch( q -> q.getAnswer() != null ) ) {
            state.setAnswersMapped( true ) ;
        }
        if( !state.isTopicsMapped() && state.isAnswersMapped() &&
            questions.stream().allMatch( q -> q.getTopic() != null ) ) {
            state.setTopicsMapped( true ) ;
        }
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
