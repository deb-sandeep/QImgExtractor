package com.sandy.sconsole.qimgextractor.sequencer;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

public class IndexedGear<T> {
    
    protected final String indexerName ;
    
    protected List<T> values ;
    protected int selectedIndex = 0 ;
    
    @Getter @Setter protected IndexedGear<?> parent ;
    @Getter @Setter protected IndexedGear<?> child ;
    
    public IndexedGear( String indexerName ) {
        this.indexerName = indexerName ;
    }
    
    public void setValues( List<T> values ) {
        this.values = values ;
        this.selectedIndex = 0 ;
    }
    
    public void setCurrentValue( T value ) {
        if( values.contains( value ) ) {
            this.selectedIndex = values.indexOf( value ) ;
        }
        else {
            throw new IllegalArgumentException( "The given value is not in " +
                    "the value list for indexer " + indexerName ) ;
        }
    }
    
    public T currentValue() {
        return values.get( selectedIndex ) ;
    }
    
    public T turnForward() {
        selectedIndex++ ;
        if( selectedIndex >= values.size() ) {
            selectedIndex = 0 ;
            if( parent != null ) {
                parent.turnForward() ;
            }
        }
        return currentValue() ;
    }
    
    public T turnBackward() {
        selectedIndex-- ;
        if( selectedIndex < 0 ) {
            selectedIndex = values.size() - 1 ;
            if( child != null ) {
                child.turnBackward() ;
            }
        }
        return currentValue() ;
    }
}
