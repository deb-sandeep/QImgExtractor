package com.sandy.sconsole.qimgextractor;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.swing.*;

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
    
    // ---------------- Instance methods start ---------------------------------
    
    private final MainFrame mainFrame ;
    
    public QImgExtractor( MainFrame mainFrame ) {
        this.mainFrame = mainFrame ;
    }
    
    @Override
    public void setApplicationContext( ApplicationContext ctx )
            throws BeansException {
        APP_CTX = ( ConfigurableApplicationContext )ctx;
    }
    
    public void initialize() {
        
        log.debug( "## Initializing QImgExtractor app. >" ) ;
        
        log.debug( "- Initializing MainFrame" ) ;
        SwingUtilities.invokeLater( () -> mainFrame.setVisible( true ) ) ;
        
        log.debug( "<< ## QImgExtractor initialization complete" ) ;
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
