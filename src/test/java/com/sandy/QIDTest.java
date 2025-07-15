package com.sandy;

import com.sandy.sconsole.qimgextractor.qid.QImgName;
import com.sandy.sconsole.qimgextractor.qid.parser.QIDParserFactory;

public class QIDTest {
    public static void main( String[] args ) {
        QIDTest test = new QIDTest();
        test.execute() ;
    }
    
    private void execute() {
        String qImgName = "AITS-13-A-FT1P1.P_A_SCA_1(1).png" ;
        QImgName qimgName = new QImgName( qImgName ) ;
    }
}
