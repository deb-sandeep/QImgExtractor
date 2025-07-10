package com.sandy.sconsole.qimgextractor.sequencer;

import java.util.ArrayList;

public class IntRangeIndexedGear extends IndexedGear<Integer> {

    public IntRangeIndexedGear( String name, int start, int end ) {
        super( name ) ;
        ArrayList<Integer> values = new ArrayList<>() ;
        for( int i = start; i <= end ; i++ ) {
            values.add( i ) ;
        }
        super.setValues( values ) ;
    }
}
