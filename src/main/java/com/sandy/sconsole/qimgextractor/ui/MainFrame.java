package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.MessageStatusComponent;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.StatusBar;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog.ProjectDirectoryView;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.UITheme;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.isValidProjectDir;

@Slf4j
@Component
public class MainFrame extends JFrame {
    
    private final AppConfig appConfig ;
    private final ProjectModel projectModel ;
    private final MenuBar menuBar ;
    
    private JFileChooser projectDirChooser ;
    
    @Getter
    private ProjectPanel currentProjectPanel ;
    
    private MessageStatusComponent messageSBComponent ;
    
    public MainFrame( AppConfig appConfig, ProjectModel projectModel ) {
        this.appConfig = appConfig ;
        this.projectModel = projectModel ;
        this.menuBar = new MenuBar( this ) ;
        
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                processWindowClosing() ;
            }
        } ) ;
    }
    
    @PostConstruct
    public void init() {
        //SwingUtils.setMaximized( this ) ;
        setBounds( 0, 0, 1400, SwingUtils.getScreenHeight() ) ;
        setJMenuBar( menuBar ) ;
        setUpProjectDirChooser() ;
        
        Container contentPane = getContentPane() ;
        contentPane.setLayout( new BorderLayout() ) ;
        contentPane.add( createStatusBar(), BorderLayout.SOUTH ) ;
    }
    
    private void setUpProjectDirChooser() {
        projectDirChooser = new JFileChooser() ;
        projectDirChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
        projectDirChooser.setCurrentDirectory( appConfig.getSourceBaseDir() ) ;
        projectDirChooser.setAcceptAllFileFilterUsed( false ) ;
        projectDirChooser.setMultiSelectionEnabled( false ) ;
        projectDirChooser.setFileView( new ProjectDirectoryView() ) ;
    }
    
    private StatusBar createStatusBar() {
        messageSBComponent = new MessageStatusComponent() ;
        messageSBComponent.setForeground( Color.DARK_GRAY ) ;
        messageSBComponent.setFont( UITheme.STATUS_FONT ) ;
        messageSBComponent.setBorder( null );
        
        StatusBar statusBar = new StatusBar() ;
        statusBar.addStatusBarComponent( messageSBComponent, StatusBar.Direction.EAST ) ;
        statusBar.initialize() ;
        return statusBar ;
    }
    
    void processWindowClosing() {
        System.exit( 0 ) ;
    }
    
    void openProject() {
    
        int userAction = projectDirChooser.showOpenDialog( this ) ;
        if( userAction == JFileChooser.APPROVE_OPTION ) {
            File projectDir = projectDirChooser.getSelectedFile() ;
            if( projectDir != null ) {
                openProject( projectDir ) ;
            }
        }
    }
    
    public void openProject( File projectDir ) {
        log.info( "## Opening project: {} ...", projectDir.getAbsolutePath() );
        if( isValidProjectDir( projectDir ) ) {
            closeCurrentProject() ;
            
            projectModel.initialize( projectDir ) ;
            currentProjectPanel = new ProjectPanel( this, projectModel ) ;
            menuBar.setCurrentProjectPanel( currentProjectPanel ) ;
            
            getContentPane().add( currentProjectPanel, BorderLayout.CENTER ) ;
            revalidate() ;
            repaint() ;
            
            QImgExtractor.getAppState().setLastOpenedProjectDir( projectDir ) ;
        }
        else {
            JOptionPane.showMessageDialog( this,
                    "Invalid project directory. The chosen directory " +
                    "should have a pages subdirectory containing image " +
                    "files for pages.",
                    "Error", JOptionPane.ERROR_MESSAGE ) ;
        }
    }
    
    void closeCurrentProject() {
        if( currentProjectPanel != null ) {
            getContentPane().remove( currentProjectPanel ) ;
            currentProjectPanel.destroy() ;
            currentProjectPanel = null ;
            menuBar.setCurrentProjectPanel( null ) ;
            revalidate() ;
            repaint() ;
        }
    }
    
    public void logStausMsg( String message ) {
        messageSBComponent.log( message ) ;
    }
    
    public void clearStatusMsg() {
        messageSBComponent.clear() ;
    }
}
