package com.sandy.sconsole.qimgextractor.qid.parser;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.blueprint.IntSegBP;
import com.sandy.sconsole.qimgextractor.qid.blueprint.SegBP;
import com.sandy.sconsole.qimgextractor.qid.blueprint.StringSegBP;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public abstract class QIDParser {
    
    protected List<SegBP> segBlueprints = new ArrayList<SegBP>() ;
    
    protected void addSegmentBP( SegBP segmentBP ) {
        segBlueprints.add( segmentBP ) ;
        if( segBlueprints.size() > 1 ) {
            SegBP parent = segBlueprints.get( segBlueprints.size()-2 ) ;
            segmentBP.setParent( parent ) ;
            parent.setChild( segmentBP ) ;
        }
    }
    
    protected void addStrSegBP( String name, String[] values ) {
        addSegmentBP( new StringSegBP( name, values ) ); ;
    }
    
    protected void addIntSegBP( String name, int min, int max ) {
        addSegmentBP( new IntSegBP( name, min, max ) ) ;
    }
    
    public QID parse( String srcId, String imgName ) {
        
        String[] parts = imgName.split( "_" ) ;
        Stack<String> partStack = new Stack<>() ;
        for( int i=parts.length-1; i>=0; i-- ) {
            partStack.push( parts[i] ) ;
        }
        
        SegBP segBP = segBlueprints.get( 0 ) ;
        while( segBP != null ) {
        
        }
        
        return null ;
    }
}
