package com.sandy.sconsole.qimgextractor.qsrc ;

import com.sandy.sconsole.qimgextractor.qsrc.manual.MNComponentFactory;

public class QSrcFactory {
    
    private static final QSrcComponentFactory AITS_COMP_FACTORY = new GenericComponentFactory() ;
    private static final QSrcComponentFactory RBM_COMP_FACTORY = new GenericComponentFactory() ;
    private static final QSrcComponentFactory MANUAL_COMP_FACTORY = new MNComponentFactory() ;

    public static QSrcComponentFactory getQSrcComponentFactory( String projectName ) {
        if( projectName.startsWith( "AITS" ) ) {
            return AITS_COMP_FACTORY ;
        }
        else if( projectName.startsWith( "RB-" ) ) {
            return RBM_COMP_FACTORY ;
        }
        else if( projectName.startsWith( "MN-" ) ) {
            return MANUAL_COMP_FACTORY ;
        }
        throw new IllegalArgumentException( "Unsupported source type : " + projectName ) ;
    }
}
