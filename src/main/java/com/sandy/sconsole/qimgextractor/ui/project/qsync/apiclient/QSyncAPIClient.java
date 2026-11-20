package com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient;

import com.fasterxml.jackson.databind.JsonNode;
import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicAssociator;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.logpanel.QSyncLogPanel;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.net.APIClient;
import com.sandy.sconsole.qimgextractor.util.net.APIResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.List;

public class QSyncAPIClient {

    /**
     * A successful sync: the server's id of the question and the hash of
     * the question content which was sent.
     */
    public record SyncResult( int questionId, String syncToken ) {}
    
    private final APIClient apiClient ;
    private final AppConfig appConfig ;
    
    public QSyncAPIClient() {
        this.apiClient = QImgExtractor.getBean( APIClient.class ) ;
        this.appConfig = QImgExtractor.getBean( AppConfig.class ) ;
    }
    
    /**
     * Posts the question to the server. Safe to call from several threads
     * at once, for different questions. The question is not changed - the
     * caller records the sync info.
     *
     * @return the sync result, or null if the server did not save the question.
     */
    public SyncResult syncQuestion( Question question, QSyncLogPanel.Buffer log )
        throws Exception {
        
        // The hash is taken before the content is read, so a change made
        // during the sync shows the question as modified after sync.
        String syncToken = question.getHashCode() ;
        QuestionVO vo = populateQuestionVO( question ) ;
        log.logQuestion( vo ) ;
        
        log.log( "\nPosting question to server..." ) ;
        APIResponse response = this.apiClient.post( appConfig.getQSyncAPIEndpoint(), vo, log::log ) ;
        log.log( "Response : " + response ) ;
        log.log( "Done." ) ;
        
        // Process the API response
        if( response.code() == 200 ) {
            JsonNode jsonNode = response.json().get( "data" ) ;
            boolean questionSaved = jsonNode.get( "questionSaved" ).asBoolean() ;
            if( questionSaved ) {
                log.log( "Question saved successfully." ) ;
                int questionId = jsonNode.get( "questionId" ).asInt() ;
                return new SyncResult( questionId, syncToken ) ;
            }
            else {
                String msg = jsonNode.get( "errorMessage" ).asText() ;
                log.log( "Error saving question. " + msg ) ;
            }
        }
        else {
            log.log( "Error syncing question. Server returned status code " + response.code() ) ;
        }
        return null ;
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
        
        // Topics associated before provenance was recorded are sent as user
        // associations, which is also what the server backfills them with.
        TopicAssociator associator = question.getTopicAssociator() ;
        if( associator == null ) {
            associator = TopicAssociator.user() ;
        }
        vo.setTopicAssocActor( associator.getActor() ) ;
        vo.setTopicAssocAiModel( associator.getModel() ) ;
        vo.setTopicAssocConfidence( associator.getConfidence() ) ;
        
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
