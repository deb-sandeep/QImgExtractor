package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import java.awt.*;
import java.util.Stack;

@Slf4j
public class AnswerTable extends JTable {
    
    private static final Font HEADER_FONT = new Font( "Helvetica", Font.PLAIN, 14 ) ;
    private static final Font TABLE_FONT = new Font( "Courier", Font.PLAIN, 15 ) ;
    
    private final ProjectModel projectModel ;
    private final AnswerTableModel answerTableModel ;
    private final AnswerTableDefaultCellRenderer cellRenderer = new AnswerTableDefaultCellRenderer() ;
    private final AnswerTableMMTCellRenderer mmtCellRenderer = new AnswerTableMMTCellRenderer() ;
    
    public AnswerTable( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        this.answerTableModel = new AnswerTableModel( projectModel ) ;
        super.setRowHeight( 30 ) ;
        super.setShowGrid( true ) ;
        super.setGridColor( Color.LIGHT_GRAY ) ;
        super.setFont( TABLE_FONT ) ;
        super.setModel( answerTableModel ) ;
        super.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
        
        decorateTableHeader() ;
        setColumnWidths() ;
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
                column.setPreferredWidth( baseWidth + 50 ) ;
            }
            else {
                column.setPreferredWidth( baseWidth - 50 ) ;
            }
        }
    }
    
    public void refreshTable() {
        this.answerTableModel.refreshModel() ;
    }
    
    public void setRawAnswers( Stack<String> answerStack )
        throws Question.InvalidAnswerException {
        
        int selectedCol = super.getSelectedColumn() ;
        int selectedRow = super.getSelectedRow() ;
        
        if( selectedCol >= 0 && selectedRow >= 0 ) {
            while( !answerStack.isEmpty() ) {
                answerTableModel.setRawAnswer( selectedCol, selectedRow, answerStack ) ;
                selectedRow++ ;
            }
            if( selectedRow < answerTableModel.getRowCount() ) {
                super.setRowSelectionInterval( selectedRow, selectedRow );
                super.setColumnSelectionInterval( selectedCol, selectedCol );
            }
        }
        else {
            throw new Question.InvalidAnswerException( "No selected row or column to set answer for!" ) ;
        }
    }
    
    @Override
    public TableCellRenderer getCellRenderer( int row, int column ) {
        Question q = answerTableModel.getQuestionAt( row, column ) ;
        if( q != null &&
            q.getQID().getQuestionType().equals( QID.MMT ) &&
            ( column == 1 || column == 3 || column == 5 ) ) {
            return mmtCellRenderer ;
        }
        return cellRenderer ;
    }
}
