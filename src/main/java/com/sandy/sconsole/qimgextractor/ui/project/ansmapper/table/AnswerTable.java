package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Stack;

@Slf4j
public class AnswerTable extends JTable {
    
    private static final Font HEADER_FONT = new Font( "Helvetica", Font.PLAIN, 14 ) ;
    private static final Font TABLE_FONT = new Font( "Courier", Font.PLAIN, 12 ) ;
    
    private final AnswerMapperUI parent ;
    private final ProjectModel projectModel ;
    private final AnswerTableModel answerTableModel ;
    private final AnswerTableDefaultCellRenderer cellRenderer = new AnswerTableDefaultCellRenderer() ;
    private final AnswerTableMMTCellRenderer mmtCellRenderer = new AnswerTableMMTCellRenderer() ;
    
    private final JPopupMenu popupMenu ;
    private Question popupQuestion ;
    
    public AnswerTable( AnswerMapperUI parent ) {
        this.parent = parent ;
        this.projectModel = parent.getProjectModel() ;
        this.answerTableModel = new AnswerTableModel( projectModel, this ) ;
        super.setRowHeight( 30 ) ;
        super.setShowGrid( true ) ;
        super.setGridColor( Color.LIGHT_GRAY ) ;
        super.setFont( TABLE_FONT ) ;
        super.setModel( answerTableModel ) ;
        super.setSelectionMode( ListSelectionModel.SINGLE_SELECTION ) ;
        
        decorateTableHeader() ;
        setColumnWidths() ;
        
        this.popupMenu = createPopupMenu() ;
        addTableListeners() ;
    }
    
    private JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu() ;
        JMenuItem deleteQuestion = new JMenuItem( "Delete Question" ) ;
        deleteQuestion.addActionListener( e -> {
            if( popupQuestion != null ) {
                parent.deleteQuestions( List.of( popupQuestion ) ) ;
            }
        } ) ;
        popup.add( deleteQuestion ) ;
        return popup ;
    }
    
    private void addTableListeners() {
        super.addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent e )  { handlePopupTrigger( e ) ; }
            public void mouseReleased( MouseEvent e ) { handlePopupTrigger( e ) ; }
        } ) ;
    }
    
    private void handlePopupTrigger( MouseEvent e ) {
        if( !e.isPopupTrigger() ) {
            return ;
        }
        
        int row = rowAtPoint( e.getPoint() ) ;
        int col = columnAtPoint( e.getPoint() ) ;
        if( row < 0 || col < 0 ) {
            return ;
        }
        
        popupQuestion = answerTableModel.getQuestionAt( row, col ) ;
        if( popupQuestion != null ) {
            setSelectedCell( row, col ) ;
            popupMenu.show( e.getComponent(), e.getX(), e.getY() ) ;
        }
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
                column.setPreferredWidth( baseWidth + 100 ) ;
            }
            else {
                column.setPreferredWidth( baseWidth - 100 ) ;
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
                answerTableModel.setRawAnswer( selectedRow, selectedCol, answerStack ) ;
                selectedRow++ ;
            }
            setSelectedCell( selectedRow, selectedCol ) ;
        }
        else {
            throw new Question.InvalidAnswerException( "No selected row or column to set answer for!" ) ;
        }
    }
    
    public void setSelectedCell( int row, int col ) {
        if( row < answerTableModel.getRowCount() &&
            col < answerTableModel.getColumnCount() ) {
            super.setRowSelectionInterval( row, row );
            super.setColumnSelectionInterval( col, col );
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
