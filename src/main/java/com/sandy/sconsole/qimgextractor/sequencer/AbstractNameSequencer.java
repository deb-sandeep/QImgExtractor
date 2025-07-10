package com.sandy.sconsole.qimgextractor.sequencer;

import lombok.Getter;
import lombok.Setter;

abstract public class AbstractNameSequencer {

    @Getter @Setter
    protected String baseName = null ;
    
    public String getNextName() {
        String nextName = generateNextName() ;
        if( nextName != null ) {
            if( baseName != null ) {
                nextName = baseName + "." + nextName ;
            }
        }
        return nextName ;
    }
    
    protected abstract String generateNextName() ;
}
