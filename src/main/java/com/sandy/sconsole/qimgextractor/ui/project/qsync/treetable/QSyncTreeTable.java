package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.*;

import javax.swing.table.TableColumnModel;
import java.awt.*;

public class QSyncTreeTable extends JXTreeTable {
    
    public static final int COL_NAME = 0 ;
    public static final int COL_TYPE = 1 ;
    public static final int COL_LAST_SYNC_DATE = 2 ;
    public static final int COL_LAST_UPDATE_DATE = 3 ;
    public static final int COL_ANSWER = 4 ;
    
    public static final Font QUESTION_ROW_FONT     = new Font( "Courier", Font.PLAIN, 11 ) ;
    public static final Font QUESTION_IMG_ROW_FONT = new Font( "Courier", Font.PLAIN, 12 ) ;
    public static final Font SYLLABUS_ROW_FONT     = new Font( "Courier", Font.BOLD, 14 ) ;
    
    public QSyncTreeTable( QSTreeTableModel model ) {
        super( model ) ;
        super.setRowHeight( 25 ) ;
        super.setShowGrid( false, true ) ;
        super.setGridColor( Color.LIGHT_GRAY.brighter() ) ;
        super.setTreeCellRenderer( new QSyncTreeCellRenderer() ) ;
        super.setAutoResizeMode( JXTreeTable.AUTO_RESIZE_OFF ) ;
        
        setCellRenderers() ;
        setColumnWidths() ;
        setRowHighlighters() ;
    }
    
    private void setCellRenderers() {
        QSyncTableCellRenderer tableCellRenderer = new QSyncTableCellRenderer();
        super.setDefaultRenderer( Object.class, tableCellRenderer ) ;
        super.getColumnModel().getColumn( 2 ).setCellRenderer( tableCellRenderer ) ;
        super.getColumnModel().getColumn( 3 ).setCellRenderer( tableCellRenderer ) ;
    }
    
    private void setColumnWidths() {
        TableColumnModel columnModel = super.getColumnModel() ;
        columnModel.getColumn( COL_NAME ).setPreferredWidth( 250 ) ;
        columnModel.getColumn( COL_TYPE ).setPreferredWidth( 50 ) ;
        columnModel.getColumn( COL_LAST_SYNC_DATE ).setPreferredWidth( 150 ) ;
        columnModel.getColumn( COL_LAST_UPDATE_DATE ).setPreferredWidth( 150 ) ;
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
