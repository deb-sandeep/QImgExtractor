package com.sandy.sconsole.qimgextractor;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.AppState;
import lombok.Getter;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.swing.*;
import java.io.File;

import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@SpringBootApplication
public class QImgExtractor
        implements ApplicationContextAware, WebMvcConfigurer {
    
    private static ConfigurableApplicationContext APP_CTX = null ;
    
    public static <T> T getBean( Class<T> requiredType ) {
        return APP_CTX.getBean( requiredType ) ;
    }
    
    public static ProjectModel getProjectModel() {
        return APP_CTX.getBean( ProjectModel.class ) ;
    }
    
    public static AppState getAppState() {
        return APP_CTX.getBean( QImgExtractor.class ).appState ;
    }
    
    // ---------------- Instance methods start ---------------------------------
    
    @Getter
    private final AppConfig appConfig ;
    
    private final MainFrame mainFrame ;
    
    private AppState appState ;
    
    public QImgExtractor( AppConfig appConfig, MainFrame mainFrame ) {
        this.appConfig = appConfig ;
        this.mainFrame = mainFrame ;
    }
    
    @Override
    public void setApplicationContext( @NonNull ApplicationContext context )
            throws BeansException {
        APP_CTX = ( ConfigurableApplicationContext )context;
    }
    
    private void initialize() {
        
        log.debug( "## Initializing QImgExtractor app. >" ) ;
        
        log.debug( "- Initializing AppState" ) ;
        this.loadAppState() ;
        
        log.debug( "- Initializing MainFrame" ) ;
        SwingUtilities.invokeLater( () -> {
            mainFrame.setVisible( true ) ;
            openLastOpenedProject() ;
        } ) ;
        
        log.debug( "<< ## QImgExtractor initialization complete" ) ;
    }
    
    private void openLastOpenedProject() {
        String lastOpenedProjectDirPath = appState.getLastOpenedProjectDir() ;
        if( lastOpenedProjectDirPath != null ) {
            File lastOpenedProjectDir = new File( lastOpenedProjectDirPath ) ;
            if( lastOpenedProjectDir.exists() ) {
                mainFrame.openProject( lastOpenedProjectDir ) ;
            }
        }
    }
    
    private void loadAppState() {
        File appStateFile = new File( appConfig.getAppWorkspaceDir(), "app-state.json" ) ;
        if( appStateFile.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                this.appState = mapper.readValue( appStateFile, AppState.class );
            }
            catch( Exception e ) {
                log.error( "Error loading app state", e );
                this.appState = new AppState();
            }
        }
        else {
            this.appState = new AppState() ;
        }
        this.appState.setApp( this ) ;
        this.appState.setInitialized( true ) ;
    }
    
    // --------------------- Main method ---------------------------------------
    public static void main( String[] args ) {
        
        log.debug( "Starting Spring Boot..." ) ;
        
        System.setProperty( "java.awt.headless", "false" ) ;
        SpringApplication.run( QImgExtractor.class, args ) ;
        
        log.debug( "Starting QImgExtractor.." ) ;
        QImgExtractor app = getBean( QImgExtractor.class ) ;
        try {
            app.initialize() ;
        }
        catch( Exception e ) {
            log.error( "Exception while initializing QImgExtractor.", e ) ;
            System.exit( -1 ) ;
        }
    }
}
