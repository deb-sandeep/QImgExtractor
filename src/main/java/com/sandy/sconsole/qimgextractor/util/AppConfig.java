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
    
    // Number of threads which sync non LCT questions in parallel. LCT
    // questions are always synced one after the other on a thread of their own.
    @Value( "${qSyncThreads:4}" )
    private int qSyncThreads = 4 ;
    
    @Value( "true" )
    private boolean hideSyncedSrcFolders = true ;

    @Value( "${claudeExecutable:${user.home}/.local/bin/claude}" )
    private String claudeExecutable ;

    @Value( "${answerExtractorWorkDir:${user.home}/Documents/StudyNotes/question-bank}" )
    private File answerExtractorWorkDir ;
    
    public boolean isDevProfile() {
        return profile.equals( "dev" ) ;
    }
}
