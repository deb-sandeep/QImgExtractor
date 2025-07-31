package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.MessageStatusComponent;
import com.sandy.sconsole.qimgextractor.ui.core.statusbar.StatusBar;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.UITheme;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

@Slf4j
@Component
public class MainFrame extends JFrame {
    
    @Autowired
    private AppConfig appConfig ;
    
    @Autowired
    private ProjectModel projectModel ;
    
    private JFileChooser projectDirChooser ;
    private ProjectPanel currentProjectPanel ;
    
    private MessageStatusComponent projectNameSBComponent ;
    private MessageStatusComponent messageSBComponent ;
    
    public MainFrame() {
        addWindowListener( new WindowAdapter() {
            public void windowClosing( WindowEvent e ) {
                processWindowClosing() ;
            }
        } ) ;
    }
    
    @PostConstruct
    public void init() {
        SwingUtils.setMaximized( this ) ;
        setJMenuBar( createMenuBar() ) ;
        setUpProjectDirChooser() ;
        
        Container contentPane = getContentPane() ;
        contentPane.setLayout( new BorderLayout() ) ;
        contentPane.add( createStatusBar(), BorderLayout.NORTH ) ;
    }
    
    private void setUpProjectDirChooser() {
        projectDirChooser = new JFileChooser() ;
        projectDirChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
        projectDirChooser.setCurrentDirectory( appConfig.getSourceBaseDir() ) ;
        projectDirChooser.setAcceptAllFileFilterUsed( false ) ;
        projectDirChooser.setMultiSelectionEnabled( false ) ;
    }
    
    private StatusBar createStatusBar() {
        
        projectNameSBComponent = new MessageStatusComponent() ;
        projectNameSBComponent.setBorder( BorderFactory.createBevelBorder( BevelBorder.RAISED ) );
        projectNameSBComponent.setForeground( Color.BLUE ) ;
        projectNameSBComponent.setFont( UITheme.STATUS_FONT );
        projectNameSBComponent.log( "Choose project directory" ) ;
        
        messageSBComponent = new MessageStatusComponent() ;
        messageSBComponent.setForeground( Color.DARK_GRAY ) ;
        messageSBComponent.setFont( UITheme.STATUS_FONT ) ;
        messageSBComponent.setBorder( null );
        
        StatusBar statusBar = new StatusBar() ;
        statusBar.addStatusBarComponent( projectNameSBComponent, StatusBar.Direction.WEST ) ;
        statusBar.addStatusBarComponent( messageSBComponent, StatusBar.Direction.EAST ) ;
        statusBar.initialize() ;
        return statusBar ;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar() ;
        menuBar.add( getFileMenu() ) ;
        return menuBar ;
    }
    
    private JMenu getFileMenu() {
        JMenuItem openMenuItem = new JMenuItem( "Open..." ) ;
        openMenuItem.addActionListener( e -> openProject() ) ;
        
        JMenuItem closeMenuItem = new JMenuItem( "Close..." ) ;
        closeMenuItem.addActionListener( e -> closeCurrentProject() ) ;
        
        JMenuItem exitMenuItem = new JMenuItem( "Exit" ) ;
        exitMenuItem.addActionListener( e -> processWindowClosing() ) ;
        
        JMenu fileMenu = new JMenu( "File" ) ;
        fileMenu.add( openMenuItem ) ;
        fileMenu.add( closeMenuItem ) ;
        fileMenu.addSeparator() ;
        fileMenu.add( exitMenuItem ) ;
        return fileMenu ;
    }
    
    private void processWindowClosing() {
        System.exit( 0 ) ;
    }
    
    private void openProject() {
    
        int userAction = projectDirChooser.showOpenDialog( this ) ;
        if( userAction == JFileChooser.APPROVE_OPTION ) {
            File projectDir = projectDirChooser.getSelectedFile() ;
            if( projectDir != null ) {
                if( isValidProjectDir( projectDir ) ) {
                    closeCurrentProject() ;
                    
                    projectModel.initialize( projectDir ) ;
                    
                    currentProjectPanel = new ProjectPanel( this, projectModel ) ;
                    
                    getContentPane().add( currentProjectPanel, BorderLayout.CENTER ) ;
                    revalidate() ;
                    repaint() ;
                    
                    projectNameSBComponent.log( currentProjectPanel.getSrcId() ) ;
                }
                else {
                    JOptionPane.showMessageDialog( this,
                        "Invalid project directory. The chosen directory " +
                        "should have a pages subdirectory containing image " +
                        "files for pages.",
                        "Error", JOptionPane.ERROR_MESSAGE ) ;
                }
            }
        }
    }
    
    private void closeCurrentProject() {
        if( currentProjectPanel != null ) {
            getContentPane().remove( currentProjectPanel ) ;
            currentProjectPanel.destroy() ;
            currentProjectPanel = null ;
            projectNameSBComponent.log( "Choose project directory" ) ;
            revalidate() ;
            repaint() ;
        }
    }
    
    private boolean isValidProjectDir( File projectDir ) {
        File pagesDir = new File( projectDir, "pages" ) ;
        if( pagesDir.exists() ) {
            File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
            return null != files && files.length > 0 ;
        }
        return false ;
    }
    
    public void logStausMsg( String message ) {
        messageSBComponent.log( message ) ;
    }
    
    public void clearStatusMsg() {
        messageSBComponent.clear() ;
    }
}
