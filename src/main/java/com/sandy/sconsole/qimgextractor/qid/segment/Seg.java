package com.sandy.sconsole.qimgextractor.qid.segment;

import com.sandy.sconsole.qimgextractor.qid.blueprint.SegBP;
import lombok.Getter;
import lombok.Setter;

public abstract class Seg<T> {

    protected final SegBP<?> blueprint ;
    
    @Getter @Setter
    private Seg<?> prevSeg = null ;
    
    @Getter @Setter
    private Seg<?> nextSeg = null ;
    
    public Seg( SegBP<?> blueprint ) {
        this.blueprint = blueprint ;
    }
    
    public abstract T getValue() ;
    
    
}
