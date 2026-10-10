package com.sandy.sconsole.qimgextractor.api;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.swing.*;
import java.io.File;
import java.util.LinkedHashMap;
import java.util.Map;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.findProjectDir;

// Lets other programs drive the UI. The HTTP request is validated and
// answered immediately; the UI work is queued on the EDT since opening a
// project can take a while and may pop up confirmation dialogs.
@Slf4j
@RestController
@RequestMapping( "/api/remote" )
public class RemoteCommandController {

    private final AppConfig appConfig ;
    private final MainFrame mainFrame ;

    public RemoteCommandController( AppConfig appConfig, MainFrame mainFrame ) {
        this.appConfig = appConfig ;
        this.mainFrame = mainFrame ;
    }

    // Opens the project to which the question image belongs, selects the
    // page tab and scrolls the image into view.
    //
    // imgName : <srcId>.<pageNum>.<tagName>[(<partNumber>)][.png]
    //           A path is accepted too; only the file name is used.
    //
    // Responses:
    //   202 - request is valid and has been queued for the UI
    //   400 - image name is not of the expected format
    //   404 - project directory or question image file not found
    @PostMapping( "/show-question-image" )
    public ResponseEntity<Map<String, Object>> showQuestionImage(
            @RequestParam( "imgName" ) String imgName ) {

        log.info( "## Remote command: show question image {}", imgName ) ;

        String fileName = new File( imgName.trim() ).getName() ;
        if( !fileName.endsWith( ".png" ) ) {
            fileName += ".png" ;
        }

        String[] parts = fileName.split( "\\." ) ;
        if( parts.length < 4 ) {
            return error( HttpStatus.BAD_REQUEST, "Image name '" + imgName +
                          "' is not of the format <srcId>.<pageNum>.<tagName>.png" ) ;
        }

        String projectName = parts[0].trim() ;
        int pageNumber ;
        try {
            pageNumber = Integer.parseInt( parts[1].trim() ) ;
        }
        catch( NumberFormatException e ) {
            return error( HttpStatus.BAD_REQUEST, "Invalid page number '" +
                          parts[1] + "' in image name '" + imgName + "'" ) ;
        }

        File projectDir = findProjectDir( appConfig.getSourceBaseDir(), projectName ) ;
        if( projectDir == null ) {
            return error( HttpStatus.NOT_FOUND, "Project '" + projectName +
                          "' not found under " + appConfig.getSourceBaseDir() ) ;
        }

        File qImgFile = new File( new File( projectDir, "question-images" ), fileName ) ;
        if( !qImgFile.exists() ) {
            return error( HttpStatus.NOT_FOUND, "Question image '" + fileName +
                          "' not found in project " + projectDir.getAbsolutePath() ) ;
        }

        final String qImgFileName = fileName ;
        SwingUtilities.invokeLater( () -> mainFrame.showQuestionImage( projectDir, qImgFileName ) ) ;

        Map<String, Object> body = new LinkedHashMap<>() ;
        body.put( "status", "accepted" ) ;
        body.put( "projectDir", projectDir.getAbsolutePath() ) ;
        body.put( "pageNumber", pageNumber ) ;
        body.put( "imgName", qImgFileName ) ;
        return ResponseEntity.status( HttpStatus.ACCEPTED ).body( body ) ;
    }

    private ResponseEntity<Map<String, Object>> error( HttpStatus status, String msg ) {
        log.warn( "  Remote command rejected. {}", msg ) ;
        Map<String, Object> body = new LinkedHashMap<>() ;
        body.put( "status", "error" ) ;
        body.put( "message", msg ) ;
        return ResponseEntity.status( status ).body( body ) ;
    }
}
