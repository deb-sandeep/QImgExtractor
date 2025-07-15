package com.sandy.sconsole.qimgextractor.qid.blueprint;

public class QTypeSegBP extends SegBP {
    
    private static final String[] Q_TYPES = { "SCA", "MCA", "LCT", "NVT", "IVT", "MMT" } ;
    
    public static final QTypeSegBP INSTANCE = new QTypeSegBP() ;
    
    private QTypeSegBP() {
        super( "Question Type" ) ;
    }
}
