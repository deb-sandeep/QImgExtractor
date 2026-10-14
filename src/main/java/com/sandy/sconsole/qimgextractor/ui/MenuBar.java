package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

// Menu items are backed by Actions which are shared with the ToolBar, so
// that enabling/disabling an action reflects in both the menu and toolbar.
@Slf4j
@Getter
public class MenuBar extends JMenuBar {
    
    private final MainFrame mainFrame ;
    
    private ProjectPanel currentProjectPanel ;
    
    // Project menu actions
    private Action openProjectAction ;
    private Action closeProjectAction ;
    private Action reloadAISuggestionsAction ;
    private Action autoAssocTopicsAction ;
    private Action markAnsKeyAction ;
    private Action extractAnswersAction ;
    private Action closeTabAction ;
    
    private Action imageScraperAction ;
    private Action ansMappingAction ;
    private Action topicMappingAction ;
    private Action qSyncAction ;
    
    private Action imgCuttingCompleteAction ;
    private Action answersMappedAction ;
    private Action topicsMappedAction ;
    private Action qSyncCompleteAction ;
    
    // View menu actions
    private Action zoomFitPageAction ;
    private Action zoomDefaultAction ;
    
    // Sync menu actions
    private Action expandAllAction ;
    private Action collapseAllAction ;
    private Action expandSyllabusAction ;
    private Action showOnlyUnsyncedAction ;
    private Action syncAllPendingAction ;
    
    private final JMenu syncMenu ;
            
    MenuBar( MainFrame mainFrame ) {
        this.mainFrame = mainFrame ;
        this.syncMenu = getSyncMenu() ;
        
        super.add( getFileMenu() ) ;
        super.add( getProjectMenu() ) ;
        super.add( getViewMenu() ) ;
        super.add( syncMenu ) ;
        
        setCurrentProjectPanel( null ) ;
    }
    
    private JMenu getFileMenu() {
        // File menu items
        JMenuItem exitMenuItem = new JMenuItem( "Exit" );
        exitMenuItem.addActionListener( e -> mainFrame.processWindowClosing() ) ;
        
        JMenu fileMenu = new JMenu( "File" ) ;
        fileMenu.addSeparator() ;
        fileMenu.add( exitMenuItem ) ;
        return fileMenu ;
    }
    
    private JMenu getProjectMenu() {
        
        // Project menu items
        openProjectAction = action( "Open...", "open_project", cmdKey( KeyEvent.VK_O ),
                e -> mainFrame.openProject() ) ;
        
        reloadAISuggestionsAction = action( "Reload AI Suggestions", "reload_ai",
                cmdKey( KeyEvent.VK_R ),
                e -> mainFrame.reloadAISuggestionsForCurrentProject() ) ;

        autoAssocTopicsAction = action( "Auto Associate Topics", "auto_assoc_topics",
                cmdKey( KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::autoAssociateTopics ) ) ;
        autoAssocTopicsAction.setEnabled( false ) ;
        
        markAnsKeyAction = action( "Toggle Ans Marker [Active Page]", "toggle_ans_marker",
                cmdKey( KeyEvent.VK_K ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::toggleAnswerKeyMarkerForActivePage ) ) ;

        extractAnswersAction = action( "Extract Answers [Active Ans Key]", "extract_answers",
                cmdKey( KeyEvent.VK_E ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::extractAnswers ) ) ;

        closeTabAction = action( "Close Active Tab", "close_tab", cmdKey( KeyEvent.VK_W ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::closeActiveImageScraperTab ) ) ;

        closeProjectAction = action( "Close...", "close_project",
                cmdKey( KeyEvent.VK_W, InputEvent.SHIFT_DOWN_MASK ),
                e -> mainFrame.closeCurrentProject() ) ;
        
        JMenu projectMenu = new JMenu( "Project" ) ;
        projectMenu.add( openProjectAction ) ;
        projectMenu.add( closeProjectAction ) ;

        projectMenu.addSeparator() ;
        //------------------------------
        
        projectMenu.add( reloadAISuggestionsAction ) ;
        projectMenu.add( autoAssocTopicsAction ) ;
        projectMenu.add( markAnsKeyAction ) ;
        projectMenu.add( extractAnswersAction ) ;
        projectMenu.add( closeTabAction ) ;

        projectMenu.addSeparator() ;
        //------------------------------

        addUIChangeMenuItems( projectMenu ) ;
        
        projectMenu.addSeparator() ;
        addProjectStateChangeMenuItems( projectMenu ) ;
        
        return projectMenu;
    }
    
    private JMenu getViewMenu() {
        
        zoomFitPageAction = action( "Zoom to Fit Page [All Pages]", "zoom_fit",
                cmdKey( KeyEvent.VK_9 ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::zoomAllPagesToFitPage ) ) ;
        
        zoomDefaultAction = action( "Default Zoom [All Pages]", "zoom_default",
                cmdKey( KeyEvent.VK_0 ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::zoomAllPagesToDefault ) ) ;
        
        JMenu viewMenu = new JMenu( "View" ) ;
        viewMenu.add( zoomFitPageAction ) ;
        viewMenu.add( zoomDefaultAction ) ;
        return viewMenu ;
    }
    
    private void addUIChangeMenuItems( JMenu menu ) {
        
        imageScraperAction = action( "Open Question Scraper", "open_scraper",
                cmdKey( KeyEvent.VK_Q ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::activateQuestionScraperUI ) ) ;
        imageScraperAction.setEnabled( false ) ;
        
        ansMappingAction = action( "Open Answer Mapper", "open_ans_mapper",
                cmdKey( KeyEvent.VK_A ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::activateAnswerMapperUI ) ) ;
        
        topicMappingAction = action( "Open Topic Mapper", "open_topic_mapper",
                cmdKey( KeyEvent.VK_T ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::activateTopicMapperUI ) ) ;
        
        qSyncAction = action( "Open Question Sync", "open_qsync",
                cmdKey( KeyEvent.VK_S ),
                e -> getCurrentProjectPanel().ifPresent( ProjectPanel::activateQSyncUI ) ) ;
        
        menu.add( imageScraperAction ) ;
        menu.add( ansMappingAction ) ;
        menu.add( topicMappingAction ) ;
        menu.add( qSyncAction ) ;
    }
    
    private void addProjectStateChangeMenuItems( JMenu menu ) {
        
        imgCuttingCompleteAction = action( "Set Image Cutting Complete", "set_img_cutting_complete",
                cmdKey( KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK ),
                e -> getProjectState().ifPresent( ps -> ps.setImgCuttingComplete( true ) ) ) ;
        
        answersMappedAction = action( "Set Answers Mapped", "set_answers_mapped",
                cmdKey( KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK ),
                e -> getProjectState().ifPresent( ps -> ps.setAnswersMapped( true ) ) ) ;
        
        topicsMappedAction = action( "Set Topics Mapped", "set_topics_mapped",
                cmdKey( KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK ),
                e -> getProjectState().ifPresent( ps -> ps.setTopicsMapped( true ) ) ) ;
        
        qSyncCompleteAction = action( "Set Server Synced", "set_server_synced",
                cmdKey( KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK ),
                e -> getProjectState().ifPresent( ps -> ps.setQSyncedToServer( true ) ) ) ;
        
        menu.add( imgCuttingCompleteAction ) ;
        menu.add( answersMappedAction ) ;
        menu.add( topicsMappedAction ) ;
        menu.add( qSyncCompleteAction ) ;
    }
    
    // Creates an action with the menu text, accelerator and toolbar icon.
    // The icon is set as LARGE_ICON_KEY so that it shows up only on toolbar
    // buttons and not in the menu items.
    private static Action action( String name, String toolbarIconName,
                                  KeyStroke accelerator, ActionListener listener ) {
        Action action = new AbstractAction( name ) {
            @Override
            public void actionPerformed( ActionEvent e ) {
                listener.actionPerformed( e ) ;
            }
        } ;
        action.putValue( Action.LARGE_ICON_KEY, SwingUtils.getIcon( "tb_" + toolbarIconName ) ) ;
        if( accelerator != null ) {
            action.putValue( Action.ACCELERATOR_KEY, accelerator ) ;
        }
        return action ;
    }
    
    private static KeyStroke cmdKey( int keyCode ) {
        return cmdKey( keyCode, 0 ) ;
    }
    
    private static KeyStroke cmdKey( int keyCode, int extraModifiers ) {
        int menuMask = Toolkit.getDefaultToolkit().getMenuShortcutKeyMaskEx() ;
        return KeyStroke.getKeyStroke( keyCode, menuMask | extraModifiers ) ;
    }
    
    private Optional<ProjectState> getProjectState() {
        ProjectState val = null ;
        if( currentProjectPanel != null ) {
            val = currentProjectPanel.getProjectModel().getState() ;
        }
        return Optional.ofNullable( val ) ;
    }
    
    private Optional<ProjectPanel> getCurrentProjectPanel() {
        return Optional.ofNullable( currentProjectPanel ) ;
    }
    
    private JMenu getSyncMenu() {
        
        expandAllAction = action( "Expand All", "expand_all", null,
                e -> handleSyncAction( QSyncUI.AC_EXPAND_ALL ) ) ;
        
        collapseAllAction = action( "Collapse All", "collapse_all", null,
                e -> handleSyncAction( QSyncUI.AC_COLLAPSE_ALL ) ) ;
        
        expandSyllabusAction = action( "Expand Syllabus", "expand_syllabus", null,
                e -> handleSyncAction( QSyncUI.AC_EXPAND_SYLLABUS ) ) ;
        
        // SELECTED_KEY keeps the menu check box and the toolbar toggle
        // button in sync with each other.
        showOnlyUnsyncedAction = action( "Show only unsynced", "show_only_unsynced", null,
                e -> handleSyncAction( QSyncUI.AC_TOGGLE_SHOW_ONLY_UNSYNCHED ) ) ;
        showOnlyUnsyncedAction.putValue( Action.SELECTED_KEY, Boolean.FALSE ) ;
        
        syncAllPendingAction = action( "Sync All Pending", "sync_all_pending",
                cmdKey( KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK ),
                e -> handleSyncAction( QSyncUI.AC_SYNC_ALL_PENDING ) ) ;
        
        JMenu syncMenu = new JMenu( "Sync" ) ;
        syncMenu.add( expandAllAction ) ;
        syncMenu.add( collapseAllAction ) ;
        syncMenu.add( expandSyllabusAction ) ;
        syncMenu.addSeparator() ;
        syncMenu.add( new JCheckBoxMenuItem( showOnlyUnsyncedAction ) ) ;
        syncMenu.add( syncAllPendingAction ) ;
        
        return syncMenu;
    }
    
    private void handleSyncAction( String actionCmd ) {
        getCurrentProjectPanel().ifPresent( p -> p.handleMenuAction( actionCmd ) ) ;
    }
    
    // The sync actions are enabled only when the sync menu is enabled, so
    // that the toolbar buttons follow the menu's enabled state.
    private void setSyncActionsEnabled( boolean enabled ) {
        syncMenu.setEnabled( enabled ) ;
        expandAllAction.setEnabled( enabled ) ;
        collapseAllAction.setEnabled( enabled ) ;
        expandSyllabusAction.setEnabled( enabled ) ;
        showOnlyUnsyncedAction.setEnabled( enabled ) ;
        syncAllPendingAction.setEnabled( enabled ) ;
    }
    
    void setCurrentProjectPanel( ProjectPanel currentProjectPanel ) {
        this.currentProjectPanel = currentProjectPanel ;
        
        boolean enabled = currentProjectPanel != null ;
        
        setSyncActionsEnabled( false ) ;
        // A new project panel starts with the show-only-unsynced filter off
        showOnlyUnsyncedAction.putValue( Action.SELECTED_KEY, Boolean.FALSE ) ;
        
        imageScraperAction.setEnabled( false ) ;
        autoAssocTopicsAction.setEnabled( false ) ;
        closeProjectAction.setEnabled( enabled ) ;
        reloadAISuggestionsAction.setEnabled( enabled ) ;

        // When a project is set, the zoom items have already been enabled
        // by the ProjectPanel constructor via setEditorMode.
        if( !enabled ) {
            markAnsKeyAction.setEnabled( false ) ;
            closeTabAction.setEnabled( false ) ;
            extractAnswersAction.setEnabled( false ) ;
            zoomFitPageAction.setEnabled( false ) ;
            zoomDefaultAction.setEnabled( false ) ;
        }
        ansMappingAction.setEnabled( enabled ) ;
        topicMappingAction.setEnabled( enabled ) ;
        qSyncAction.setEnabled( enabled ) ;
        imgCuttingCompleteAction.setEnabled( enabled ) ;
        answersMappedAction.setEnabled( enabled ) ;
        topicsMappedAction.setEnabled( enabled ) ;
        qSyncCompleteAction.setEnabled( enabled ) ;
    }
    
    public void setEditorMode( ProjectPanel.EditorMode mode ) {
        
        imageScraperAction.setEnabled( false ) ;
        ansMappingAction.setEnabled( false ) ;
        topicMappingAction.setEnabled( false ) ;
        qSyncAction.setEnabled( false ) ;

        autoAssocTopicsAction.setEnabled( false ) ;
        markAnsKeyAction.setEnabled( false ) ;
        extractAnswersAction.setEnabled( false ) ;
        closeTabAction.setEnabled( false ) ;
        zoomFitPageAction.setEnabled( false ) ;
        zoomDefaultAction.setEnabled( false ) ;

        setSyncActionsEnabled( false ) ;

        switch( mode ) {
            case IMAGE_SCRAPER:
                ansMappingAction.setEnabled( true ) ;
                topicMappingAction.setEnabled( true ) ;
                markAnsKeyAction.setEnabled( true ) ;
                closeTabAction.setEnabled( true ) ;
                zoomFitPageAction.setEnabled( true ) ;
                zoomDefaultAction.setEnabled( true ) ;
                qSyncAction.setEnabled( true ) ;
                break ;
            case ANSWER_MAPPER:
                imageScraperAction.setEnabled( true ) ;
                extractAnswersAction.setEnabled( true ) ;
                topicMappingAction.setEnabled( true ) ;
                qSyncAction.setEnabled( true ) ;
                break ;
            case TOPIC_MAPPER:
                imageScraperAction.setEnabled( true ) ;
                ansMappingAction.setEnabled( true ) ;
                qSyncAction.setEnabled( true ) ;
                autoAssocTopicsAction.setEnabled( true ) ;
                break ;
            case QUESTION_SYNC:
                imageScraperAction.setEnabled( true ) ;
                ansMappingAction.setEnabled( true ) ;
                topicMappingAction.setEnabled( true ) ;
                setSyncActionsEnabled( true ) ;
        }
    }
}
