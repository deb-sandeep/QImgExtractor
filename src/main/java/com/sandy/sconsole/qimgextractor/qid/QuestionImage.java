package com.sandy.sconsole.qimgextractor.qid;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import static com.sandy.sconsole.qimgextractor.qid.ParserUtil.*;

// <srcId>.[P|M|C]_[qType]_<LCT#>_{[qID]}<(n)>.png
//
// [srcId]      - Identifier of the source from which the image is extracted,
//                for example, AITS-18-A-CT1P2. This source contains multiple image pages.
// [subjectInd] - Subject indicator [P|M|C] 
// [qType]      - Question type [SCA|MCA|MMT|NT|LCT]
// <LCT#>       - If qType is LCT, this will contain the sequence of LCT
// {qId}        - Based on the bookCode, this is overridden. This can have 
//                multiple parts
// (n)          - Part number
@Data
@EqualsAndHashCode( callSuper = false )
@Slf4j
public class QuestionImage implements Comparable<QuestionImage> {
    
    public static final String SCA = "SCA" ;
    public static final String MCA = "MCA" ;
    public static final String MMT = "MMT" ;
    public static final String IVT = "IVT"  ;
    public static final String NVT = "NVT"  ;
    public static final String LCT = "LCT" ;
    
    private static final int SUB_IDX = 0 ;
    private static final int QTYPE_IDX = 1 ;
    private static final int LCT_NO_IDX = 2 ;
    
    static List<String> Q_TYPE_SEQ = Arrays.asList( SCA, MCA, LCT, IVT, NVT ) ;
    static List<String> SUB_SEQ = Arrays.asList( "P", "C", "M" ) ;
    
    private boolean isLCTContext = false ;
    
    private String srcId        = null ;
    private String subjectCode  = null ;
    private String questionType = null ;
    private int    lctSequence  = -1 ;
    private QID    qId          = null ;
    private int    partNumber   = -1 ;
    
    private File imgFile ;
    
    public QuestionImage( File file ) {
        this.imgFile = file ;
        parseFileName( this.imgFile.getName() ) ;
    }
    
    public QuestionImage getClone() {
        File file = new File( imgFile.getParent(), getFileName() ) ;
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
            // LCT contexts do not have a QID. QID comes for the questions
            // to which LCT context gets attached to.
            parseBookSpecificQuestionId( parts ) ;
        }
    }
    
    private void parseBookSpecificQuestionId( Stack<String> parts ) {
        if( this.srcId.startsWith( "AITS" ) ) {
            this.qId = new AITS_QID( this, parts ) ;
        }
        else {
            throw new IllegalArgumentException( 
                    "Source " + this.srcId + " not recognized." ) ;
        }
    }
    
    public String getFileName() {
        StringBuilder sb = new StringBuilder() ;
        
        sb.append( this.srcId ).append( '.' )
          .append( this.subjectCode ).append( "_" )
          .append( this.questionType ).append( "_" ) ;
        
        if( isLCT() ) {
            sb.append( this.lctSequence ).append( '_' ) ;
            if( isLCTContext ) {
                sb.append( "Ctx" ) ;
            }
            else {
                sb.append( this.qId.getFilePartName() ) ;
            }
        }
        else {
            sb.append( this.qId.getFilePartName() ) ;
        }
        
        if( this.partNumber != -1 ) {
            sb.append( "(" )
              .append( this.partNumber )
              .append( ")" );
        }
        sb.append( ".png" ) ;
        
        return sb.toString() ;
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
    
    public boolean isLCT() {
        return this.questionType.equals( LCT ) ;
    }
    
    public QuestionImage nextQuestion() {
        QuestionImage q = this.getClone() ;
        if( q.isPart() ) {
            q.partNumber++ ;
        }
        else if( !q.isLCTContext ) {
            q.getQId().incrementQuestionNumber() ;
        }
        return q ;
    }
    
    public static void main( String[] args ) {
        
        String[] ids = {
            "AITS-25-M-FT9.P_SCA_1.png",
            "AITS-25-M-FT9.P_MCA_5.png",
            "AITS-25-M-FT9.P_LCT_1_Ctx(1).png",
            "AITS-25-M-FT9.P_LCT_1_Ctx(2).png",
            "AITS-25-M-FT9.P_LCT_1_1.png",
        } ;
        
        QuestionImage q ;
        for( String id : ids ) {
            System.out.println( id ) ;
            File file = new File( id ) ;
            q = new QuestionImage( file ) ;
            System.out.println( "\t" + q.getFileName() + " : " + id.equals( q.getFileName() ) ) ;

            q = q.nextQuestion() ;
            System.out.println( "\t" + q.getFileName() ) ;
        }
    }
    
    @Override
    public int compareTo( QuestionImage img ) {
        return (int)(this.imgFile.lastModified() - img.imgFile.lastModified()) ;
    }
}
