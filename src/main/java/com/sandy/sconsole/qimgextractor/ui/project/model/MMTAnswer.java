package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.extern.slf4j.Slf4j;

import java.util.*;

@Slf4j
public class MMTAnswer {
    
    private final String[] rowNames = new String[4] ;
    private String[] colNames = null ;
    
    private boolean[][] ansMatrix = null ;
    
    /**
     * MMTAnswer expects a specially formatted text string as per the
     * following protocol.
     * <p>
     * 1. The string consists of four # delimited substrings (called part)
     * 2. Each part has a first character which is [A-D] and represents the row
     *    of matrix
     * 3. The remaining part string is comma-delimited, and each subpart
     *    represents a column.
     * <p>
     * eg. The following sequences are valid.
     * <p>
     * Ap,q,r#Bs#Cq,s#Dp
     * A1,2,3#B4#C2,4#D1
     * @throws Question.InvalidAnswerException In case the input string
     * does not conform to the syntax.
     */
    MMTAnswer( String text ) throws Question.InvalidAnswerException {
        parseInputText( text ) ;
    }
    
    public void parseInputText( String text )
        throws Question.InvalidAnswerException {
        
        log.debug( "Parsing MMT input text: {}", text ) ;
        String[] parts = text.split( "#" ) ;
        if( parts.length != 4 ) {
            throw new Question.InvalidAnswerException(
                    "Invalid answer format. Expected 4 parts, got " + parts.length ) ;
        }
        extractRowColNames( parts ) ;
        
        log.debug( "Populating answer matrix" ) ;
        for( String part : parts ) {
            log.debug( "  Processing part : {} ", part ) ;
            populateAnsMatrix( part ) ;
        }
    }
    
    private void extractRowColNames( String[] parts )
        throws Question.InvalidAnswerException {
        
        log.debug( "Extracting row and column names" ) ;
        List<String> colNamesList = new ArrayList<>() ;
        for( int i = 0; i < 4; i++ ) {
            String part = parts[i] ;
            log.debug( "  Processing part : {} ", part ) ;
            
            char rowName = part.charAt( 0 ) ;
            rowNames[i] = Character.toString( rowName ) ;
            log.debug( "    Extracted row name: {} ", rowNames[i] ) ;
            
            if( "ABCD".indexOf( rowName ) < 0 ) {
                throw new Question.InvalidAnswerException(
                        "Invalid answer format. Expected row name to be one of [A-D], got " + rowName ) ;
            }
            
            String[] subParts = part.substring( 1 ).toLowerCase().split( "," ) ;
            if( subParts.length > 5 ) {
                throw new Question.InvalidAnswerException(
                        "Invalid answer format. Expected at most 4 sub-parts, got " + subParts.length ) ;
            }
            
            for( String subPart : subParts ) {
                subPart = subPart.trim() ;
                if( !subPart.isEmpty() && !colNamesList.contains( subPart ) ) {
                    log.debug( "    Extracted column name: {} ", subPart ) ;
                    if( subPart.length() != 1 && "pqrs".indexOf( subPart.charAt( 0 ) ) < 0 ) {
                        throw new Question.InvalidAnswerException( "Invalid column name found" + subPart ) ;
                    }
                    colNamesList.add( subPart ) ;
                }
            }
        }

        if( colNamesList.size() > 5 ) {
            throw new Question.InvalidAnswerException(
                    "Invalid answer format. Expected 4 or 5 unique column names, got " + colNamesList.size() ) ;
        }
        Collections.sort( colNamesList ) ;
        colNames = new String[colNamesList.size()] ;
        for( int i = 0; i < colNamesList.size(); i++ ) {
            colNames[i] = colNamesList.get( i ) ;
        }
        log.debug( "Row names: {}, Col names: {}", Arrays.toString( rowNames ), Arrays.toString( colNames ) ) ;
        
        this.ansMatrix = new boolean[rowNames.length][colNames.length] ;
    }
    
    private void populateAnsMatrix( String part ) {
        
        String rowName = part.substring( 0, 1 ) ;
        String[] subParts = part.substring( 1 ).toLowerCase().split( "," ) ;
        int rowIndex = getRowIndex( rowName ) ;
        
        for( String subPart : subParts ) {
            subPart = subPart.trim() ;
            if( !subPart.isEmpty() ) {
                int colIndex = getColIndex( subPart ) ;
                log.debug( "    Setting answer matrix: row: {}, col: {} to true.", rowIndex, colIndex ) ;
                this.ansMatrix[rowIndex][colIndex] = true ;
            }
        }
    }
    
    private int getRowIndex( String rowName ) {
        for( int i = 0; i < rowNames.length; i++ ) {
            if( rowNames[i].equals( rowName ) ) {
                return i ;
            }
        }
        return -1 ;
    }
    
    private int getColIndex( String colName ) {
        for( int i = 0; i < colNames.length; i++ ) {
            if( colNames[i].equals( colName ) ) {
                return i ;
            }
        }
        return -1 ;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder() ;
        for( int rowNum = 0; rowNum < rowNames.length; rowNum++ ) {
            sb.append( rowNames[rowNum] ) ;
            for( int colName = 0; colName < colNames.length; colName++ ) {
                if( ansMatrix[rowNum][colName] ) {
                    sb.append( colNames[colName] ) ;
                    sb.append( "," ) ;
                }
            }
            
            if( sb.charAt( sb.length()-1 ) == ',' ) {
                sb.deleteCharAt( sb.length()-1 ) ;
            }
            
            if( rowNum < 3 ) {
                sb.append( "#" ) ;
            }
        }
        return sb.toString() ;
    }
    
    public int getNumCols() {
        return colNames.length ;
    }
    
    public int getNumRows() {
        return rowNames.length ;
    }
    
    public boolean isCorrectMapping( int rowNum, int colNum ) {
        if( rowNum >= 0 && rowNum < rowNames.length ) {
            if( colNum >= 0 && colNum < colNames.length ) {
                return ansMatrix[rowNum][colNum] ;
            }
        }
        return false ;
    }
}
