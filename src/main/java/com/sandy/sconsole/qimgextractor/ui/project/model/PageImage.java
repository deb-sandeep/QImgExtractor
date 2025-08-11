package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SelectedRegionMetadata;
import com.sandy.sconsole.qimgextractor.ui.project.model.state.PageImageState;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.*;

@Slf4j
public class PageImage implements Comparable<PageImage> {
    
    // A mix-in class to exclude the Rectangle.class from the JSON serialization
    // Why? Jackson queries the getter methods to get dynamic information about
    // the object. If the Rectangle class is included in the JSON serialization,
    // Jackson will try to invoke the Rectangle.getBounds() method, which recurses
    // resulting in a StackOverflowError. Fix suggested by ChatGPT :)
    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    abstract static class RectangleMixIn {}
    
    private final ProjectModel projectModel ; // Injected
    
    @Getter private final File imgFile ; // Injected
    @Getter private final int pageNumber ; // Derived
    
    @Getter private final List<QuestionImage> qImgList = new ArrayList<>() ;
    
    @Getter @Setter
    private PageImageState state = null ;
    
    PageImage( ProjectModel projectModel, File imgFile ) {
        this.projectModel = projectModel ;
        this.imgFile = imgFile ;
        this.pageNumber = extractPageNumber( imgFile ) ;
        
        this.loadQuestionImages() ;
    }
    
    private void loadQuestionImages() {
        File qImgMetaFile = getQuestionImgMetadataFile() ;
        if( qImgMetaFile.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<SelectedRegionMetadata> regionMetaList = mapper.readValue( qImgMetaFile, new TypeReference<>() {} );
                
                for( SelectedRegionMetadata metadata : regionMetaList ) {
                    addQuestionImg( metadata ) ;
                }
                Collections.sort( qImgList ) ;
                saveQuestionImgMetadata() ;
            }
            catch( Exception e ) {
                log.error( "Error reading image info.", e ) ;
                showErrorMsg( "Error reading image info", e ) ;
            }
        }
    }
    
    private File getQuestionImgMetadataFile() {
        return new File( projectModel.getWorkDir(),
                         stripExtension( imgFile ) + ".regions.json" );
    }
    
    private void addQuestionImg( SelectedRegionMetadata imgRegionMetadata ) {
        if( isQuestionMetadataValid( imgRegionMetadata ) ) {
            File imgFile = getQuestionImgFile( imgRegionMetadata ) ;
            QuestionImage qImg = new QuestionImage( this, imgFile, imgRegionMetadata ) ;
            addQuestionImg( qImg, false ) ;
        }
    }
    
    public void addQuestionImg( QuestionImage qImg, boolean persistMetadata ) {
        qImgList.add( qImg ) ;
        if( persistMetadata ) {
            saveQuestionImgMetadata() ;
        }
    }
    
    public void deleteQuestionImg( QuestionImage qImg ) {
        qImgList.remove( qImg ) ;
        saveQuestionImgMetadata() ;
    }

    // Sub-image information is valid when
    // 1 - The corresponding file exists
    // 2 - The name of the corresponding file is of valid syntax
    private boolean isQuestionMetadataValid( SelectedRegionMetadata regionMetadata ) {
        
        // Validation 1: The file for this question-image exists
        File qImgFile = getQuestionImgFile( regionMetadata ) ;
        if( !qImgFile.exists() ) {
            log.error( "Question image does not exist: {}", qImgFile.getName() ) ;
            return false ;
        }
        
        // Validation 2: The name of the file is syntactically valid
        try {
            new QuestionImage( this, qImgFile, regionMetadata ) ;
        }
        catch( Exception e ) {
            log.error( "Sub image name is not syntactically valid: {}", qImgFile.getName(), e ) ;
            return false ;
        }
        return true ;
    }
    
    public void saveQuestionImgMetadata() {
        File metadataFile = getQuestionImgMetadataFile() ;
        try {
            ObjectMapper mapper = new ObjectMapper() ;
            mapper.enable( SerializationFeature.INDENT_OUTPUT ) ;
            mapper.addMixIn( Rectangle.class, RectangleMixIn.class) ;
            mapper.writeValue( metadataFile, getSelectedRegionsMetadataList() ) ;
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    public List<SelectedRegionMetadata> getSelectedRegionsMetadataList() {
        List<SelectedRegionMetadata> list = new ArrayList<>() ;
        for( QuestionImage qImg : qImgList ) {
            list.add( qImg.getImgRegionMetadata() ) ;
        }
        return list ;
    }
    
    private File getQuestionImgFile( SelectedRegionMetadata selRegionMetadata ) {
        String fqFileName = getFQFileName( projectModel.getProjectName(),
                                           this.pageNumber,
                                           selRegionMetadata.getTag() + ".png" ) ;
        return new File( projectModel.getExtractedImgDir(), fqFileName ) ;
    }
    
    @Override
    public int compareTo( PageImage page ) {
        return this.pageNumber - page.pageNumber ;
    }
    
    public List<File> getQuestionImgFiles() {
        List<File> files = new ArrayList<>() ;
        for( QuestionImage qImg : qImgList ) {
            files.add( qImg.getImgFile() ) ;
        }
        return files ;
    }
    
    public QuestionImage getLastQuestionImg() {
        if( qImgList.isEmpty() ) {
            return null ;
        }
        return qImgList.get( qImgList.size()-1 ) ;
    }
}
