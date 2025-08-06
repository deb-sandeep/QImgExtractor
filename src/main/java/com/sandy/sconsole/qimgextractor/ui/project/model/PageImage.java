package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SelectedRegionMetadata;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.awt.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.*;

@Slf4j
public class PageImage implements Comparable<PageImage> {
    
    @JsonAutoDetect(
        fieldVisibility = JsonAutoDetect.Visibility.ANY,
        getterVisibility = JsonAutoDetect.Visibility.NONE,
        isGetterVisibility = JsonAutoDetect.Visibility.NONE,
        setterVisibility = JsonAutoDetect.Visibility.NONE
    )
    abstract static class RectangleMixIn {}
    
    private final ProjectModel projectModel ;
    
    @Getter private final File imgFile ;
    @Getter private final int pageNumber ;
    
    @Getter private final List<QuestionImage> qImgList = new ArrayList<>() ;
    
    PageImage( ProjectModel projectModel, File imgFile ) {
        this.projectModel = projectModel ;
        this.imgFile = imgFile ;
        this.pageNumber = extractPageNumber( imgFile ) ;
        
        this.loadQuestionImgList() ;
    }
    
    private void loadQuestionImgList() {
        File imgInfoFile = getImgInfoFile() ;
        if( imgInfoFile.exists() ) {
            try {
                ObjectMapper                 mapper   = new ObjectMapper();
                List<SelectedRegionMetadata> infoList = mapper.readValue( imgInfoFile, new TypeReference<>() {} );
                
                for( SelectedRegionMetadata info : infoList ) {
                    addQImg( info ) ;
                }
                Collections.sort( qImgList ) ;
                saveSubImgInfoList() ;
            }
            catch( Exception e ) {
                log.error( "Error reading image info.", e ) ;
                showErrorMsg( "Error reading image info", e ) ;
            }
        }
    }
    
    private void addQImg( SelectedRegionMetadata selRegionMetadata ) {
        if( isSubImgInfoValid( selRegionMetadata ) ) {
            File subImgFile = getSubImgFile( selRegionMetadata ) ;
            QuestionImage questionImage = new QuestionImage( this, subImgFile, selRegionMetadata ) ;
            addQImg( questionImage, false ) ;
        }
    }
    
    public void addQImg( QuestionImage qImg, boolean persist ) {
        qImgList.add( qImg ) ;
        if( persist ) {
            saveSubImgInfoList() ;
        }
    }
    
    // Sub-image information is valid when
    // 1 - The corresponding file exists
    // 2 - The name of the corresponding file is of valid syntax
    private boolean isSubImgInfoValid( SelectedRegionMetadata selRegionMetadata ) {
        
        // Validation 1: The file for this sub-image exists
        File subImgFile = getSubImgFile( selRegionMetadata ) ;
        if( !subImgFile.exists() ) {
            log.error( "Sub image does not exist: {}", subImgFile.getName() ) ;
            return false ;
        }
        
        // Validation 2: The name of the file is syntactically valid
        try {
            new QuestionImage( this, subImgFile, selRegionMetadata ) ;
        }
        catch( Exception e ) {
            log.error( "Sub image name is not syntactically valid: {}", subImgFile.getName(), e ) ;
            return false ;
        }
        return true ;
    }
    
    public void saveSubImgInfoList() {
        File imgInfoFile = getImgInfoFile() ;
        try {
            ObjectMapper mapper = new ObjectMapper() ;
            mapper.enable( SerializationFeature.INDENT_OUTPUT ) ;
            mapper.addMixIn( Rectangle.class, RectangleMixIn.class) ;
            mapper.writeValue( imgInfoFile, getSubImgInfoList() ) ;
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    public List<SelectedRegionMetadata> getSubImgInfoList() {
        List<SelectedRegionMetadata> list = new ArrayList<>() ;
        for( QuestionImage qImg : qImgList ) {
            list.add( qImg.getSelRegionMetadata() ) ;
        }
        return list ;
    }
    
    private File getSubImgFile( SelectedRegionMetadata selRegionMetadata ) {
        String fqFileName = getFQFileName( projectModel.getProjectName(),
                                           this.pageNumber,
                                           selRegionMetadata.getTag() + ".png" ) ;
        return new File( projectModel.getExtractedImgDir(), fqFileName ) ;
    }
    
    private File getImgInfoFile() {
        return new File( projectModel.getWorkDir(),
                         stripExtension( imgFile ) + ".regions.json" );
    }
    
    @Override
    public int compareTo( PageImage page ) {
        return this.pageNumber - page.pageNumber ;
    }
    
    public List<File> getSubImgFiles() {
        List<File> subImgFiles = new ArrayList<>() ;
        for( QuestionImage qImg : qImgList ) {
            subImgFiles.add( qImg.getQImgFile() ) ;
        }
        return subImgFiles ;
    }
    
    public QuestionImage getLastQuestionImg() {
        if( qImgList.isEmpty() ) {
            return null ;
        }
        return qImgList.get( qImgList.size()-1 ) ;
    }
}
