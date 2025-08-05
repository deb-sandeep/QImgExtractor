package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
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
    
    // Note that this is the golden copy of the sub-image information list.
    // No other part of the application should store a cached copy.
    @Getter private final List<SubImgInfo> subImgInfoList = new ArrayList<>();
    
    PageImage( ProjectModel projectModel, File imgFile ) {
        log.info( "    Loading page: {}.", imgFile.getName() + " ..." ) ;
        this.projectModel = projectModel ;
        this.imgFile = imgFile ;
        this.pageNumber = extractPageNumber( imgFile ) ;
        
        this.loadSubImgInfoList() ;
    }
    
    public void selectedRegionAdded( SubImgInfo newRegionInfo ) {
        addSubImgInfo( newRegionInfo, true ) ;
        projectModel.newSubImgAdded( this, newRegionInfo ) ;
    }
    
    private void loadSubImgInfoList() {
        File imgInfoFile = getImgInfoFile() ;
        if( imgInfoFile.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<SubImgInfo> infoList = mapper.readValue( imgInfoFile, new TypeReference<>() {} );
                
                for( SubImgInfo info : infoList ) {
                    addSubImgInfo( info, false ) ;
                }
                saveSubImgInfoList() ;
            }
            catch( Exception e ) {
                log.error( "Error reading image info.", e ) ;
                showErrorMsg( "Error reading image info", e ) ;
            }
        }
    }
    
    private void addSubImgInfo( SubImgInfo subImgInfo, boolean persistAll ) {
        if( isSubImgInfoValid( subImgInfo ) ) {
            subImgInfoList.add( subImgInfo ) ;
            if( persistAll ) {
                saveSubImgInfoList() ;
            }
        }
    }
    
    // Sub-image information is valid when
    // 1 - The corresponding file exists
    // 2 - The name of the corresponding file is of valid syntax
    private boolean isSubImgInfoValid( SubImgInfo subImgInfo ) {
        
        // Validation 1: The file for this sub-image exists
        File subImgFile = getSubImgFile( subImgInfo ) ;
        if( !subImgFile.exists() ) {
            log.error( "Sub image does not exist: {}", subImgFile.getName() ) ;
            return false ;
        }
        
        // Validation 2: The name of the file is syntactically valid
        try {
            QuestionImage questionImage = new QuestionImage( this, subImgFile ) ;
            subImgInfo.setQuestionImage( questionImage ) ;
        }
        catch( Exception e ) {
            log.error( "Sub image name is not syntactically valid: {}", subImgFile.getName(), e ) ;
            return false ;
        }
        return true ;
    }
    
    private void saveSubImgInfoList() {
        File imgInfoFile = getImgInfoFile() ;
        try {
            Collections.sort( subImgInfoList ) ;
            ObjectMapper mapper = new ObjectMapper() ;
            mapper.addMixIn( Rectangle.class, RectangleMixIn.class);
            mapper.writeValue( imgInfoFile, subImgInfoList );
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    private File getSubImgFile( SubImgInfo subImgInfo ) {
        String fqFileName = getFQFileName( projectModel.getProjectName(),
                                           this.pageNumber,
                                           subImgInfo.getTag() + ".png" ) ;
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
        for( SubImgInfo subImgInfo : subImgInfoList ) {
            subImgFiles.add( getSubImgFile( subImgInfo ) ) ;
        }
        return subImgFiles ;
    }
    
    public QuestionImage getLastQuestionImg() {
        if( subImgInfoList.isEmpty() ) {
            return null ;
        }
        return subImgInfoList.get( subImgInfoList.size()-1 ).getQuestionImage() ;
    }
}
