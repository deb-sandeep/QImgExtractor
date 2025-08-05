package com.sandy.sconsole.qimgextractor.ui.project.model.qid;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getProjectModel;
import static com.sandy.sconsole.qimgextractor.ui.project.model.qid.ParserUtil.getInt;
import static com.sandy.sconsole.qimgextractor.ui.project.model.qid.ParserUtil.validateQuestionType;

public abstract class QID {
    
    private static final String SCA = "SCA" ;
    private static final String MCA = "MCA" ;
    private static final String LCT = "LCT" ;
    private static final String IVT = "IVT" ;
    private static final String NVT = "NVT" ;
    private static final String MMT = "MMT" ;
    
    static List<String> Q_TYPE_SEQ = Arrays.asList( SCA, MCA, LCT, IVT, NVT, MMT ) ;

    @Getter
    protected QuestionImage parent ;
    
    @Getter
    private String questionType = null ;
    
    @Getter
    private boolean isLCTContext = false ;
    
    @Getter
    private int lctSequence    = -1 ;
    
    @Getter
    private int questionNumber = -1 ;
    
    protected QID( QuestionImage qImg ){
        this.parent = qImg ;
    }
    
    public abstract void parse( Stack<String> parts ) ;

    protected void parseQTypeAndNumber( Stack<String> parts ) {
        
        this.questionType = parts.pop() ;
        validateQuestionType( this.questionType ) ;
        
        if( this.questionType.equals( LCT ) ) {
            this.lctSequence = getInt( "LCT sequence", parts.pop() ) ;
            if( parts.peek().equals( "Ctx" ) ) {
                isLCTContext = true ;
                parts.pop() ;
            }
        }
        
        if( !isLCTContext ) {
            String part = parts.pop() ;
            try {
                questionNumber = Integer.parseInt( part ) ;
            }
            catch( NumberFormatException e ) {
                throw new IllegalArgumentException(
                        "AITS question numner " + part + " is not a number" ) ;
            }
        }
    }
    
    public void incrementQuestionNumber() {
        if( !isLCTContext ) {
            this.questionNumber += 1 ;
        }
    }
    
    public String getFilePartName() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( this.questionType ).append( "_" ) ;
        
        if( isLCT() ) {
            sb.append( this.lctSequence ).append( '_' ) ;
            if( isLCTContext ) {
                sb.append( "Ctx" ) ;
            }
        }
        
        if( !isLCTContext ) {
            sb.append( questionNumber ) ;
        }
        return sb.toString() ;
    }
    
    public boolean isLCT() {
        return this.questionType.equals( LCT ) ;
    }
    
    public void rollQType( boolean reverse ) {
        
        int idx = Q_TYPE_SEQ.indexOf( this.questionType ) ;
        if( reverse ) {
            if( idx == 0 ) {
                this.questionType = Q_TYPE_SEQ.get( Q_TYPE_SEQ.size()-1 ) ;
            }
            else {
                this.questionType = Q_TYPE_SEQ.get( idx-1 ) ;
            }
        }
        else {
            if( idx == Q_TYPE_SEQ.size() - 1 ) {
                this.questionType = Q_TYPE_SEQ.get( 0 ) ;
            }
            else {
                this.questionType = Q_TYPE_SEQ.get( idx+1 ) ;
            }
        }
        
        if( this.questionType.equals( LCT ) ) {
            this.isLCTContext = true ;
            this.lctSequence = getProjectModel().getContext().getLastLCTSequence() + 1 ;
        }
        else {
            this.isLCTContext = false ;
            this.lctSequence = -1 ;
        }
        
        this.questionNumber = getProjectModel().getContext().getLastSavedImg().getQId().getQuestionNumber() + 1 ;
        this.parent.setPartNumber( -1 ) ;
    }
    
    public void rollQuestionNumber( boolean reverse ) {
        if( reverse ) {
            if( this.questionNumber > 1 ) {
                this.questionNumber -= 1 ;
            }
        }
        else {
            this.questionNumber += 1 ;
        }
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( questionType ).append( '/' ) ;
        if( isLCT() ) {
            sb.append( lctSequence ).append( '/' ) ;
        }
        sb.append( questionNumber ) ;
        return sb.toString() ;
    }
}
