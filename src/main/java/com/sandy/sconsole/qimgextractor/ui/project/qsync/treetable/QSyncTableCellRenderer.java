package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTreeTable;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.text.SimpleDateFormat;

import static com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSyncTreeTable.*;

@Slf4j
public class QSyncTableCellRenderer extends DefaultTableCellRenderer {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat( "dd-MM-yy HH:mm:ss" ) ;
    
    @Override
    public Component getTableCellRendererComponent(
            JTable table, Object value, boolean isSelected,
            boolean hasFocus, int row, int column ) {
        
        JLabel label = ( JLabel )super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column ) ;
        label.setFont( new Font( "Courier", Font.PLAIN, 12 ) ) ;
        label.setIcon( null ) ;
        label.setText( "" ) ;
        
        if( column == COL_LAST_UPDATE_DATE ) {
            if( value != null ) {
                label.setText( DATE_FORMAT.format( value ) );
            }
        }
        else {
            JXTreeTable treeTable = ( JXTreeTable )table ;
            var path = treeTable.getPathForRow( row ) ;
            if( path != null ) {
                Object node = path.getLastPathComponent() ;
                if( node instanceof QuestionNode ) {
                    renderQuestionRowCell( column, value, label ) ;
                }
                else if( node instanceof QuestionImgNode ) {
                    renderQuestionImgRowCell( column, value, label ) ;
                }
            }
        }
        
        return label ;
    }
    
    private void renderQuestionRowCell( int column, Object value, JLabel label ) {
        if( column == COL_TYPE ) {
            label.setText( value.toString() ) ;
        }
        else if( column == COL_LAST_SYNC_DATE ) {
            if( value != null ) {
                label.setText( DATE_FORMAT.format( value ) ) ;
            }
            else {
                label.setIcon( SwingUtils.getIcon( "bullet_red" ) ) ;
            }
        }
    }
    
    private void renderQuestionImgRowCell( int column, Object value, JLabel label ) {
        if( column == COL_TYPE ) {
            label.setIcon( SwingUtils.getIcon( "image_file" ) ) ;
        }
    }
}
