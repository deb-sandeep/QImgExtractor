package com.sandy.sconsole.qimgextractor.qid;

import com.sandy.sconsole.qimgextractor.qid.segment.Seg;

import java.util.ArrayList;
import java.util.List;

public class QID {
    
    private String srcId ;
    
    private final List<Seg<?>> segments = new ArrayList<>() ;
    
    private QID(){} ;
    
    public QID( String srcId ) {
        this.srcId = srcId;
    }
    
    public void addSegment( Seg<?> segment ) {
        segments.add( segment ) ;
        if( segments.size() > 1 ) {
            Seg<?> lastSegment = segments.get( segments.size() - 2 ) ;
            segment.setPrevSeg( lastSegment ) ;
            lastSegment.setNextSeg( segment ) ;
        }
    }
    
    public String toString() {
        return srcId + "." + generateImgName() ;
    }
    
    private String generateImgName() {
        StringBuilder sb = new StringBuilder() ;
        segments.forEach( seg -> {
            sb.append( seg.getValue().toString() ).append( "_" ) ;
        } ) ;
        sb.deleteCharAt( sb.length() - 1 ) ;
        return sb.toString() ;
    }
    
    public QID rollForward() {
        return null ;
    }
}
