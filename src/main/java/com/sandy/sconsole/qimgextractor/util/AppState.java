package com.sandy.sconsole.qimgextractor.util;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

@Slf4j
public class AppState implements Serializable {

    @JsonIgnore
    @Setter
    private transient File stateFile ;
    
    @Getter
    private String lastOpenedProjectDir ;
    
    @Getter
    private final ArrayList<Integer> verticalMarkers = new ArrayList<>() ;
    
    // This constructor is used by Jackson for temporary deserialization
    // and should not be called by application logic as it does not have a
    // backing file.
    public AppState() {
        this.stateFile = null ;
    }
    
    public AppState( AppConfig appConfig ) {
        this.stateFile = new File( appConfig.getAppWorkspaceDir(), "app-state.json" ) ;
        loadState() ;
    }
    
    public void setLastOpenedProjectDir( File projectDir ) {
        this.lastOpenedProjectDir = projectDir.getAbsolutePath() ;
        saveState() ;
    }
    
    public void clearVerticalMarkers() {
        this.verticalMarkers.clear() ;
        saveState() ;
    }
    
    public void addVerticalMarker( int markerPos ) {
        this.verticalMarkers.add( markerPos ) ;
        saveState() ;
    }
    
    private void loadState() {
        if( stateFile.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                AppState loadedState = mapper.readValue( stateFile, AppState.class );
                this.lastOpenedProjectDir = loadedState.lastOpenedProjectDir ;
                this.verticalMarkers.clear() ;
                this.verticalMarkers.addAll( loadedState.verticalMarkers ) ;
            }
            catch( IOException e ) {
                throw new RuntimeException( "Error loading app state", e );
            }
        }
    }
    
    public void saveState() {
        try {
            if( stateFile != null ) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.enable( SerializationFeature.INDENT_OUTPUT ) ;
                mapper.writeValue( stateFile, this );
            }
        }
        catch( IOException e ) {
            throw new RuntimeException( "Error saving app state", e );
        }
    }
}
