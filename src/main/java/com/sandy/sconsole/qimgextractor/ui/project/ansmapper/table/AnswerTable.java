package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;

public class AnswerTable extends JTable {
    
    private static final Font HEADER_FONT = new Font( "Helvetica", Font.PLAIN, 14 ) ;
    private static final Font TABLE_FONT = new Font( "Courier", Font.PLAIN, 15 ) ;
    
    private final ProjectModel projectModel ;
    private final AnswerTableModel answerTableModel ;
    
    public AnswerTable( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        this.answerTableModel = new AnswerTableModel( projectModel ) ;
        super.setRowHeight( 30 ) ;
        super.setShowGrid( true ) ;
        super.setGridColor( Color.LIGHT_GRAY ) ;
        super.setFont( TABLE_FONT ) ;
        super.setModel( answerTableModel ) ;
        
        decorateTableHeader() ;
        setColumnWidths() ;
        
        setDefaultRenderer( Object.class, new AnswerTableCellRenderer( answerTableModel ) ) ;
    }
    
    private void decorateTableHeader() {
        JTableHeader header = super.getTableHeader() ;
        header.setFont( HEADER_FONT ) ;
        header.setBackground( Color.DARK_GRAY ) ;
        header.setForeground( Color.WHITE ) ;
        header.setPreferredSize( new Dimension(header.getWidth(), 20 ) ) ;
        header.setResizingAllowed( false );
    }
    
    private void setColumnWidths() {
        TableColumnModel columnModel = super.getColumnModel() ;
        int baseWidth = super.getWidth() / AnswerTableModel.COL_COUNT ;
        
        for( int i=0; i<AnswerTableModel.COL_COUNT; i++ ) {
            TableColumn column = columnModel.getColumn( i ) ;
            if( i % 2 == 0 ) {
                column.setPreferredWidth( baseWidth + 75 ) ;
            }
            else {
                column.setPreferredWidth( baseWidth - 75 ) ;
            }
        }
    }
    
    public void refreshTable() {
        this.answerTableModel.refreshModel() ;
    }
    
}
