package com.sandy.sconsole.qimgextractor.qid.blueprint;

import com.sandy.sconsole.qimgextractor.qid.segment.StringSeg;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

public class StringSegBP extends SegBP<StringSeg> {
    
    protected final List<String> acceptableValues ;
    
    public StringSegBP( String name, String... acceptableValues ) {
        super( name ) ;
        this.acceptableValues = Arrays.asList( acceptableValues ) ;
    }

    @Override
    public StringSeg parse( Stack<String> partStack )
        throws ParseException {
        
        String value = partStack.pop() ;
        if( acceptableValues.contains( value ) ) {
            return new StringSeg( this, value ) ;
        }
        throw new ParseException( this, value + " not in " +
                "the acceptable list of values: " + acceptableValues ) ;
    }
}
