package com.sandy.sconsole.qimgextractor.qsrc ;

import com.sandy.sconsole.qimgextractor.qsrc.aits.AITSComponentFactory;
import com.sandy.sconsole.qimgextractor.qsrc.rbm.RBMComponentFactory;

public class QSrcFactory {
    
    private static final AITSComponentFactory AITS_COMP_FACTORY = new AITSComponentFactory() ;
    private static final RBMComponentFactory RBM_COMP_FACTORY = new RBMComponentFactory() ;

    public static QSrcComponentFactory getQSrcComponentFactory( String projectName ) {
        if( projectName.startsWith( "AITS" ) ) {
            return AITS_COMP_FACTORY ;
        }
        else if( projectName.startsWith( "RB-" ) ) {
            return RBM_COMP_FACTORY ;
        }
        throw new IllegalArgumentException( "Unsupported source type : " + projectName ) ;
    }
}
