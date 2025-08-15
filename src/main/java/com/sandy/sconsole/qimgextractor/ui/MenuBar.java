package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.ProjectState;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.util.Optional;

@Slf4j
public class MenuBar extends JMenuBar {
    
    private final MainFrame mainFrame ;
    
    private ProjectPanel currentProjectPanel ;
    
    private JMenuItem closeMenuItem;
    private JMenuItem imageScrapersMI;
    private JMenuItem ansMappingUI;
    private JMenuItem imgCuttingCompleteMI;
    private JMenuItem answersMappedMI;
    private JMenuItem topicsMappedMI;
    private JMenuItem serverSyncedMI;
    
    MenuBar( MainFrame mainFrame ) {
        this.mainFrame = mainFrame ;
        super.add( getFileMenu() ) ;
        super.add( getProjectMenu() ) ;
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
        
        closeMenuItem = new JMenuItem( "Close..." );
        closeMenuItem.addActionListener( e -> mainFrame.closeCurrentProject() );
        
        JMenu projectMenu = new JMenu( "Project" ) ;
        projectMenu.add( openMenuItem ) ;
        projectMenu.add( closeMenuItem ) ;
        
        projectMenu.addSeparator() ;
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
        
        ansMappingUI = new JMenuItem( "Open Answer Mapper" );
        ansMappingUI.addActionListener( e ->
                getCurrentProjectPanel().ifPresent( ProjectPanel::activateAnswerMapperUI ) ) ;
        
        menu.add( imageScrapersMI ) ;
        menu.add( ansMappingUI ) ;
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
        
        serverSyncedMI = new JMenuItem( "Set Server Synced" );
        serverSyncedMI.addActionListener( e ->
                getProjectState().ifPresent( ps -> ps.setSavedToServer( true ) ) ) ;
        
        menu.add( imgCuttingCompleteMI );
        menu.add( answersMappedMI );
        menu.add( topicsMappedMI );
        menu.add( serverSyncedMI );
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
    
    void setCurrentProjectPanel( ProjectPanel currentProjectPanel ) {
        this.currentProjectPanel = currentProjectPanel ;
        
        boolean enabled = currentProjectPanel != null ;
        
        imageScrapersMI.setEnabled( false ) ;
        closeMenuItem.setEnabled( enabled ) ;
        ansMappingUI.setEnabled( enabled ) ;
        imgCuttingCompleteMI.setEnabled( enabled ) ;
        answersMappedMI.setEnabled( enabled ) ;
        topicsMappedMI.setEnabled( enabled ) ;
        serverSyncedMI.setEnabled( enabled ) ;
    }
    
    public void setEditorMode( ProjectPanel.EditorMode mode ) {
        
        imageScrapersMI.setEnabled( false ) ;
        ansMappingUI.setEnabled( false ) ;
        
        switch( mode ) {
            case IMAGE_SCRAPER:
                ansMappingUI.setEnabled( true ) ;
                break ;
            case ANSWER_MAPPER:
                imageScrapersMI.setEnabled( true ) ;
                break ;
        }
    }
}
