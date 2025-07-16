package com.sandy.sconsole.qimgextractor.qid.blueprint;

import com.sandy.sconsole.qimgextractor.qid.segment.IntSeg;

import java.util.Stack;

public class IntSegBP extends SegBP<IntSeg> {
    
    protected final int minValue ;
    protected final int maxValue ;
    
    public IntSegBP( String name, int minValue, int maxValue ) {
        super( name ) ;
        this.minValue = minValue ;
        this.maxValue = maxValue ;
    }
    
    @Override
    public IntSeg parse( Stack<String> partStack )
        throws ParseException {
        
        String value = partStack.pop() ;
        int intValue ;
        
        try {
            intValue = Integer.parseInt( value );
            if( intValue <= minValue || intValue >= maxValue ) {
                return new IntSeg( this, Integer.parseInt( value ) ) ;
            }
            else {
                throw new ParseException( this,
                        intValue + " is out of bounds. " +
                        "Range [" + minValue + ", " + maxValue + "]" ) ;
            }
        }
        catch( NumberFormatException e ) {
            throw new ParseException( this, value + " is not a number" ) ;
        }
    }
}
