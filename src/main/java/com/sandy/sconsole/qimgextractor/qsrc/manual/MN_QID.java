package com.sandy.sconsole.qimgextractor.qsrc.manual;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Stack;

/**
 * A manual QID is of the form:
 * <p>
 * <subjectCode>_<bookCode>_<chapterNum>_<exerciseName>_<questionType>_<questionNumber>
 */
@EqualsAndHashCode(callSuper = false)
@Slf4j
public class MN_QID extends QID {
    
    @Getter private String bookCode ;
    @Getter private int chapterNum ;
    @Getter private String exerciseName ;
    
    public MN_QID( QuestionImage qImg ) {
        super( qImg ) ;
    }
    
    public void parse( Stack<String> parts ) {
        
        this.bookCode = parts.pop() ;
        this.chapterNum = Integer.parseInt( parts.pop() ) ;
        this.exerciseName = parts.pop() ;
        super.parseQTypeAndNumber( parts ) ;
    }
    
    public boolean isValid( Stack<String> parts ) {
        if( parts.size() != 5 ) {
            log.debug( "MN QID is not valid. Expected 5 parts, got: {}", parts.size() ) ;
            return false ;
        }
        
        // Pop the book code
        parts.pop() ;
        
        // Pop the chapter number
        String temp = parts.pop() ;
        try {
            Integer.parseInt( temp ) ;
        }
        catch( NumberFormatException e ) {
            log.debug( "MN QID is not valid. Expected chapter number to be an integer, got: {}", temp ) ;
            return false ;
        }
        
        // Pop the exercise name
        parts.pop() ;
        
        // Let the super class validate the rest of the parts
        return super.validateQTypeAndNumber( parts ) ;
    }
    
    public String getFilePartName() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( bookCode ).append( "_" ) ;
        sb.append( String.format( "%02d", chapterNum ) ).append( "_" ) ;
        sb.append( exerciseName ).append( "_" ) ;
        sb.append( super.getFilePartName() ) ;
        return sb.toString() ;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        sb.append( getSubjectCode() ).append( "/" ) ;
        sb.append( bookCode ).append( "/" ) ;
        sb.append( String.format( "%02d", chapterNum ) ).append( "/" ) ;
        sb.append( exerciseName ).append( "/" ) ;
        sb.append( questionType ).append( '/' ) ;
        if( isLCT() ) {
            sb.append( lctSequence ).append( '/' ) ;
        }
        if( !isLCTContext ) {
            sb.append( questionNumber ) ;
        }
        else {
            sb.append( "Ctx" ) ;
        }
        return sb.toString() ;
    }
    
    @Override
    public int compareTo( QID qId ) {
        if( !( qId instanceof MN_QID mnQID ) ) {
            return super.compareTo( qId ) ;
        }
        
        if( bookCode.equals( mnQID.bookCode ) ) {
            if( chapterNum == mnQID.chapterNum ) {
                if( exerciseName.equals( mnQID.exerciseName ) ) {
                    return super.compareTo( qId ) ;
                }
                return exerciseName.compareTo( mnQID.exerciseName ) ;
            }
            return chapterNum - mnQID.chapterNum ;
        }
        return bookCode.compareTo( mnQID.bookCode ) ;
    }
}
