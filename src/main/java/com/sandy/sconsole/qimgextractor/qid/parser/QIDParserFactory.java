package com.sandy.sconsole.qimgextractor.qid.parser;

public class QIDParserFactory {

    public static QIDParser getParser( String qid ) {
        if( qid.startsWith( "AITS" ) ) {
            return AITS_QIDParser.getInstance() ;
        }
        return null ;
    } ;
}
