package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Slf4j
public class Question extends QuestionImageCluster
    implements Comparable<Question> {
    
    public static class InvalidAnswerException extends Exception {
        public InvalidAnswerException( String message ) {
            super( message ) ;
        }
    }
    
    public static class SyncInfo {
        
        @Getter @Setter
        private int id = -1 ;
        
        @Getter @Setter
        private Date syncTime = null ;
        
        @Getter @Setter
        private String syncToken = null ;
    }
    
    private QuestionImageCluster lctCtxImgCluster = null ;
    
    @Getter
    private final String srcId ;
    
    @Getter
    private String answer = null ;
    
    @Getter @Setter
    private Topic topic = null ;
    
    @Getter
    private MMTAnswer mmtAnswer = null ;
    
    private final SyncInfo devSyncInfo = new SyncInfo() ;
    
    private final SyncInfo prodSyncInfo = new SyncInfo() ;
    
    Question( String srcId, QID qID ) {
        super( qID ) ;
        this.srcId = srcId ;
    }
    
    public boolean isLCT() {
        return qID.isLCT() ;
    }
    
    public String getLCTRoot() {
        if( isLCT() ) {
            return qID.toString().substring( 0, qID.toString().lastIndexOf( '/' )  ) ;
        }
        return null ;
    }
    
    public void addLCTCtxImgCluster( QuestionImageCluster lctCtxCluster ) {
        this.lctCtxImgCluster = lctCtxCluster ;
    }
    
    @Override
    public int compareTo( Question q ) {
        if( getSubjectTypeIndex() == q.getSubjectTypeIndex() ) {
            if( getQTypeIndex() == q.getQTypeIndex() ) {
                if( isLCT() && q.isLCT() ) {
                    if( qID.getLctSequence() == q.qID.getLctSequence() ) {
                        return qID.getQuestionNumber() - q.qID.getQuestionNumber() ;
                    }
                    return qID.getLctSequence() - q.qID.getLctSequence() ;
                }
                return qID.getQuestionNumber() - q.qID.getQuestionNumber() ;
            }
            return getQTypeIndex() - q.getQTypeIndex() ;
        }
        return getSubjectTypeIndex() - q.getSubjectTypeIndex() ;
    }
    
    private int getQTypeIndex() {
        return QID.Q_TYPE_SEQ.indexOf( qID.getQuestionType() ) ;
    }
    
    private int getSubjectTypeIndex() {
        String subCode = qID.getSubjectCode() ;
        return switch( subCode ) {
            case "P" -> 0;
            case "C" -> 1;
            case "M" -> 2;
            default -> throw new IllegalArgumentException( "Invalid subject code: " + subCode ) ;
        } ;
    }
    
    public String getQRef() {
        return qID.getParent().getSrcId() + "://" + qID ;
    }
    
    public JSONObject getSerializedForm() throws Exception {
        
        JSONObject json = new JSONObject() ;
        json.put( "qid", qID.toString() ) ;
        json.put( "lctSeq", qID.getLctSequence() ) ;
        json.put( "lctCtxImages", getImgInfoList( lctCtxImgCluster ) ) ;
        json.put( "questionImages", getImgInfoList( this ) ) ;
        json.put( "answer", answer ) ;
        
        if( topic != null ) {
            json.put( "topic", topic.getSerializedForm() ) ;
        }
        else {
            json.put( "topic", JSONObject.NULL ) ;
        }
        
        json.put( "devSyncInfo", getSyncInfoSerializedForm( devSyncInfo ) ) ;
        json.put( "prodSyncInfo", getSyncInfoSerializedForm( prodSyncInfo ) ) ;

        return json ;
    }
    
    private JSONObject getSyncInfoSerializedForm( SyncInfo info ) throws Exception {
        
        JSONObject json = new JSONObject() ;
        json.put( "id", info.id ) ;
        json.put( "syncTime", info.syncTime != null ? info.syncTime.getTime() : JSONObject.NULL ) ;
        json.put( "syncToken", info.syncToken != null ? info.syncToken : JSONObject.NULL ) ;
        return json ;
    }
    
    public void deserializeFrom( JSONObject qJson ) throws Exception {
        
        TopicRepo topicRepo = QImgExtractor.getBean( TopicRepo.class ) ;

        if( !qJson.isNull( "answer" ) ) {
            try {
                this.setAnswer( qJson.getString( "answer" ) );
            }
            catch( Question.InvalidAnswerException e ) {
                log.error( "Invalid answer found in persisted state.", e ) ;
            }
        }
        
        if( !qJson.isNull( "topic" ) ) {
            JSONObject topicJson = qJson.getJSONObject( "topic" );
            int topicId = topicJson.getInt( "id" ) ;
            this.setTopic( topicRepo.getTopicById( topicId ) ) ;
        }
        
        if( qJson.has( "devSyncInfo" ) ) {
            deserializeSyncInfo( qJson.getJSONObject( "devSyncInfo" ), devSyncInfo ) ;
        }
        
        if( qJson.has( "prodSyncInfo" ) ) {
            deserializeSyncInfo( qJson.getJSONObject( "prodSyncInfo" ), prodSyncInfo ) ;
        }
    }
    
    private void deserializeSyncInfo( JSONObject json, SyncInfo info ) throws Exception {
        
        info.setId( json.getInt( "id" ) ) ;
        
        if( !json.isNull( "syncTime" ) ) {
            long syncTime = json.getLong( "syncTime" ) ;
            info.setSyncTime( new Date( syncTime ) ) ;
        }
        
        String token = json.getString( "syncToken" ) ;
        info.setSyncToken( token ) ;
    }
    
    private JSONArray getImgInfoList( QuestionImageCluster qImgCluster ) throws Exception {
        
        List<QuestionImage> qImgList = qImgCluster != null ? qImgCluster.qImgList : Collections.emptyList() ;
        JSONArray array = new JSONArray() ;
        for( QuestionImage qImg : qImgList ) {
            BufferedImage img = ImageIO.read( qImg.getImgFile() ) ;
            
            JSONObject imgInfo = new JSONObject() ;
            imgInfo.put( "fileName", qImg.getImgFile().getName() ) ;
            imgInfo.put( "imgWidth", img.getWidth() ) ;
            imgInfo.put( "imgHeight", img.getHeight() ) ;
            
            array.put( imgInfo ) ;
        }
        return array ;
    }
    
    public List<QuestionImage> getQImgList() {
        
        List<QuestionImage> list = new ArrayList<>() ;
        if( lctCtxImgCluster != null ) {
            list.addAll( lctCtxImgCluster.qImgList ) ;
        }
        list.addAll( super.qImgList ) ;
        return list ;
    }
    
    public void setAnswer( String ans ) throws InvalidAnswerException {
        
        this.answer = ans ;
        if( qID.getQuestionType().equals( QID.MMT ) ) {
            this.mmtAnswer = new MMTAnswer( ans ) ;
        }
    }
    
    public void setRawAnswer( String ans ) throws InvalidAnswerException {
        
        if( ans == null || ans.trim().isEmpty() ) {
            throw new InvalidAnswerException( "Answer cannot be empty" ) ;
        }
        
        String qType = qID.getQuestionType() ;
        switch( qType ) {
            case QID.SCA, QID.LCT -> formatAndStoreSCAAnswer( ans ) ;
            case QID.IVT, QID.NVT -> formatAndStoreNVTAnswer( ans ) ;
            case QID.MCA -> formatAndStoreMCQAnswer( ans );
            case QID.MMT -> formatAndStoreMMTAnswer( ans ) ;
        }
    }
    
    private void validateSCAChoice( String text ) throws InvalidAnswerException {
        if( !text.matches( "^[A-Da-d1-4]$" ) ) {
            throw new InvalidAnswerException( "Answer must be either A, B, C or D. Found " + text + " instead." ) ;
        }
    }
    
    private void formatAndStoreSCAAnswer( String ans ) throws InvalidAnswerException {
        validateSCAChoice( ans );
        this.answer = ans.toUpperCase() ;
    }
    
    private void formatAndStoreNVTAnswer( String ans ) throws InvalidAnswerException {
        try {
            Double.parseDouble( ans );
            this.answer = ans.trim();
        }
        catch( Exception e ) {
            throw new InvalidAnswerException( "Answer must be a number. Found " + ans + " instead." );
        }
    }
    
    private void formatAndStoreMCQAnswer( String ans )
            throws InvalidAnswerException {
        
        String[] parts = ans.split( "," );
        StringBuilder answerText = new StringBuilder();
        for( String part : parts ) {
            part = part.trim() ;
            validateSCAChoice( part );
            answerText.append( part.toUpperCase() ).append( "," );
        }
        if( answerText.charAt( answerText.length() - 1 ) == ',' ) {
            answerText.deleteCharAt( answerText.length() - 1 );
        }
        this.answer = answerText.toString() ;
    }
    
    private void formatAndStoreMMTAnswer( String ans )
        throws InvalidAnswerException {
        
        String cleanedAnswer = cleanMMTInputText( ans ) ;
        this.mmtAnswer = new MMTAnswer( cleanedAnswer ) ;
        this.answer = this.mmtAnswer.toString() ;
    }
    
    private String cleanMMTInputText( String text ) {
        log.debug( "Cleaning MMT input text: {}", text ) ;
        final String VALID_CHARS = "ABCDpqrstPQRSTF,#" ;
        StringBuilder sb = new StringBuilder() ;
        for( int i = 0; i < text.length(); i++ ) {
            char c = text.charAt( i ) ;
            if( VALID_CHARS.indexOf( c ) != -1 ) {
                if( c == 'F' ) {
                    c = 'r' ;
                }
                sb.append( c ) ;
            }
            else if( c == '4' ) {
                sb.append( "q" ) ;
            }
        }
        log.debug( "Cleaned MMT input text: {}", sb ) ;
        return sb.toString().toUpperCase() ;
    }
    
    public boolean isSynced() {
        return getSyncInfo().syncTime != null ;
    }
    
    public boolean isModifiedAfterSync() {
        SyncInfo syncInfo = getSyncInfo() ;
        return isSynced() && !syncInfo.syncToken.equals( getHashCode() ) ;
    }
    
    private SyncInfo getSyncInfo() {
        AppConfig config = QImgExtractor.getBean( AppConfig.class ) ;
        if( config.isDevProfile() ) {
            return devSyncInfo ;
        }
        return prodSyncInfo ;
    }
    
    public boolean isReadyForServerSync() {
        if( srcId != null ) {
            if( answer != null ) {
                if( topic != null ) {
                    if( !qImgList.isEmpty() ) {
                        return true ;
                    }
                }
            }
        }
        return false ;
    }
    
    public String getHashCode() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( srcId ).append( ":" ) ;
        sb.append( qID.toString() ).append( ":" ) ;
        sb.append( topic.getId() ).append( ":" ) ;
        sb.append( answer ).append( ":" ) ;
        List<QuestionImage> qImgList = getQImgList() ;
        for( QuestionImage qImg : qImgList ) {
            sb.append( qImg.getImgFile().getName() ).append( ":" ) ;
        }
        return AppUtil.getHash( sb.toString() ) ;
    }
    
    public void setSyncInfo( int questionId, Date date, String hashCode ) {
        SyncInfo syncInfo = getSyncInfo() ;
        syncInfo.id = questionId ;
        syncInfo.syncTime = date ;
        syncInfo.syncToken = hashCode ;
    }
    
    public Date getSyncTime() {
        return getSyncInfo().syncTime ;
    }
}
