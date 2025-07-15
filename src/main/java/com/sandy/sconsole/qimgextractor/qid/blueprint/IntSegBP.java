package com.sandy.sconsole.qimgextractor.qid.blueprint;

public class IntSegBP extends SegBP {
    
    private final int minValue ;
    private final int maxValue ;
    
    public IntSegBP( String name, int minValue, int maxValue ) {
        super( name ) ;
        this.minValue = minValue ;
        this.maxValue = maxValue ;
    }
}
