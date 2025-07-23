package com.sandy.sconsole.qimgextractor.qsrc ;

import com.sandy.sconsole.qimgextractor.qsrc.aits.AITSComponentFactory;

import java.util.Map;

public class QSrcFactory {
    
    private static final AITSComponentFactory AITS_COMP_FACTORY = new AITSComponentFactory() ;

    public static QSrcComponentFactory getQSrcComponentFactory( String projectName ) {
        if( projectName.startsWith( "AITS" ) ) {
            return AITS_COMP_FACTORY ;
        }
        throw new IllegalArgumentException( "Unsupported source type : " + projectName ) ;
    }
}
