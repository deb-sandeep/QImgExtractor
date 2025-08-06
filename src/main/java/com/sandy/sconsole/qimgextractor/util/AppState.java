package com.sandy.sconsole.qimgextractor.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.Serializable;

@Slf4j
public class AppState implements Serializable {

    @JsonIgnore
    @Setter
    private transient File persistenceFile ;
    
    @JsonIgnore
    @Setter
    private transient boolean isInitialized = false ;
    
    @Getter
    private String lastOpenedProjectDir ;
    
    public void setLastOpenedProjectDir( File projectDir ) {
        this.lastOpenedProjectDir = projectDir.getAbsolutePath() ;
        saveAppState() ;
    }
    
    private void saveAppState() {
        if( isInitialized ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable( SerializationFeature.INDENT_OUTPUT ) ;
                mapper.writeValue( persistenceFile, this );
            }
            catch( Exception e ) {
                log.error( "Error saving app state", e );
            }
        }
    }
}
