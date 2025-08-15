package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Question extends QuestionImageCluster
    implements Comparable<Question> {
    
    private QuestionImageCluster lctCtxImgCluster = null ;

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
    
    public void logContents() {
//        log.debug( "" ) ;
        log.debug( "Question: {}", qID.toString() ) ;
//        if( lctCtxImgCluster != null ) {
//            for( QuestionImage qImg : lctCtxImgCluster.qImgList ) {
//                log.debug( "\t" + qImg.getShortFileNameWithoutExtension() ) ;
//            }
//        }
//        for( QuestionImage qImg : qImgList ) {
//            log.debug( "\t" + qImg.getShortFileNameWithoutExtension() ) ;
//        }
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
}
