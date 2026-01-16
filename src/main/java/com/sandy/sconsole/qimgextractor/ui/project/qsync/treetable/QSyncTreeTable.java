package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table.AnswerTableMMTCellRenderer;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.*;

import javax.swing.*;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import java.awt.*;

@Slf4j
public class QSyncTreeTable extends JXTreeTable implements QSyncTableSyncButtonEditor.ButtonClickHandler {
    
    public static final int COL_NAME = 0 ;
    public static final int COL_TYPE = 1 ;
    public static final int COL_LAST_SYNC_DATE = 2 ;
    public static final int COL_LAST_UPDATE_DATE = 3 ;
    public static final int COL_ANSWER = 4 ;
    public static final int COL_SYNC_BTN = 5 ;
    
    public static final String COL_NAME_LABEL = "Name" ;
    public static final String COL_TYPE_LABEL = "Type" ;
    public static final String COL_LAST_SYNC_DATE_LABEL = "Last Sync" ;
    public static final String COL_LAST_UPDATE_DATE_LABEL = "Last Update" ;
    public static final String COL_ANSWER_LABEL = "Answer" ;
    public static final String COL_SYNC_BTN_LABEL = "Sync" ;
    
    public static final Font QUESTION_ROW_FONT     = new Font( "Courier", Font.PLAIN, 11 ) ;
    public static final Font QUESTION_IMG_ROW_FONT = new Font( "Courier", Font.PLAIN + Font.ITALIC, 12 ) ;
    public static final Font SYLLABUS_ROW_FONT     = new Font( "Courier", Font.BOLD, 14 ) ;
    
    private final AnswerTableMMTCellRenderer mmtCellRenderer = new AnswerTableMMTCellRenderer() ;
    private final QSyncTableSyncButtonEditor syncBtnCellEditor ;
    
    private final QSyncUI parent ;

    public QSyncTreeTable( QSTreeTableModel model, QSyncUI parent ) {
        super( model ) ;
        super.setRowHeight( 26 ) ;
        super.setShowGrid( false, true ) ;
        super.setGridColor( Color.LIGHT_GRAY.brighter() ) ;
        super.setTreeCellRenderer( new QSyncTreeCellRenderer() ) ;
        super.setAutoResizeMode( JXTreeTable.AUTO_RESIZE_OFF ) ;
        
        this.parent = parent ;
        this.syncBtnCellEditor = new QSyncTableSyncButtonEditor( this ) ;
        
        setCellRenderers() ;
        setColumnWidths() ;
        setRowHighlighters() ;
        setColumnEditors() ;
        
        super.setAutoCreateColumnsFromModel( false ) ;
    }
    
    private void setCellRenderers() {
        
        QSyncTableCellRenderer tableCellRenderer = new QSyncTableCellRenderer() ;
        QSyncTableSyncButtonRenderer syncBtnRenderer = new QSyncTableSyncButtonRenderer( tableCellRenderer ) ;
        
        super.setDefaultRenderer( Object.class, tableCellRenderer ) ;
        super.getColumnModel().getColumn( COL_LAST_SYNC_DATE ).setCellRenderer( tableCellRenderer ) ;
        super.getColumnModel().getColumn( COL_LAST_UPDATE_DATE ).setCellRenderer( tableCellRenderer ) ;
        super.getColumnModel().getColumn( COL_SYNC_BTN ).setCellRenderer( syncBtnRenderer ) ;
    }
    
    private void setColumnWidths() {
        
        TableColumnModel columnModel = super.getColumnModel() ;
        columnModel.getColumn( COL_NAME ).setPreferredWidth( 250 ) ;
        columnModel.getColumn( COL_TYPE ).setPreferredWidth( 50 ) ;
        columnModel.getColumn( COL_LAST_SYNC_DATE ).setPreferredWidth( 150 ) ;
        columnModel.getColumn( COL_LAST_UPDATE_DATE ).setPreferredWidth( 150 ) ;
        columnModel.getColumn( COL_ANSWER ).setPreferredWidth( 150 ) ;
        columnModel.getColumn( COL_SYNC_BTN ).setPreferredWidth( 50 ) ;
    }
    
    private void setRowHighlighters() {
        
        SyllabusNodeHighlighter syllabusHL = new SyllabusNodeHighlighter() ;
        QuestionNodeHighlighter questionHL = new QuestionNodeHighlighter() ; // light yellow
        QuestionImgNodeHighlighter imgHL = new QuestionImgNodeHighlighter(
                Color.WHITE,  // even rows
                new Color( 253, 253, 253 ) // odd rows
        ) ;
    
        super.setHighlighters( syllabusHL, questionHL, imgHL ) ;
    }
    
    private void setColumnEditors() {
        
        TableColumnModel columnModel = super.getColumnModel() ;
        columnModel.getColumn( COL_SYNC_BTN ).setCellEditor( this.syncBtnCellEditor ) ;
    }
    
    @Override
    public TableCellRenderer getCellRenderer( int row, int column ) {
        if( column == COL_ANSWER ) {
            var path = getPathForRow( row ) ;
            if( path != null ) {
                Object node = path.getLastPathComponent() ;
                if( node instanceof QuestionNode ) {
                    Question q = ((QuestionNode)node).question ;
                    if( q.getMmtAnswer() != null ) {
                        return mmtCellRenderer ;
                    }
                }
            }
        }
        return super.getCellRenderer( row, column ) ;
    }
    
    @Override
    public void syncButtonClick( int row ) {
    }
    
    public void expandSyllabus() {
        int row = 0 ;
        while( row < getRowCount() ) {
            Object node = getPathForRow( row ).getLastPathComponent() ;
            if( node instanceof SyllabusNode ) {
                expandRow( row ) ;
            }
            else if( node instanceof QuestionNode ) {
                collapseRow( row ) ;
            }
            row++ ;
        }
    }
    
    // -------------------- Inner classes --------------------------------------
    static class SyllabusNodeHighlighter extends AbstractHighlighter {
        
        public SyllabusNodeHighlighter() {
            super( SyllabusNodeHighlighter::isSyllabusRow ) ;
        }
        
        @Override
        protected Component doHighlight( Component component, ComponentAdapter adapter ) {
            
            if( adapter.isSelected() ) return component ;
            
            Object node = TreeTableNodeUtils.getNode(adapter);
            if (!(node instanceof SyllabusNode syllabus)) return component ;
            
            String name = syllabus.getName() ;
            
            Color bg = Color.BLUE ;
            if( name.contains( "Physics" ) ) {
                bg = SwingUtils.PHY_COLOR.darker() ;
            }
            else if( name.contains("Chemistry") ) {
                bg = SwingUtils.CHEM_COLOR.darker() ;
            }
            else if( name.contains("Math") ) {
                bg = SwingUtils.MATHS_COLOR.darker() ;
            }
            
            component.setBackground( bg ) ;
            component.setForeground( Color.WHITE ) ;
            return component ;
        }
        
        private static boolean isSyllabusRow( Component renderer, ComponentAdapter adapter ) {
            Object node = TreeTableNodeUtils.getNode(adapter);
            return node instanceof SyllabusNode;
        }
    }
    
    static class QuestionNodeHighlighter extends AbstractHighlighter {
        
        public QuestionNodeHighlighter() {
            super( QuestionNodeHighlighter::isQuestionRow ) ;
        }
        
        @Override
        protected Component doHighlight( Component component, ComponentAdapter adapter ) {
            
            if( adapter.isSelected() ) return component ;
            
            Object node = TreeTableNodeUtils.getNode( adapter ) ;
            if (!(node instanceof QuestionNode questionNode)) return component ;
            
            String syllabusName = "Unclassified" ;
            
            if( questionNode.question.getTopic() != null ) {
                syllabusName = questionNode.question.getTopic().getSyllabusName() ;
            }
            
            Color bg ;
            if( syllabusName.contains( "Physics" ) ) {
                bg = SwingUtils.PHY_COLOR.brighter() ;
            }
            else if( syllabusName.contains("Chemistry") ) {
                bg = SwingUtils.CHEM_COLOR.brighter() ;
            }
            else if( syllabusName.contains("Math") ) {
                bg = SwingUtils.MATHS_COLOR.brighter() ;
            }
            else {
                bg = Color.YELLOW ;
            }

            component.setBackground( bg ) ;
            return component ;
        }
        
        private static boolean isQuestionRow( Component renderer, ComponentAdapter adapter ) {
            Object node = TreeTableNodeUtils.getNode(adapter);
            return node instanceof QuestionNode;
        }
    }
    
    static class QuestionImgNodeHighlighter extends AbstractHighlighter {
        
        private final Color evenColor;
        private final Color oddColor;
        
        public QuestionImgNodeHighlighter( Color evenColor, Color oddColor ) {
            super( QuestionImgNodeHighlighter::isImgRow ) ;
            this.evenColor = evenColor;
            this.oddColor = oddColor;
        }
        
        private static boolean isImgRow(Component renderer, ComponentAdapter adapter) {
            Object node = TreeTableNodeUtils.getNode( adapter ) ;
            return node instanceof QuestionImgNode;
        }
        
        @Override
        protected Component doHighlight( Component component, ComponentAdapter adapter ) {

            if( adapter.isSelected() ) return component ;
            
            boolean even = adapter.row % 2 == 0 ;
            component.setBackground( even ? evenColor : oddColor ) ;
            return component ;
        }
    }
}
