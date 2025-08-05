package com.sandy.sconsole.qimgextractor.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandy.sconsole.qimgextractor.QImgExtractor;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Serializable;

@Slf4j
public class AppState implements Serializable {

    @JsonIgnore
    @Setter
    private QImgExtractor app ;
    
    @Getter
    private String lastOpenedProjectDir ;
    
    public void setLastOpenedProjectDir( File projectDir ) {
        this.lastOpenedProjectDir = projectDir.getAbsolutePath() ;
        saveAppState() ;
    }
    
    private void saveAppState() {
        try {
            File stateFile = new File( app.getAppConfig().getAppWorkspaceDir(), "app-state.json" ) ;
            ObjectMapper mapper = new ObjectMapper();
            mapper.writeValue( stateFile, this );
        }
        catch( Exception e ) {
            log.error( "Error saving app state", e );
        }
    }
}
