package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.ExtractedImgInfo;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.extractPageNumber;
import static com.sandy.sconsole.qimgextractor.util.AppUtil.showErrorMsg;

@Slf4j
class PageImage implements Comparable<PageImage> {

    private final ProjectModel projectModel ;
    private final File imgFile ;
    private final int pageNumber ;
    
    private List<ExtractedImgInfo> selectedRegionsInfo ;
    
    PageImage( ProjectModel projectModel, File imgFile ) {
        log.info( "    Loading page: {}.", imgFile.getName() + " ..." ) ;
        this.projectModel = projectModel ;
        this.imgFile = imgFile ;
        this.pageNumber = extractPageNumber( imgFile ) ;
        this.selectedRegionsInfo = this.loadImgInfo() ;
    }
    
    @Override
    public int compareTo( PageImage page ) {
        return this.pageNumber - page.pageNumber ;
    }
    
    private List<ExtractedImgInfo> loadImgInfo() {
        List<ExtractedImgInfo> imgInfoList = new ArrayList<>() ;
        File imgInfoFile = getImgInfoFile() ;
        if( imgInfoFile.exists() ) {
            try {
                ObjectInputStream ois = new ObjectInputStream( new FileInputStream( imgInfoFile ) ) ;
                imgInfoList = ( List<ExtractedImgInfo> )ois.readObject() ;
                ois.close() ;
            }
            catch( Exception e ) {
                log.error( "Error reading image info.", e ) ;
                showErrorMsg( "Error reading image info", e ) ;
            }
        }
        return imgInfoList ;
    }
    
    private void saveImgInfo( List<ExtractedImgInfo> selectedRegionsInfo ) {
        File imgInfoFile = getImgInfoFile() ;
        try {
            ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( imgInfoFile ) ) ;
            oos.writeObject( selectedRegionsInfo ) ;
            oos.close() ;
        }
        catch( Exception e ) {
            log.error( "Error saving image info.", e ) ;
            showErrorMsg( "Error saving image info.", e ) ;
        }
    }
    
    private File getImgInfoFile() {
        return new File( projectModel.getWorkDir(),
                         AppUtil.stripExtension( imgFile ) + ".regions.info" );
    }
    
    private void validateRegionInfo( List<ExtractedImgInfo> selectedRegionsInfo ) {
        if( selectedRegionsInfo == null ) {
            throw new IllegalArgumentException( "Selected regions info cannot be null." ) ;
        }
    }
}
