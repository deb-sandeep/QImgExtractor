package com.sandy.sconsole.qimgextractor.qid.parser;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.segment.Seg;
import com.sandy.sconsole.qimgextractor.qid.blueprint.IntSegBP;
import com.sandy.sconsole.qimgextractor.qid.blueprint.SegBP;
import com.sandy.sconsole.qimgextractor.qid.blueprint.ParseException;
import com.sandy.sconsole.qimgextractor.qid.blueprint.StringSegBP;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class QIDParser {
    
    protected List<SegBP<?>> segBlueprints = new ArrayList<>() ;
    
    protected void addSegmentBP( SegBP<?> segmentBP ) {
        segBlueprints.add( segmentBP ) ;
        if( segBlueprints.size() > 1 ) {
            SegBP<?> parent = segBlueprints.get( segBlueprints.size()-2 ) ;
            segmentBP.setParent( parent ) ;
            parent.setChild( segmentBP ) ;
        }
    }
    
    protected void addStrSegBP( String name, String[] values ) {
        addSegmentBP( new StringSegBP( name, values ) ) ;
    }
    
    protected void addIntSegBP( String name, int min, int max ) {
        addSegmentBP( new IntSegBP( name, min, max ) ) ;
    }
    
    public QID parse( String srcId, String imgName ) throws ParseException {
        
        String[] parts = imgName.split( "_" ) ;
        Stack<String> partStack = new Stack<>() ;
        for( int i=parts.length-1; i>=0; i-- ) {
            partStack.push( parts[i] ) ;
        }
        
        QID qid = new QID( srcId ) ;
        SegBP<?> segBP = segBlueprints.get( 0 ) ;
        try {
            while( segBP != null ) {
                if( partStack.isEmpty() ) {
                    throw new ParseException( segBP, "Image name " + imgName +
                            " does not have any parts left to parse." ) ;
                }
                Seg<?> seg = ( Seg<?> )segBP.parse( partStack );
                qid.addSegment( seg ) ;
                segBP = segBP.getChild() ;
            }
        }
        catch( ParseException e ) {
            throw e ;
        }
        catch( Exception e ) {
            throw new ParseException( segBP, "Exception : " + e.getMessage() ) ;
        }
        return qid ;
    }
}
