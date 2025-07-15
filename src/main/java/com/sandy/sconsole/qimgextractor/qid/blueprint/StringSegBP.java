package com.sandy.sconsole.qimgextractor.qid.blueprint;

import java.util.Arrays;
import java.util.List;

public class StringSegBP extends SegBP {
    
    private final List<String> values ;
    
    public StringSegBP( String name, String[] values ) {
        super( name ) ;
        this.values = Arrays.asList( values ) ;
    }

    public StringSegBP( String name, List<String> values ) {
        super( name ) ;
        this.values = values ;
    }
    
}
