package com.sandy.sconsole.qimgextractor.qid.blueprint;

import com.sandy.sconsole.qimgextractor.qid.segment.QTypeSeg;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class QTypeSegBP extends SegBP<QTypeSeg> {
    
    private static final List<String> Q_TYPES =
            Arrays.asList( "SCA", "MCA", "LCT", "NVT", "IVT", "MMT" ) ;
    
    public static final QTypeSegBP INSTANCE = new QTypeSegBP() ;
    
    private QTypeSegBP() {
        super( "Question Type" ) ;
    }
    
    @Override
    public QTypeSeg parse( Stack<String> partStack )
        throws ParseException {
        
        String type = partStack.pop() ;
        
        if( Q_TYPES.contains( type ) ) {
            if( type.equals( "LCT" ) ) {
                String lctNoStr = partStack.pop() ;
                if( lctNoStr.length() > 1 ) {
                    throw new ParseException( this, lctNoStr + " (LCT no) should be " +
                            "one character in length." ) ;
                }
                return new QTypeSeg( this, type, lctNoStr.charAt( 0 ) ) ;
            }
            else {
                return new QTypeSeg( this, type ) ;
            }
        }
        else {
            throw new ParseException( this, type + " is not a valid question type. " +
                    "Valid types are " + Q_TYPES ) ;
        }
    }
}
