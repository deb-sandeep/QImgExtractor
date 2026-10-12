package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Optional;

@Slf4j
public class MenuBar extends JMenuBar {
    
    private final MainFrame mainFrame ;
    
    private ProjectPanel currentProjectPanel ;
    
    private JMenuItem closeMenuItem;
    private JMenuItem closeTabMI;
    private JMenuItem reloadAISuggestionsMI;
    private JMenuItem autoAssocTopicsMI;
    private JMenuItem imageScrapersMI;
    private JMenuItem ansMappingMI;
    private JMenuItem topicMappingMI;
    private JMenuItem qSyncMI;
    
    private JMenuItem imgCuttingCompleteMI;
    private JMenuItem answersMappedMI;
    private JMenuItem topicsMappedMI;
    private JMenuItem qSyncCompleteMI;
    
    private JMenuItem markAnsKeyMI;
    private JMenuItem extractAnswersMI;
    
    private JMenuItem zoomFitPageMI;
    private JMenuItem zoomDefaultMI;
    
    private final JMenu syncMenu ;
            
    MenuBar( MainFrame mainFrame ) {
        this.mainFrame = mainFrame ;
        this.syncMenu = getSyncMenu() ;
        this.syncMenu.setEnabled( false ) ;
        
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
        JMenuItem openMenuItem = new JMenuItem( "Open..." );
        openMenuItem.setAccelerator( cmdKey( KeyEvent.VK_O ) ) ;
        openMenuItem.addActionListener( e -> mainFrame.openProject() );
        
        reloadAISuggestionsMI = new JMenuItem( "Reload AI Suggestions" ) ;
        reloadAISuggestionsMI.setAccelerator( cmdKey( KeyEvent.VK_R ) ) ;
        reloadAISuggestionsMI.addActionListener( e ->
                mainFrame.reloadAISuggestionsForCurrentProject() ) ;

        autoAssocTopicsMI = new JMenuItem( "Auto Associate Topics" ) ;
        autoAssocTopicsMI.setAccelerator( cmdKey( KeyEvent.VK_T, InputEvent.SHIFT_DOWN_MASK ) ) ;
        autoAssocTopicsMI.setEnabled( false ) ;
        autoAssocTopicsMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::autoAssociateTopics ) ) ;
        
        markAnsKeyMI = new JMenuItem( "Toggle Ans Marker [Active Page]" ) ;
        markAnsKeyMI.setAccelerator( cmdKey( KeyEvent.VK_K ) ) ;
        markAnsKeyMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::toggleAnswerKeyMarkerForActivePage ) ) ;

        extractAnswersMI = new JMenuItem( "Extract Answers [Active Ans Key]" ) ;
        extractAnswersMI.setAccelerator( cmdKey( KeyEvent.VK_E ) ) ;
        extractAnswersMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::extractAnswers ) ) ;

        closeTabMI = new JMenuItem( "Close Active Tab" ) ;
        closeTabMI.setAccelerator( cmdKey( KeyEvent.VK_W ) ) ;
        closeTabMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::closeActiveImageScraperTab ) ) ;

        closeMenuItem = new JMenuItem( "Close..." );
        closeMenuItem.setAccelerator( cmdKey( KeyEvent.VK_W, InputEvent.SHIFT_DOWN_MASK ) ) ;
        closeMenuItem.addActionListener( e -> mainFrame.closeCurrentProject() );
        
        JMenu projectMenu = new JMenu( "Project" ) ;
        projectMenu.add( openMenuItem ) ;
        projectMenu.add( closeMenuItem ) ;

        projectMenu.addSeparator() ;
        //------------------------------
        
        projectMenu.add( reloadAISuggestionsMI ) ;
        projectMenu.add( autoAssocTopicsMI ) ;
        projectMenu.add( markAnsKeyMI ) ;
        projectMenu.add( extractAnswersMI ) ;
        projectMenu.add( closeTabMI ) ;

        projectMenu.addSeparator() ;
        //------------------------------

        addUIChangeMenuItems( projectMenu ) ;
        
        projectMenu.addSeparator() ;
        addProjectStateChangeMenuItems( projectMenu ) ;
        
        return projectMenu;
    }
    
    private JMenu getViewMenu() {
        
        zoomFitPageMI = new JMenuItem( "Zoom to Fit Page [All Pages]" ) ;
        zoomFitPageMI.setAccelerator( cmdKey( KeyEvent.VK_9 ) ) ;
        zoomFitPageMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::zoomAllPagesToFitPage ) ) ;
        
        zoomDefaultMI = new JMenuItem( "Default Zoom [All Pages]" ) ;
        zoomDefaultMI.setAccelerator( cmdKey( KeyEvent.VK_0 ) ) ;
        zoomDefaultMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::zoomAllPagesToDefault ) ) ;
        
        JMenu viewMenu = new JMenu( "View" ) ;
        viewMenu.add( zoomFitPageMI ) ;
        viewMenu.add( zoomDefaultMI ) ;
        return viewMenu ;
    }
    
    private void addUIChangeMenuItems( JMenu menu ) {
        
        imageScrapersMI = new JMenuItem( "Open Question Scraper" );
        imageScrapersMI.setEnabled( false ) ;
        imageScrapersMI.setAccelerator( cmdKey( KeyEvent.VK_Q ) ) ;
        imageScrapersMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateQuestionScraperUI ) );
        
        ansMappingMI = new JMenuItem( "Open Answer Mapper" );
        ansMappingMI.setAccelerator( cmdKey( KeyEvent.VK_A ) ) ;
        ansMappingMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateAnswerMapperUI ) ) ;
        
        
        topicMappingMI = new JMenuItem( "Open Topic Mapper" );
        topicMappingMI.setAccelerator( cmdKey( KeyEvent.VK_T ) ) ;
        topicMappingMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateTopicMapperUI ) ) ;
        
        qSyncMI = new JMenuItem( "Open Question Sync" );
        qSyncMI.setAccelerator( cmdKey( KeyEvent.VK_S ) ) ;
        qSyncMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateQSyncUI ) ) ;
        
        menu.add( imageScrapersMI ) ;
        menu.add( ansMappingMI ) ;
        menu.add( topicMappingMI ) ;
        menu.add( qSyncMI ) ;
    }
    
    private void addProjectStateChangeMenuItems( JMenu menu ) {
        
        imgCuttingCompleteMI = new JMenuItem( "Set Image Cutting Complete" );
        imgCuttingCompleteMI.setAccelerator( cmdKey( KeyEvent.VK_Q, InputEvent.ALT_DOWN_MASK ) ) ;
        imgCuttingCompleteMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setImgCuttingComplete( true ) ) );
        
        answersMappedMI = new JMenuItem( "Set Answers Mapped" );
        answersMappedMI.setAccelerator( cmdKey( KeyEvent.VK_A, InputEvent.ALT_DOWN_MASK ) ) ;
        answersMappedMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setAnswersMapped( true ) ) );
        
        topicsMappedMI = new JMenuItem( "Set Topics Mapped" );
        topicsMappedMI.setAccelerator( cmdKey( KeyEvent.VK_T, InputEvent.ALT_DOWN_MASK ) ) ;
        topicsMappedMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setTopicsMapped( true ) ) );
        
        qSyncCompleteMI = new JMenuItem( "Set Server Synced" );
        qSyncCompleteMI.setAccelerator( cmdKey( KeyEvent.VK_S, InputEvent.ALT_DOWN_MASK ) ) ;
        qSyncCompleteMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setQSyncedToServer( true ) ) ) ;
        
        menu.add( imgCuttingCompleteMI );
        menu.add( answersMappedMI );
        menu.add( topicsMappedMI );
        menu.add( qSyncCompleteMI );
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
        
        JMenuItem expandAllMI = new JMenuItem( "Expand All" ) ;
        expandAllMI.addActionListener( e ->
                mainFrame.getCurrentProjectPanel()
                        .handleMenuAction( QSyncUI.AC_EXPAND_ALL ) ) ;
        
        JMenuItem collapseAllMI = new JMenuItem( "Collapse All" ) ;
        collapseAllMI.addActionListener( e ->
                mainFrame.getCurrentProjectPanel()
                        .handleMenuAction( QSyncUI.AC_COLLAPSE_ALL ) ) ;
        
        JMenuItem expandSyllabusMI = new JMenuItem( "Expand Syllabus" ) ;
        expandSyllabusMI.addActionListener( e ->
                mainFrame.getCurrentProjectPanel()
                        .handleMenuAction( QSyncUI.AC_EXPAND_SYLLABUS ) ) ;
        
        JCheckBoxMenuItem showOnlyUnsyncedMI = new JCheckBoxMenuItem( "Show only unsynced", false ) ;
        showOnlyUnsyncedMI.addActionListener( e ->
                mainFrame.getCurrentProjectPanel()
                         .handleMenuAction( QSyncUI.AC_TOGGLE_SHOW_ONLY_UNSYNCHED ) ) ;
        
        JMenuItem syncAllPendingMI = new JMenuItem( "Sync All Pending" ) ;
        syncAllPendingMI.setAccelerator( cmdKey( KeyEvent.VK_S, InputEvent.SHIFT_DOWN_MASK ) ) ;
        syncAllPendingMI.addActionListener( e ->
                mainFrame.getCurrentProjectPanel()
                         .handleMenuAction( QSyncUI.AC_SYNC_ALL_PENDING ) ) ;
        
        JMenu syncMenu = new JMenu( "Sync" ) ;
        syncMenu.add( expandAllMI ) ;
        syncMenu.add( collapseAllMI ) ;
        syncMenu.add( expandSyllabusMI ) ;
        syncMenu.addSeparator() ;
        syncMenu.add( showOnlyUnsyncedMI ) ;
        syncMenu.add( syncAllPendingMI ) ;
        
        return syncMenu;
    }
    
    void setCurrentProjectPanel( ProjectPanel currentProjectPanel ) {
        this.currentProjectPanel = currentProjectPanel ;
        
        boolean enabled = currentProjectPanel != null ;
        
        syncMenu.setEnabled( false ) ;
        imageScrapersMI.setEnabled( false ) ;
        autoAssocTopicsMI.setEnabled( false ) ;
        closeMenuItem.setEnabled( enabled ) ;

        // When a project is set, the zoom items have already been enabled
        // by the ProjectPanel constructor via setEditorMode.
        if( !enabled ) {
            extractAnswersMI.setEnabled( false ) ;
            zoomFitPageMI.setEnabled( false ) ;
            zoomDefaultMI.setEnabled( false ) ;
        }
        ansMappingMI.setEnabled( enabled ) ;
        topicMappingMI.setEnabled( enabled ) ;
        qSyncMI.setEnabled( enabled ) ;
        imgCuttingCompleteMI.setEnabled( enabled ) ;
        answersMappedMI.setEnabled( enabled ) ;
        topicsMappedMI.setEnabled( enabled ) ;
        qSyncCompleteMI.setEnabled( enabled ) ;
    }
    
    public void setEditorMode( ProjectPanel.EditorMode mode ) {
        
        imageScrapersMI.setEnabled( false ) ;
        ansMappingMI.setEnabled( false ) ;
        topicMappingMI.setEnabled( false ) ;
        qSyncMI.setEnabled( false ) ;

        autoAssocTopicsMI.setEnabled( false ) ;
        markAnsKeyMI.setEnabled( false ) ;
        extractAnswersMI.setEnabled( false ) ;
        closeTabMI.setEnabled( false ) ;
        zoomFitPageMI.setEnabled( false ) ;
        zoomDefaultMI.setEnabled( false ) ;

        syncMenu.setEnabled( false ) ;

        switch( mode ) {
            case IMAGE_SCRAPER:
                ansMappingMI.setEnabled( true ) ;
                topicMappingMI.setEnabled( true ) ;
                markAnsKeyMI.setEnabled( true ) ;
                closeTabMI.setEnabled( true ) ;
                zoomFitPageMI.setEnabled( true ) ;
                zoomDefaultMI.setEnabled( true ) ;
                qSyncMI.setEnabled( true ) ;
                break ;
            case ANSWER_MAPPER:
                imageScrapersMI.setEnabled( true ) ;
                extractAnswersMI.setEnabled( true ) ;
                topicMappingMI.setEnabled( true ) ;
                qSyncMI.setEnabled( true ) ;
                break ;
            case TOPIC_MAPPER:
                imageScrapersMI.setEnabled( true ) ;
                ansMappingMI.setEnabled( true ) ;
                qSyncMI.setEnabled( true ) ;
                autoAssocTopicsMI.setEnabled( true ) ;
                break ;
            case QUESTION_SYNC:
                imageScrapersMI.setEnabled( true ) ;
                ansMappingMI.setEnabled( true ) ;
                topicMappingMI.setEnabled( true ) ;
                syncMenu.setEnabled( true ) ;
        }
    }
}
