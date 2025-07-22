package com.sandy;

import com.sandy.sconsole.qimgextractor.qid.blueprint.ParseException;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QIDTest {
    public static void main( String[] args ) throws ParseException {
        QIDTest test = new QIDTest();
        test.execute() ;
    }
    
    private void execute() throws ParseException {
        //String qImgName = "AITS-13-A-FT1P1.P_A_LCT_A_1(2).png" ;
        String qImgName = "AITS-13-A-FT1P1.P_A_LCT_A_Hdr(2).png" ;
        QImgName qimgName = new QImgName( qImgName ) ;
        log.debug( "Name matches : {}", qimgName.toString().equals( qImgName ) ) ;
        QImgName nextImgName = qimgName.getNextName() ;
        log.debug( "Next name - {}", nextImgName.toString() ) ;
    }
}
