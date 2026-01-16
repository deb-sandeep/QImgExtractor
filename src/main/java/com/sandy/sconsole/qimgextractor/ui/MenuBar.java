package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Optional;

@Slf4j
public class MenuBar extends JMenuBar {
    
    private final MainFrame mainFrame ;
    
    private ProjectPanel currentProjectPanel ;
    
    private JMenuItem closeMenuItem;
    private JMenuItem imageScrapersMI;
    private JMenuItem ansMappingMI;
    private JMenuItem topicMappingMI;
    private JMenuItem qSyncMI;
    
    private JMenuItem imgCuttingCompleteMI;
    private JMenuItem answersMappedMI;
    private JMenuItem topicsMappedMI;
    private JMenuItem qSyncCompleteMI;
    
    private JMenuItem markAnsKeyMI;
    
    private final JMenu syncMenu ;
            
    MenuBar( MainFrame mainFrame ) {
        this.mainFrame = mainFrame ;
        this.syncMenu = getSyncMenu() ;
        this.syncMenu.setEnabled( false ) ;
        
        super.add( getFileMenu() ) ;
        super.add( getProjectMenu() ) ;
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
        openMenuItem.addActionListener( e -> mainFrame.openProject() );
        
        markAnsKeyMI = new JMenuItem( "Toggle Ans Marker [Active Page]" ) ;
        markAnsKeyMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::toggleAnswerKeyMarkerForActivePage ) ) ;

        closeMenuItem = new JMenuItem( "Close..." );
        closeMenuItem.addActionListener( e -> mainFrame.closeCurrentProject() );
        
        JMenu projectMenu = new JMenu( "Project" ) ;
        projectMenu.add( openMenuItem ) ;
        projectMenu.add( closeMenuItem ) ;

        projectMenu.addSeparator() ;
        //------------------------------
        
        projectMenu.add( markAnsKeyMI ) ;
        
        projectMenu.addSeparator() ;
        //------------------------------

        addUIChangeMenuItems( projectMenu ) ;
        
        projectMenu.addSeparator() ;
        addProjectStateChangeMenuItems( projectMenu ) ;
        
        return projectMenu;
    }
    
    private void addUIChangeMenuItems( JMenu menu ) {
        
        imageScrapersMI = new JMenuItem( "Open Question Scraper" );
        imageScrapersMI.setEnabled( false ) ;
        imageScrapersMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateQuestionScraperUI ) );
        
        ansMappingMI = new JMenuItem( "Open Answer Mapper" );
        ansMappingMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateAnswerMapperUI ) ) ;
        
        
        topicMappingMI = new JMenuItem( "Open Topic Mapper" );
        topicMappingMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateTopicMapperUI ) ) ;
        
        qSyncMI = new JMenuItem( "Open Question Sync" );
        qSyncMI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateQSyncUI ) ) ;
        
        menu.add( imageScrapersMI ) ;
        menu.add( ansMappingMI ) ;
        menu.add( topicMappingMI ) ;
        menu.add( qSyncMI ) ;
    }
    
    private void addProjectStateChangeMenuItems( JMenu menu ) {
        
        imgCuttingCompleteMI = new JMenuItem( "Set Image Cutting Complete" );
        imgCuttingCompleteMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setImgCuttingComplete( true ) ) );
        
        answersMappedMI = new JMenuItem( "Set Answers Mapped" );
        answersMappedMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setAnswersMapped( true ) ) );
        
        topicsMappedMI = new JMenuItem( "Set Topics Mapped" );
        topicsMappedMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setTopicsMapped( true ) ) );
        
        qSyncCompleteMI = new JMenuItem( "Set Server Synced" );
        qSyncCompleteMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setQSyncedToServer( true ) ) ) ;
        
        menu.add( imgCuttingCompleteMI );
        menu.add( answersMappedMI );
        menu.add( topicsMappedMI );
        menu.add( qSyncCompleteMI );
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
        
        JMenu syncMenu = new JMenu( "Sync" ) ;
        syncMenu.add( expandAllMI ) ;
        syncMenu.add( collapseAllMI ) ;
        syncMenu.add( expandSyllabusMI ) ;
        syncMenu.addSeparator() ;
        syncMenu.add( showOnlyUnsyncedMI ) ;
        
        return syncMenu;
    }
    
    void setCurrentProjectPanel( ProjectPanel currentProjectPanel ) {
        this.currentProjectPanel = currentProjectPanel ;
        
        boolean enabled = currentProjectPanel != null ;
        
        syncMenu.setEnabled( false ) ;
        imageScrapersMI.setEnabled( false ) ;
        closeMenuItem.setEnabled( enabled ) ;
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
        
        markAnsKeyMI.setEnabled( false ) ;
        
        syncMenu.setEnabled( false ) ;
        
        switch( mode ) {
            case IMAGE_SCRAPER:
                ansMappingMI.setEnabled( true ) ;
                topicMappingMI.setEnabled( true ) ;
                markAnsKeyMI.setEnabled( true ) ;
                qSyncMI.setEnabled( true ) ;
                break ;
            case ANSWER_MAPPER:
                imageScrapersMI.setEnabled( true ) ;
                topicMappingMI.setEnabled( true ) ;
                qSyncMI.setEnabled( true ) ;
                break ;
            case TOPIC_MAPPER:
                imageScrapersMI.setEnabled( true ) ;
                ansMappingMI.setEnabled( true ) ;
                qSyncMI.setEnabled( true ) ;
                break ;
            case QUESTION_SYNC:
                imageScrapersMI.setEnabled( true ) ;
                ansMappingMI.setEnabled( true ) ;
                topicMappingMI.setEnabled( true ) ;
                syncMenu.setEnabled( true ) ;
        }
    }
}
