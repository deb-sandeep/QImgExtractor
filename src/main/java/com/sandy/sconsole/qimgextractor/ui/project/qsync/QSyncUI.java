package com.sandy.sconsole.qimgextractor.ui.project.qsync;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionRepo;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient.QSyncAPIClient;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.logpanel.QSyncLogPanel;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSTreeTableModel;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSyncTreeTable;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;

import javax.swing.*;
import java.awt.*;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

@Slf4j
public class QSyncUI extends JPanel {
    
    public static final String AC_COLLAPSE_ALL      = "QSyncUI.collapseAll" ;
    public static final String AC_EXPAND_ALL        = "QSyncUI.expandAll" ;
    public static final String AC_EXPAND_SYLLABUS   = "QSyncUI.expandSyllabus" ;
    public static final String AC_SYNC_ALL_PENDING  = "QSyncUI.syncAllPending" ;
    public static final String AC_TOGGLE_SHOW_ONLY_UNSYNCHED = "QSyncUI.toggleShowOnlyUnsynched" ;
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final QSTreeTableModel treeTableModel;
    private final QSyncTreeTable treeTable ;
    private final QSyncLogPanel logPanel ;
    private final QSyncAPIClient apiClient ;
    private final QSyncExecutor syncExecutor ;
    private final JProgressBar syncProgressBar ;
    
    private boolean syncAllRunning = false ;
    
    public QSyncUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        
        this.treeTableModel = new QSTreeTableModel( this, this.projectModel, false ) ;
        this.treeTable = new QSyncTreeTable( this.treeTableModel, this ) ;
        this.logPanel = new QSyncLogPanel() ;
        this.apiClient = new QSyncAPIClient() ;
        this.syncExecutor = new QSyncExecutor( QImgExtractor.getBean( AppConfig.class ).getQSyncThreads() ) ;
        this.syncProgressBar = new JProgressBar() ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        
        this.treeTable.setRootVisible( false ) ;
        this.treeTable.expandSyllabus() ;
        
        syncProgressBar.setStringPainted( true ) ;
        syncProgressBar.setFont( syncProgressBar.getFont().deriveFont( Font.BOLD, 14f ) ) ;
        
        // Sits in the log panel's header, right of its Clear button.
        JPanel progressPanel = new JPanel( new BorderLayout( 8, 0 ) ) ;
        progressPanel.add( new JLabel( "Synced" ), BorderLayout.WEST ) ;
        progressPanel.add( syncProgressBar, BorderLayout.CENTER ) ;
        logPanel.setHeaderComponent( progressPanel ) ;
        
        add( new JScrollPane( treeTable ), BorderLayout.CENTER ) ;
        add( logPanel, BorderLayout.EAST ) ;
        updateSyncProgress() ;
    }
    
    // Shows how many of the project's questions are synced (with no changes
    // since the last sync) to the server of the active profile, as "x / Y".
    // Safe to call from any thread.
    private void updateSyncProgress() {
        if( !SwingUtilities.isEventDispatchThread() ) {
            SwingUtilities.invokeLater( this::updateSyncProgress ) ;
            return ;
        }
        List<Question> questions = projectModel.getQuestionRepo().getQuestionList() ;
        int total = questions.size() ;
        int synced = (int)questions.stream()
                                   .filter( q -> q.isSynced() && !q.isModifiedAfterSync() )
                                   .count() ;
        syncProgressBar.setMaximum( Math.max( total, 1 ) ) ;
        syncProgressBar.setValue( synced ) ;
        syncProgressBar.setString( synced + " / " + total ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        treeTableModel.refreshModel() ;
        treeTable.expandSyllabus() ;
        updateSyncProgress() ;
    }
    
    public void handleMenuAction( String actionCommand ) {
        if( AC_EXPAND_ALL.equals( actionCommand ) ) {
            treeTable.expandAll() ;
        }
        else if( AC_COLLAPSE_ALL.equals( actionCommand ) ) {
            treeTable.collapseAll() ;
        }
        else if( AC_EXPAND_SYLLABUS.equals( actionCommand ) ) {
            treeTable.expandSyllabus() ;
        }
        else if( AC_TOGGLE_SHOW_ONLY_UNSYNCHED.equals( actionCommand ) ) {
            treeTableModel.toggleShowOnlyUnsynced() ;
            treeTable.expandSyllabus() ;
        }
        else if( AC_SYNC_ALL_PENDING.equals( actionCommand ) ) {
            syncAllPending() ;
        }
    }
    
    private void syncAllPending() {
        if( syncAllRunning ) {
            return ;
        }
        syncAllRunning = true ;
        new SwingWorker<Void, Void>() {
            @Override
            protected Void doInBackground() throws Exception {
                treeTableModel.syncAllSyncPendingQuestions() ;
                return null ;
            }
            
            @Override
            protected void done() {
                syncAllRunning = false ;
                try {
                    get() ;
                }
                catch( Exception e ) {
                    log.error( "Sync all pending failed.", e ) ;
                    logPanel.log( "Sync all pending failed." ) ;
                    logPanel.log( ExceptionUtils.getStackTrace( e ) ) ;
                    return ;
                }
                projectPanel.syncAllPendingCompleted() ;
            }
        }.execute() ;
    }
    
    /**
     * Queues the question for sync and returns without waiting. Can be
     * called from any thread. See QSyncExecutor for how syncs are run in
     * parallel.
     *
     * @return the sync's future, or null if the question is already queued
     *         or being synced.
     */
    public Future<?> syncQuestion( Question question ) {
        Future<?> future = syncExecutor.submit( question, () -> runSync( question ) ) ;
        if( future == null ) {
            logPanel.log( "Question " + question.getQID() + " is already being synced. Skipped." ) ;
        }
        return future ;
    }
    
    // Runs on a sync executor thread.
    private void runSync( Question question ) {
        QSyncLogPanel.Buffer log = logPanel.newBuffer() ;
        log.logSeparator() ;
        log.log( "Syncing question " + question.getQID().toString() + "\n" ) ;
        
        try {
            QSyncAPIClient.SyncResult result = apiClient.syncQuestion( question, log ) ;
            if( result != null ) {
                // Locked with the repo's save(), which serializes the sync
                // info of all the questions, and may run on another sync
                // thread right now.
                QuestionRepo repo = projectModel.getQuestionRepo() ;
                synchronized( repo ) {
                    question.setSyncInfo( result.questionId(), new Date(), result.syncToken() ) ;
                }
                repo.save() ;
            }
        }
        catch( Exception e ) {
            log.log( "Error syncing question " ) ;
            log.log( ExceptionUtils.getStackTrace( e ) );
            log.log( "\n" ) ;
        }
        finally {
            log.flush() ;
        }
        updateSyncProgress() ;
        SwingUtilities.invokeLater( () -> {
            treeTable.repaint() ;
            treeTable.collapseSyncedSyllabi() ;
        } ) ;
    }
}
