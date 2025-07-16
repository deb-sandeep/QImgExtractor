package com.sandy.sconsole.qimgextractor.qid;

import com.sandy.sconsole.qimgextractor.qid.blueprint.ParseException;
import com.sandy.sconsole.qimgextractor.qid.parser.QIDParser;
import com.sandy.sconsole.qimgextractor.qid.parser.QIDParserFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QImgName {

    @Getter private String srcId = null ;
    @Getter private QID qid ;
    @Getter private int partNumber = -1 ;
    
    private QImgName() {}
    
    public QImgName( String imgName ) throws ParseException {
        String normalizedImgName = imgName ;
        
        normalizedImgName = stripImgExtension( normalizedImgName ) ;
        
        String[] parts = splitImgName( normalizedImgName ) ;
        srcId = parts[0] ;
        normalizedImgName = parts[1] ;

        partNumber = extractPartNumberIfPresent( normalizedImgName ) ;
        
        if( partNumber != -1 ) {
            normalizedImgName = normalizedImgName.substring( 0, normalizedImgName.indexOf( '(' ) ) ;
        }

        QIDParser parser = QIDParserFactory.getParser( srcId ) ;
        assert parser != null;
        qid = parser.parse( srcId, normalizedImgName ) ;
    }
    
    private String stripImgExtension( String imgName ) {
        if( imgName.endsWith( ".png" ) ) {
            return imgName.substring( 0, imgName.lastIndexOf( "." ) ) ;
        }
        return imgName ;
    }
    
    private String[] splitImgName( String imgName ) {
        if( imgName.contains( "." ) ) {
            String[] parts = imgName.split( "\\." ) ;
            if( parts.length > 2 ) {
                throw new IllegalArgumentException( "Image name contains more than 2 parts." ) ;
            }
            return parts ;
        }
        else {
            throw new IllegalArgumentException( "Image name is not in the format <src id>.<name>" ) ;
        }
    }
    
    private int extractPartNumberIfPresent( String imgName ) {
        if( imgName.contains( "(" ) ) {
            String partNumStr = imgName.substring( imgName.indexOf( "(" ) + 1, imgName.length()-1 ) ;
            return Integer.parseInt( partNumStr ) ;
        }
        return -1 ;
    }
    
    public String toString() {
        String partNo = "" ;
        if( partNumber != -1 ) {
            partNo = "(" + partNumber + ")" ;
        }
        return qid.toString() + partNo + ".png" ;
    }
    
    public QImgName getNextName() {
        QImgName nextName = null ;
        if( partNumber != -1 ) {
            nextName = new QImgName() ;
            nextName.srcId = srcId ;
            nextName.partNumber = partNumber + 1 ;
            nextName.qid = qid ;
            return nextName ;
        }
        else {
            QID nextQid = qid.rollForward() ;
            if( nextQid != null ) {
                nextName = new QImgName() ;
                nextName.srcId = srcId ;
                nextName.qid = nextQid ;
                return nextName ;
            }
        }
        return nextName ;
    }
}
