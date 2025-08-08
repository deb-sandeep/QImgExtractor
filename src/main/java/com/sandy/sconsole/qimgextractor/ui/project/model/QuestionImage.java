package com.sandy.sconsole.qimgextractor.ui.project.model;

import com.sandy.sconsole.qimgextractor.qsrc.QSrcFactory;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SelectedRegionMetadata;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Arrays;
import java.util.Stack;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getProjectModel;
import static com.sandy.sconsole.qimgextractor.ui.project.model.qid.ParserUtil.*;

@Data
@Slf4j
@EqualsAndHashCode( callSuper = false )
public class QuestionImage implements Comparable<QuestionImage> {
    
    private String srcId       = null ;
    private String subjectCode = null ;
    private int    partNumber  = -1 ;
    private int    pageNumber  = -1 ;
    private QID    qId         = null ;
    
    private final PageImage pageImg ; // Injected
    private File imgFile ; // Injected
    
    @Getter
    private final SelectedRegionMetadata imgRegionMetadata;
    
    public QuestionImage( PageImage pageImg,
                          File questionImgFile,
                          SelectedRegionMetadata imgRegionMetadata )  {
        this.pageImg = pageImg ;
        this.imgFile = questionImgFile;
        this.imgRegionMetadata = imgRegionMetadata;
        parseFileName( questionImgFile.getName() ) ;
    }
    
    public QuestionImage getClone() {
        return new QuestionImage( pageImg, imgFile, imgRegionMetadata ) ;
    }
    
    private void parseFileName( String fileName )
        throws IllegalArgumentException {
        
        String fName = fileName ;
        
        fName = stripFileExtension( fName ) ;// Returns the file name without the extension
        fName = collectPartNumber( fName ) ; // Returns the file name without the part number
        fName = collectSrcId( fName ) ;      // Returns the file name without the source id
        fName = collectPageNumber( fName ) ; // Returns the file name without the page number
        
        // Tokenize the remaining string into parts
        Stack<String> parts = parseToStack( fName ) ;
        
        this.subjectCode = parts.pop() ;
        validateSubjectCode( this.subjectCode ) ;
        
        this.qId = QSrcFactory.getQSrcComponentFactory( srcId )
                              .getNewQIDInstance( this ) ;
        this.qId.parse( parts ) ;
    }

    // A question image file name is of the format:
    // <srcId>.<pageNumber>.<tagName>[(<partNumber>)].png
    //
    // This function expects the tagName (with optional part number) to be
    // passed as parameter. If a file extension and part number are present,
    // they are ignored.
    public boolean isValidTagName( String tagName ) {
        
        // String off file extension if present
        tagName = stripFileExtension( tagName ) ;
        
        // Strip off the part number if present. QID doesn't
        // deal with part number.
        if( tagName.indexOf( '(' ) != -1 ) {
            tagName = tagName.substring( 0, tagName.indexOf( '(' ) ) ;
        }
        
        Stack<String> parts = parseToStack( tagName ) ;
        if( parts.size() < 2 ) {
            log.debug( "Tag name {} has less than 2 parts", tagName ) ;
            return false ;
        }
        
        // Pop the subject code
        parts.pop() ;
        return this.qId.isValid( parts ) ;
    }
    
    public void setNewTagName( String tagName ) {
    
        String fName = stripFileExtension( tagName ) ;
        fName = collectPartNumber( fName ) ;
        
        // Tokenize the remaining string into parts
        Stack<String> parts = parseToStack( fName ) ;
        
        this.subjectCode = parts.pop() ;
        this.qId.parse( parts ) ;
    }
    
    // Returns <tagName>[(<partNumber>)]
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
    
    // Returns <tagName>[(<partNumber>)].png
    public String getShortFileName() {
        return getShortFileNameWithoutExtension() + ".png" ;
    }
    
    // Returns <srcId>.<pageNum>.<tagName>[(<partNumber>)].png
    public String getLongFileName() {
        return srcId + "." +
               String.format( "%03d", pageNumber ) + "." +
               getShortFileName() ;
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
        this.partNumber = -1 ;
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
            fName = String.join( ".", Arrays.copyOfRange( parts, 1, parts.length ) ) ;
        }
        else {
            throw new IllegalArgumentException( "File name " + fName +
                    " does not have source ID." ) ;
        }
        return fName ;
    }
    
    private String collectPageNumber( String fileName ) {
        String fName = fileName ;
        if( fName.contains( "." ) ) {
            String[] parts = fName.split( "\\." ) ;
            this.pageNumber = Integer.parseInt( parts[0].trim() ) ;
            fName = parts[1].trim() ;
        }
        else {
            throw new IllegalArgumentException( "File name " + fName +
                    " does not have page number." ) ;
        }
        return fName ;
    }
    
    public boolean isPart() {
        return this.partNumber != -1 ;
    }
    
    public QuestionImage nextQuestion() {
        QuestionImage q = this.getClone() ;
        ProjectContext context = getProjectModel().getContext() ;
        
        if( q.isPart() ) {
            if( context.isPartSelectionModeEnabled() ) {
                if( context.isForceNextImgFlag() ) {
                    q.partNumber = 1 ;
                    q.getQId().incrementQuestionNumber() ;
                }
                else {
                    q.partNumber++ ;
                }
            }
            else {
                q.partNumber = -1 ;
                q.getQId().incrementQuestionNumber() ;
            }
        }
        else {
            q.getQId().incrementQuestionNumber() ;
            if( context.isPartSelectionModeEnabled() ) {
                q.partNumber = 1 ;
            }
        }
        return q ;
    }
    
    public void rollSubjectCode( boolean backward ) {
        int idx = SUBJECT_SEQ.indexOf( this.subjectCode ) ;
        if( !backward ) {
            if( idx == SUBJECT_SEQ.size() - 1 ) {
                this.subjectCode = SUBJECT_SEQ.get( 0 ) ;
            }
            else {
                this.subjectCode = SUBJECT_SEQ.get( idx+1 ) ;
            }
        }
        else {
            if( idx == 0 ) {
                this.subjectCode = SUBJECT_SEQ.get( SUBJECT_SEQ.size()-1 ) ;
            }
            else {
                this.subjectCode = SUBJECT_SEQ.get( idx-1 ) ;
            }
        }
        
        this.partNumber = -1 ;
        ProjectContext context = getProjectModel().getContext() ;
        context.setPartSelectionModeEnabled( false ) ;
    }
    
    public void mutatePartSequence( boolean endSequence ) {
        
        ProjectContext context = getProjectModel().getContext() ;
        if( endSequence ) {
            this.partNumber = -1 ;
            context.setPartSelectionModeEnabled( false ) ;
        }
        else {
            if( this.partNumber == -1 ) {
                this.partNumber = 1 ;
                context.setPartSelectionModeEnabled( true ) ;
            }
            else {
                this.partNumber++ ;
            }
        }
    }
    
    @Override
    public int compareTo( QuestionImage img ) {
        return (int)( this.imgFile.lastModified() - img.imgFile.lastModified() ) ;
    }
    
    public String toString() {
        StringBuilder sb = new StringBuilder( srcId + "//" + subjectCode + "/" + qId  ) ;
        if( partNumber != -1 ) {
            sb.append( "/(" ).append( partNumber ).append( ")" ) ;
        }
        return sb.toString() ;
    }
}
