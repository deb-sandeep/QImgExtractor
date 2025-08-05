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
    
    @Getter private final List<QuestionImage> qImgList = new ArrayList<>() ;
    
    PageImage( ProjectModel projectModel, File imgFile ) {
        log.info( "    Loading page: {}.", imgFile.getName() + " ..." ) ;
        this.projectModel = projectModel ;
        this.imgFile = imgFile ;
        this.pageNumber = extractPageNumber( imgFile ) ;
        
        this.loadQuestionImgList() ;
    }
    
    private void loadQuestionImgList() {
        File imgInfoFile = getImgInfoFile() ;
        if( imgInfoFile.exists() ) {
            try {
                ObjectMapper mapper = new ObjectMapper();
                List<SubImgInfo> infoList = mapper.readValue( imgInfoFile, new TypeReference<>() {} );
                
                for( SubImgInfo info : infoList ) {
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
    
    private void addQImg( SubImgInfo subImgInfo ) {
        if( isSubImgInfoValid( subImgInfo ) ) {
            File subImgFile = getSubImgFile( subImgInfo ) ;
            QuestionImage questionImage = new QuestionImage( this, subImgFile, subImgInfo ) ;
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
    private boolean isSubImgInfoValid( SubImgInfo subImgInfo ) {
        
        // Validation 1: The file for this sub-image exists
        File subImgFile = getSubImgFile( subImgInfo ) ;
        if( !subImgFile.exists() ) {
            log.error( "Sub image does not exist: {}", subImgFile.getName() ) ;
            return false ;
        }
        
        // Validation 2: The name of the file is syntactically valid
        try {
            new QuestionImage( this, subImgFile, subImgInfo ) ;
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
            ObjectMapper mapper = new ObjectMapper() ;
            mapper.addMixIn( Rectangle.class, RectangleMixIn.class);
            mapper.writeValue( imgInfoFile, getSubImgInfoList() );
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    public List<SubImgInfo> getSubImgInfoList() {
        List<SubImgInfo> list = new ArrayList<>() ;
        for( QuestionImage qImg : qImgList ) {
            list.add( qImg.getSubImgInfo() ) ;
        }
        return list ;
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
