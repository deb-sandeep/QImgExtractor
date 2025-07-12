package com.sandy.sconsole.qimgextractor.ui;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

@Slf4j
@Component
public class MainFrame extends JFrame {
    
    @Autowired
    private AppConfig appConfig ;
    
    JFileChooser projectDirChooser ;
    ProjectPanel currentProjectPanel ;

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
        getContentPane().setLayout( new BorderLayout() );
        setUpProjectDirChooser() ;
    }
    
    private void setUpProjectDirChooser() {
        projectDirChooser = new JFileChooser() ;
        projectDirChooser.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY ) ;
        projectDirChooser.setCurrentDirectory( appConfig.getSourceBaseDir() ) ;
        projectDirChooser.setAcceptAllFileFilterUsed( false ) ;
        projectDirChooser.setMultiSelectionEnabled( false ) ;
    }
    
    private JMenuBar createMenuBar() {
        JMenuBar menuBar = new JMenuBar() ;
        menuBar.add( getFileMenu() ) ;
        return menuBar ;
    }
    
    private JMenu getFileMenu() {
        JMenuItem openMenuItem = new JMenuItem( "Open..." ) ;
        openMenuItem.addActionListener( e -> openProject() ) ;
        
        JMenu fileMenu = new JMenu( "File" ) ;
        fileMenu.add( openMenuItem ) ;
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
                    if( currentProjectPanel != null ) {
                        getContentPane().remove( currentProjectPanel ) ;
                        currentProjectPanel.destroy() ;
                    }
                    currentProjectPanel = new ProjectPanel( projectDir ) ;
                    getContentPane().add( currentProjectPanel, BorderLayout.CENTER ) ;
                    getContentPane().revalidate() ;
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
    
    private boolean isValidProjectDir( File projectDir ) {
        File pagesDir = new File( projectDir, "pages" ) ;
        if( pagesDir.exists() ) {
            File[] files = pagesDir.listFiles( f -> f.getName().endsWith( ".png" ) ) ;
            return null != files && files.length > 0 ;
        }
        return false ;
    }
}
