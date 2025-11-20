package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.*;

import java.awt.*;





public class QSyncTreeTable extends JXTreeTable {
    
    public QSyncTreeTable( QSTreeTableModel model ) {
        super( model ) ;
        super.setRowHeight( 25 ) ;
        super.setShowGrid( false, true ) ;
        super.setGridColor( Color.LIGHT_GRAY.brighter() ) ;
        super.setTreeCellRenderer( new QSyncTreeCellRenderer() ) ;
        super.setAutoResizeMode( JXTreeTable.AUTO_RESIZE_OFF ) ;
        
        setColumnWidths() ;
        setRowHighlighters() ;
    }
    
    private void setColumnWidths() {
        super.getColumnModel().getColumn( 0 ).setPreferredWidth( 300 ) ;
    }
    
    private void setRowHighlighters() {
        
        SyllabusNodeHighlighter syllabusHL = new SyllabusNodeHighlighter() ;
        QuestionNodeHighlighter questionHL = new QuestionNodeHighlighter() ; // light yellow
        QuestionImgNodeHighlighter imgHL = new QuestionImgNodeHighlighter(
                Color.WHITE,  // odd rows
                new Color( 253, 253, 253 ) // even rows
        ) ;
    
        super.setHighlighters( syllabusHL, questionHL, imgHL ) ;
    }
    
    // -------------------- Inner classes --------------------------------------
    static class SyllabusNodeHighlighter extends AbstractHighlighter {
        
        public SyllabusNodeHighlighter() {
            super( SyllabusNodeHighlighter::isSyllabusRow ) ;
        }
        
        @Override
        protected Component doHighlight( Component component, ComponentAdapter adapter ) {
            
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
            
            Object node = TreeTableNodeUtils.getNode( adapter ) ;
            if (!(node instanceof QuestionNode questionNode)) return component ;
            
            String syllabusName = questionNode.question.getTopic().getSyllabusName() ;
            
            Color bg = Color.RED ;
            if( syllabusName.contains( "Physics" ) ) {
                bg = SwingUtils.PHY_COLOR.brighter() ;
            }
            else if( syllabusName.contains("Chemistry") ) {
                bg = SwingUtils.CHEM_COLOR.brighter() ;
            }
            else if( syllabusName.contains("Math") ) {
                bg = SwingUtils.MATHS_COLOR.brighter() ;
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
            boolean even = adapter.row % 2 == 0 ;
            component.setBackground( even ? evenColor : oddColor ) ;
            return component ;
        }
    }
}
