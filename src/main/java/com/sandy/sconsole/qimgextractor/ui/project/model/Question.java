package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class Question extends QuestionImageCluster
    implements Comparable<Question> {
    
    private QuestionImageCluster lctCtxImgCluster = null ;
    
    @Setter
    private String answer = null ;
    
    @Setter
    private Topic topic = null ;

    Question( QID qID ) {
        super( qID ) ;
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
        return qID.getParent().getSrcId() + "//" + qID ;
    }
    
    JSONObject getSerializedForm() throws Exception {
        JSONObject json = new JSONObject() ;
        json.put( "qid", qID.toString() ) ;
        json.put( "lctSeq", qID.getLctSequence() ) ;
        json.put( "lctCtxImages", getImgInfoList( lctCtxImgCluster ) ) ;
        json.put( "questionImages", getImgInfoList( this ) ) ;
        json.put( "answer", answer ) ;
        json.put( "topic", topic ) ;
        return json ;
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
}
