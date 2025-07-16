package com.sandy.sconsole.qimgextractor.qid.segment;

import com.sandy.sconsole.qimgextractor.qid.blueprint.QTypeSegBP;

public class QTypeSeg extends Seg<String> {

    private final String qType ;
    private Character lctNo ;
    
    public QTypeSeg( QTypeSegBP blueprint, String qType ) {
        super( blueprint ) ;
        this.qType = qType ;
    }
    
    public QTypeSeg( QTypeSegBP blueprint, String qType, char lctNo ) {
        super( blueprint ) ;
        this.qType = qType ;
        this.lctNo = lctNo ;
    }
    
    @Override
    public String getValue() {
        if( lctNo == null ) {
            return qType ;
        }
        return qType + "_" + lctNo ;
    }
}
