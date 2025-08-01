package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgInfo;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.*;

@Slf4j
public class PageImage implements Comparable<PageImage> {

    private final ProjectModel projectModel ;
    
    @Getter private final File imgFile ;
    @Getter private final int pageNumber ;
    @Getter private List<SubImgInfo> subImgInfoList ;
    
    private Map<String, SubImgInfo> subImgInfoMap ;
    
    PageImage( ProjectModel projectModel, File imgFile ) {
        log.info( "    Loading page: {}.", imgFile.getName() + " ..." ) ;
        this.projectModel = projectModel ;
        this.imgFile = imgFile ;
        this.pageNumber = extractPageNumber( imgFile ) ;
        
        this.loadSubImgInfoList() ;
    }
    
    private void loadSubImgInfoList() {
        subImgInfoList = new ArrayList<>() ;
        File imgInfoFile = getImgInfoFile() ;
        if( imgInfoFile.exists() ) {
            try {
                ObjectInputStream ois = new ObjectInputStream( new FileInputStream( imgInfoFile ) ) ;
                subImgInfoList = ( List<SubImgInfo> )ois.readObject() ;
                ois.close() ;
                
                validateAndClassifySubImages() ;
            }
            catch( Exception e ) {
                log.error( "Error reading image info.", e ) ;
                showErrorMsg( "Error reading image info", e ) ;
            }
        }
    }
    
    private void saveSubImgInfoList( boolean runValidation ) {
        File imgInfoFile = getImgInfoFile() ;
        try {
            // Validate and classify sub images before saving so that in case
            // of validation failures, the persisted copy won't be overwritten
            if( runValidation ) {
                validateAndClassifySubImages() ;
            }
            
            ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( imgInfoFile ) ) ;
            oos.writeObject( subImgInfoList ) ;
            oos.close() ;
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    // Call this method after deserializing any persisted sub image information
    // and before persisting the sub image information list.
    private void validateAndClassifySubImages() {
        
        List<SubImgInfo> validList = new ArrayList<>() ;
        Map<String, SubImgInfo> validMap = new HashMap<>() ;
        
        for( SubImgInfo subImgInfo : subImgInfoList ) {
            boolean isValid = isSubImgInfoValid( subImgInfo ) ;
            if( isValid ) {
                validList.add( subImgInfo ) ;
                validMap.put( subImgInfo.getTag(), subImgInfo ) ;
            }
            else {
                log.error( "    Sub image is invalid: {}", subImgInfo.getTag() ) ;
            }
        }

        // TODO: Organize sub images into logical questions. Tricky, the LCT
        //  context will belong now to multiple questions :) Go through a
        //  round of refactoring keeping in mind that the sub image info can
        //  be edited, deleted and created new at runtime and the tree
        //  should be updated. So model should be observable and provide apis
        //  to properly bookkeep a newly added entity or modification of existing
        //  entity.
        
        // If we have some invalid sub images metadata, persist a fresh
        // copy of a valid metadata list. This time don't run validation.
        if( validList.size() != subImgInfoList.size() ) {
            subImgInfoList = validList ;
            subImgInfoMap = validMap ;
            saveSubImgInfoList( false ) ;
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
            QuestionImage questionImage = new QuestionImage( subImgFile ) ;
            subImgInfo.setQuestionImage( questionImage ); ;
        }
        catch( Exception e ) {
            log.error( "Sub image name is not syntactically valid: {}", subImgFile.getName() ) ;
            return false ;
        }
        return true ;
    }
    
    private File getSubImgFile( SubImgInfo subImgInfo ) {
        String fqFileName = getFQFileName( projectModel.getProjectName(),
                                           this.pageNumber,
                                           subImgInfo.getTag() + ".png" ) ;
        return new File( projectModel.getExtractedImgDir(), fqFileName ) ;
    }
    
    private File getImgInfoFile() {
        return new File( projectModel.getWorkDir(),
                         stripExtension( imgFile ) + ".regions.info" );
    }
    
    @Override
    public int compareTo( PageImage page ) {
        return this.pageNumber - page.pageNumber ;
    }
    
    public void selectedRegionsUpdated( List<SubImgInfo> selectedRegionsInfo ) {
        this.subImgInfoList = selectedRegionsInfo ;
        saveSubImgInfoList( true ) ;
    }
    
    public SubImgInfo getSubImgInfo( String tag ) {
        return subImgInfoMap.get( tag ) ;
    }
    
    public List<File> getSubImgFiles() {
        List<File> subImgFiles = new ArrayList<>() ;
        for( SubImgInfo subImgInfo : subImgInfoList ) {
            subImgFiles.add( getSubImgFile( subImgInfo ) ) ;
        }
        return subImgFiles ;
    }
}
