package com.sandy.sconsole.qimgextractor.qid;

import com.sandy.sconsole.qimgextractor.qsrc.QSrcFactory;
import com.sandy.sconsole.qimgextractor.qsrc.aits.AITS_QID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getProjectContext;
import static com.sandy.sconsole.qimgextractor.qid.ParserUtil.*;

@Data
@EqualsAndHashCode( callSuper = false )
@Slf4j
public class QuestionImage implements Comparable<QuestionImage> {
    
    static List<String> SUB_SEQ = Arrays.asList( "P", "C", "M" ) ;
    
    private String srcId       = null ;
    private String subjectCode = null ;
    private int    partNumber  = -1 ;
    private QID    qId         = null ;
    
    private File imgFile ;
    
    public QuestionImage( File file ) {
        this.imgFile = file ;
        parseFileName( this.imgFile.getName() ) ;
    }
    
    public QuestionImage getClone() {
        File file = new File( imgFile.getParent(), getLongFileName() ) ;
        return new QuestionImage( file ) ;
    }
    
    private void parseFileName( String fileName ) 
        throws IllegalArgumentException {
        
        checkNullFile( this.imgFile ) ;
        
        String fName = fileName ;
        
        fName = stripFileExtension( fName ) ;
        fName = collectPartNumber( fName ) ; // Returns the file name without the part number
        fName = collectSrcId( fName ) ;
        
        // At this point, the file name has been stripped off any file extension,
        // any part numbers (if present) and source id.
        
        // Tokenize the remaining string into parts
        Stack<String> parts = parseToStack( fName ) ;
        
        this.subjectCode = parts.pop() ;
        validateSubjectCode( this.subjectCode ) ;
        
        this.qId = QSrcFactory.getQSrcComponentFactory( srcId )
                              .getNewQIDInstance( this ) ;
        this.qId.parse( parts ) ;
    }
    
    public String getShortFileNameWithoutExtension() {
        
        StringBuilder sb = new StringBuilder() ;
        
        sb.append( this.subjectCode ).append( "_" )
          .append( this.qId.getFilePartName() ) ;
        
        if( this.partNumber != -1 ) {
            sb.append( "(" )
              .append( this.partNumber )
              .append( ")" ) ;
        }
        return sb.toString() ;
    }
    
    public String getShortFileName() {
        return getShortFileNameWithoutExtension() + ".png" ;
    }
    
    public String getLongFileName() {
        return this.srcId + "." + getShortFileName() ;
    }
    
    private String stripFileExtension( String fileName ) {
        String fName = fileName ;
        if( fName.endsWith( ".png" ) ) {
            fName = fileName.substring( 0, fileName.length()-4 ) ;
        }
        return fName ;
    }
    
    // It is assumed that the file name's extension has been stripped
    private String collectPartNumber( String fileName ) {
        String fName = fileName ;
        if( fName.contains( "(" ) ) {
            int startIndex = fName.indexOf( "(" ) ;
            int endIndex   = fName.indexOf( ")", startIndex ) ;
            
            String partNumStr = fName.substring( startIndex+1, endIndex ) ;
            this.partNumber = Integer.parseInt( partNumStr ) ;
            
            fName = fName.substring( 0, startIndex ) ;
        }
        return fName ;
    }
    
    // It is assumed that the file name's extension has been stripped
    private String collectSrcId( String fileName ) {
        String fName = fileName ;
        if( fName.contains( "." ) ) {
            String[] parts = fName.split( "\\." ) ;
            this.srcId = parts[0].trim() ;
            fName = parts[1].trim() ;
        }
        else {
            throw new IllegalArgumentException( "File name " + fName +
                    " does not have source ID." ) ;
        }
        return fName ;
    }
    
    public boolean isPart() {
        return this.partNumber != -1 ;
    }
    
    public QuestionImage nextQuestion() {
        QuestionImage q = this.getClone() ;
        if( q.isPart() ) {
            q.partNumber++ ;
        }
        else {
            q.getQId().incrementQuestionNumber() ;
        }
        return q ;
    }
    
    @Override
    public int compareTo( QuestionImage img ) {
        return (int)(this.imgFile.lastModified() - img.imgFile.lastModified()) ;
    }
    
    public void rollForwardSubjectCode() {
        int idx = SUB_SEQ.indexOf( this.subjectCode ) ;
        if( idx == SUB_SEQ.size() - 1 ) {
            this.subjectCode = SUB_SEQ.get( 0 ) ;
        }
        else {
            this.subjectCode = SUB_SEQ.get( idx+1 ) ;
        }
    }
}
