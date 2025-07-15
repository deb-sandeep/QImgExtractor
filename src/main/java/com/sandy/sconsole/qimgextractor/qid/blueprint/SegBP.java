package com.sandy.sconsole.qimgextractor.qid.blueprint;

import lombok.Setter;

public class SegBP {

    private final String segmentName ;
    
    @Setter private SegBP parent ;
    @Setter private SegBP child ;
    
    public SegBP( String name ) {
        this.segmentName = name ;
    }
}
