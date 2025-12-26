package com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.logpanel.QSyncLogPanel;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.net.APIClient;
import com.sandy.sconsole.qimgextractor.util.net.APIResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Date;
import java.util.List;

public class QSyncAPIClient {

    private final QSyncLogPanel logPanel ;
    private final APIClient apiClient ;
    private final AppConfig appConfig ;
    
    public QSyncAPIClient( QSyncLogPanel logPanel ) {
        this.logPanel = logPanel ;
        this.apiClient = QImgExtractor.getBean( APIClient.class ) ;
        this.appConfig = QImgExtractor.getBean( AppConfig.class ) ;
    }
    
    public boolean syncQuestion( Question question ) throws Exception {
        
        QuestionVO vo = populateQuestionVO( question ) ;
        logPanel.logQuestion( vo ) ;
        
        logPanel.log( "\nPosting question to server..." ) ;
        APIResponse response = this.apiClient.post( appConfig.getQSyncAPIEndpoint(), vo, logPanel::log ) ;
        logPanel.log( "Response : " + response ) ;
        logPanel.log( "Done." ) ;
        
        // Process the API response
        if( response.code() == 200 ) {
            JsonNode jsonNode = response.json().get( "data" ) ;
            boolean questionSaved = jsonNode.get( "questionSaved" ).asBoolean() ;
            if( questionSaved ) {
                logPanel.log( "Question saved successfully." ) ;
                int questionId = jsonNode.get( "questionId" ).asInt() ;
                question.setServerSyncTime( new Date() ) ;
                question.setId( questionId ) ;
                question.setServerSyncToken( question.getHashCode() ) ;
                return true ;
            }
            else {
                String msg = jsonNode.get( "errorMessage" ).asText() ;
                logPanel.log( "Error saving question. " + msg ) ;
            }
        }
        else {
            logPanel.log( "Error syncing question. Server returned status code " + response.code() ) ;
        }
        return false ;
    }
    
    private QuestionVO populateQuestionVO( Question question )
        throws Exception {
        
        QuestionVO vo = new QuestionVO() ;
        
        // TODO: If the question has a server ID populate it.
        
        vo.setQuestionId( question.getQRef() ) ;
        vo.setSyllabusName( question.getTopic().getSyllabusName() ) ;
        vo.setTopicId( question.getTopic().getId() ) ;
        vo.setSourceId( question.getSrcId() ) ;
        vo.setProblemType( question.getQID().getQuestionType() ) ;
        vo.setLctSequence( question.getQID().getLctSequence() ) ;
        vo.setQuestionNumber( question.getQID().getQuestionNumber() ) ;
        vo.setAnswer( question.getAnswer() ) ;
        
        List<QuestionImage> qImgs = question.getQImgList() ;
        for( int i=0; i<qImgs.size(); i++ ) {
            QuestionImage qImg = qImgs.get( i ) ;
            vo.getQuestionImages().add( createQuestionImgVO( qImg, i ) ) ;
        }
        
        return vo ;
    }
    
    private QuestionImageVO createQuestionImgVO( QuestionImage qImg, int sequence )
        throws Exception {
        
        byte[] fileContents = FileUtils.readFileToByteArray( qImg.getImgFile() ) ;
        BufferedImage img = ImageIO.read( new ByteArrayInputStream( fileContents ) ) ;
        
        QuestionImageVO vo = new QuestionImageVO() ;
        vo.setSequence( sequence ) ;
        vo.setPageNumber( qImg.getPageNumber() ) ;
        vo.setFileName( qImg.getShortFileName() ) ;
        vo.setLctCtxImage( qImg.getQId().isLCTContext() ) ;
        vo.setPartNumber( qImg.getPartNumber() ) ;
        vo.setImgWidth( img.getWidth() ) ;
        vo.setImgHeight( img.getHeight() ) ;
        vo.setImgData( Base64.encodeBase64String( fileContents ) ) ;

        return vo ;
    }
}
