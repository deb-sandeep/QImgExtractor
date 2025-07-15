package com.sandy.sconsole.qimgextractor.qid.parser;

import com.sandy.sconsole.qimgextractor.qid.blueprint.QTypeSegBP;
import com.sandy.sconsole.qimgextractor.qid.blueprint.SubjectSegBP;

public class AITS_QIDParser extends QIDParser {

    private static QIDParser instance = null ;
    
    public static QIDParser getInstance() {
        if( instance == null ) {
            instance = new AITS_QIDParser();
        }
        return instance;
    }
    
    private AITS_QIDParser() {
        addSegmentBP( SubjectSegBP.INSTANCE ) ;
        addStrSegBP( "Section", new String[]{ "A", "B", "C" } ) ;
        addSegmentBP( QTypeSegBP.INSTANCE ) ;
        addIntSegBP( "QNo", 1, 1000 ) ;
    }
}
