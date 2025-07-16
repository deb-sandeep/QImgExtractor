package com.sandy.sconsole.qimgextractor.qid.segment;

import com.sandy.sconsole.qimgextractor.qid.blueprint.StringSegBP;

public class StringSeg extends Seg<String> {

    protected String value ;
    
    public StringSeg( StringSegBP blueprint, String value ) {
        super( blueprint ) ;
        this.value = value ;
    }
    
    @Override
    public String getValue() {
        return value ;
    }
}
