package com.sandy.sconsole.qimgextractor.ui.project.model.state;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import lombok.Getter;

import java.io.File;
import java.io.IOException;

@Getter
public class ProjectState {

    @JsonIgnore
    private final File stateFile ;
    
    private boolean imgCuttingWip = false ;
    private boolean imgCuttingComplete = false ;
    private boolean answersMapped = false ;
    private boolean topicsMapped = false ;
    private boolean savedToServer = false ;
    
    // This constructor is used by Jackson for temporary deserialization
    // and should not be called by application logic as it does not have a
    // backing file.
    public ProjectState() {
        this.stateFile = null ;
    }
    
    public ProjectState( File projectDir ) {
        this.stateFile = new File( projectDir, "/.workspace/project-state.json" ) ;
        loadState() ;
    }
    
    public ProjectState( ProjectModel projectModel ) {
        this.stateFile = new File( projectModel.getWorkDir(), "project-state.json" ) ;
        loadState() ;
    }
    
    public void setImgCuttingWip( boolean value ) {
        this.imgCuttingWip = value ;
        if( value ) {
            this.imgCuttingComplete = false ;
            this.answersMapped = false ;
            this.topicsMapped = false ;
            this.savedToServer = false ;
        }
        saveState();
    }
    
    public void setImgCuttingComplete( boolean value ) {
        this.imgCuttingComplete = value;
        if( value ) {
            this.imgCuttingWip = false ;
            this.answersMapped = false ;
            this.topicsMapped = false ;
            this.savedToServer = false ;
        }
        saveState();
    }
    
    public void setAnswersMapped( boolean value ) {
        this.answersMapped = value;
        if( value ) {
            this.imgCuttingWip = false ;
            this.imgCuttingComplete = true ;
            this.topicsMapped = false ;
            this.savedToServer = false ;
        }
        saveState();
    }
    
    public void setTopicsMapped( boolean value ) {
        this.topicsMapped = value;
        if( value ) {
            this.imgCuttingWip = false ;
            this.imgCuttingComplete = true ;
            this.answersMapped = true ;
            this.savedToServer = false ;
        }
        saveState();
    }
    
    public void setSavedToServer( boolean value ) {
        this.savedToServer = value;
        if( value ) {
            this.imgCuttingWip = false ;
            this.imgCuttingComplete = true ;
            this.answersMapped = true ;
            this.topicsMapped = true ;
        }
        saveState();
    }
    
    private void loadState() {
        if( stateFile.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                ProjectState loadedState = mapper.readValue( stateFile, ProjectState.class );
                this.imgCuttingWip = loadedState.isImgCuttingWip();
                this.imgCuttingComplete = loadedState.isImgCuttingComplete();
                this.answersMapped = loadedState.isAnswersMapped();
                this.topicsMapped = loadedState.isTopicsMapped();
                this.savedToServer = loadedState.isSavedToServer();
            }
            catch( IOException e ) {
                throw new RuntimeException( "Error loading project state", e );
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
            throw new RuntimeException( "Error saving project state", e );
        }
    }
}
