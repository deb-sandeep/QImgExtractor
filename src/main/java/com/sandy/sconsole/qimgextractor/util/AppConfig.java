package com.sandy.sconsole.qimgextractor.util;

import java.io.File ;

import lombok.Data ;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Data
@Component
public class AppConfig {
    
    @Value( "${spring.profiles.active:dev}" )
    private String profile ;
    
    @Value( "${appWorkspaceDir}" )
    private File appWorkspaceDir ;

    @Value( "${sourceBaseDir}" )
    private File sourceBaseDir ;
    
    @Value( "${repairProjectOnStartup}" )
    private boolean repairProjectOnStartup = false ;
    
    @Value( "${serverAddress}" )
    private String serverAddress ;
    
    @Value( "${qSyncAPIEndpoint}" )
    private String qSyncAPIEndpoint ;
    
    public boolean isDevProfile() {
        return profile.equals( "dev" ) ;
    }
}
