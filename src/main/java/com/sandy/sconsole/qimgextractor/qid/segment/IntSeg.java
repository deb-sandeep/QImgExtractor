package com.sandy.sconsole.qimgextractor.qid.segment;

import com.sandy.sconsole.qimgextractor.qid.blueprint.IntSegBP;

public class IntSeg extends Seg<Integer> {

    protected Integer value ;
    
    public IntSeg( IntSegBP blueprint, Integer value ) {
        super( blueprint ) ;
        this.value = value ;
    }
    
    @Override
    public Integer getValue() {
        return value ;
    }
}
