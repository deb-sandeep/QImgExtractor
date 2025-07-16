package com.sandy.sconsole.qimgextractor.qid.blueprint;

import lombok.Getter;
import lombok.Setter;

import java.util.Stack;

public abstract class SegBP<T> {

    @Getter
    protected final String name;
    
    @Setter
    protected SegBP<?> parent ;
    
    @Setter @Getter
    protected SegBP<?> child ;
    
    protected SegBP( String name ) {
        this.name = name ;
    }
    
    public abstract T parse( Stack<String> partStack )
            throws ParseException ;
}
