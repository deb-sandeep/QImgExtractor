package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import org.jdesktop.swingx.JXTreeTable;

import java.awt.*;

public class QSyncTreeTable extends JXTreeTable {
    
    public QSyncTreeTable( QSTreeTableModel model ) {
        super( model ) ;
        super.setRowHeight( 20 ) ;
        super.setShowGrid( true ) ;
        super.setGridColor( Color.LIGHT_GRAY ) ;
    }
}
