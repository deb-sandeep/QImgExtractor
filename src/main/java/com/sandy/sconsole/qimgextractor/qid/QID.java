package com.sandy.sconsole.qimgextractor.qid;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getProjectContext;
import static com.sandy.sconsole.qimgextractor.qid.ParserUtil.getInt;
import static com.sandy.sconsole.qimgextractor.qid.ParserUtil.validateQuestionType;

public abstract class QID {
    
    private static final String SCA = "SCA" ;
    private static final String MCA = "MCA" ;
    private static final String LCT = "LCT" ;
    private static final String IVT = "IVT" ;
    private static final String NVT = "NVT" ;
    private static final String MMT = "MMT" ;
    
    static List<String> Q_TYPE_SEQ = Arrays.asList( SCA, MCA, LCT, IVT, NVT, MMT ) ;

    public static final List<String> COMMON_SAVE_HELP_CONTENTS = Arrays.asList(
        "----------- Save shortcuts --",
        "Ctrl+1 - Increment Subject Code",
        "Ctrl+2 - Increment QType"
    ) ;
    protected QuestionImage parent ;
    
    private String questionType = null ;
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
    
    public void rollForwardQType() {
        int idx = Q_TYPE_SEQ.indexOf( this.questionType ) ;
        if( idx == Q_TYPE_SEQ.size() - 1 ) {
            this.questionType = Q_TYPE_SEQ.get( 0 ) ;
        }
        else {
            this.questionType = Q_TYPE_SEQ.get( idx+1 ) ;
        }
        
        if( this.questionType.equals( LCT ) ) {
            this.isLCTContext = true ;
            this.lctSequence = getProjectContext().getLastLCTSequence() + 1 ;
        }
        else {
            this.isLCTContext = false ;
            this.lctSequence = -1 ;
        }
    }
}
