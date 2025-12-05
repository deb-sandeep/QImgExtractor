package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import java.awt.*;

@Slf4j
public class QSyncTableSyncButtonRenderer extends JButton implements TableCellRenderer {
    
    private final QSyncTableCellRenderer defaultRenderer ;
    
    public QSyncTableSyncButtonRenderer( QSyncTableCellRenderer defaultRenderer ) {
        super.setOpaque( true ) ;
        this.defaultRenderer = defaultRenderer ;
        super.setText( "" ) ;
    }
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column ) {
        
        JXTreeTable treeTable = ( JXTreeTable )table ;
        var path = treeTable.getPathForRow( row ) ;
        
        if( path != null ) {
            Object node = path.getLastPathComponent() ;
            if( node instanceof QuestionNode ) {
                Question question = ((QuestionNode)node).question ;
                if( question.isReadyForServerSync() ) {
                    setIcon( SwingUtils.getIcon( "sync_button" ) ) ;
                }
                else {
                    setIcon( SwingUtils.getIcon( "sync_button_disabled" ) ) ;
                }
                return this ;
            }
        }
        return this.defaultRenderer ;
    }
}
