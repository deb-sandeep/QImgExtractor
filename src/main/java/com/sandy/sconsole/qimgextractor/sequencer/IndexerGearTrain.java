package com.sandy.sconsole.qimgextractor.sequencer;

import java.util.ArrayList;
import java.util.List;

public class IndexerGearTrain {

    List<IndexedGear<?>> gears = new ArrayList<IndexedGear<?>>() ;
    
    public void addGear( IndexedGear<?> gear ) {
        IndexedGear<?> lastGear = null ;
        if( !gears.isEmpty() ) {
            lastGear = gears.get( gears.size() - 1 ) ;
        }
        
        gears.add( gear ) ;
        
        if( lastGear != null ) {
            gear.setParent( lastGear ) ;
            lastGear.setChild( gear ) ;
        }
    }
    
    public List<Object> increment() {
        if( !gears.isEmpty() ) {
            gears.get( gears.size() - 1 ).turnForward() ;
        }
        return getGearValues() ;
    }
    
    public List<Object> decrement() {
        if( !gears.isEmpty() ) {
            gears.get( gears.size() - 1 ).turnBackward() ;
        }
        return getGearValues() ;
    }
    
    public List<Object> getGearValues() {
        List<Object> values = new ArrayList<>() ;
        for( IndexedGear<?> gear : gears ) {
            values.add( gear.currentValue() ) ;
        }
        return values ;
    }
}
