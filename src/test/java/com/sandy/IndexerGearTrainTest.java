package com.sandy;

import com.sandy.sconsole.qimgextractor.sequencer.IndexedGear;
import com.sandy.sconsole.qimgextractor.sequencer.IndexerGearTrain;
import com.sandy.sconsole.qimgextractor.sequencer.IntRangeIndexedGear;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class IndexerGearTrainTest {
    public static void main( String[] args ) {
        IndexerGearTrainTest test = new IndexerGearTrainTest() ;
        test.init() ;
        test.test() ;
    }
    
    private IndexerGearTrain train = new IndexerGearTrain() ;
    private IndexedGear<String> subNameIndexer ;
    private IndexedGear<String> qTypeIndexer;
    private IntRangeIndexedGear qNoIndexer ;

    private void init() {
        subNameIndexer = new IndexedGear<>( "Subject Names" ) ;
        qTypeIndexer = new IndexedGear<>( "Question Types" ) ;
        qNoIndexer = new IntRangeIndexedGear( "Q No", 1, 3 ) ;
        
        subNameIndexer.setValues( Arrays.asList( "Phy", "Chem", "Maths" ) ) ;
        qTypeIndexer.setValues( Arrays.asList( "SCA", "MCA", "NVT", "IVT" ) ) ;
        
        train.addGear( subNameIndexer ) ;
        train.addGear( qTypeIndexer ) ;
        train.addGear( qNoIndexer ) ;
    }
    
    public void test() {
        for( int i = 0 ; i < 20 ; i++ ) {
            List<Object> values = train.increment() ;
            StringBuilder sb = new StringBuilder() ;
            for( Object value : values ) {
                sb.append( value ).append( '_' ) ;
            }
            sb.deleteCharAt( sb.length() - 1 ) ;
            log.debug( sb.toString() ) ;
        }
    }
}
