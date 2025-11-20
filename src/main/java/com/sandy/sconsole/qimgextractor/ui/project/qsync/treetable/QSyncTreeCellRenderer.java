package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

@Slf4j
public class QSyncTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public static final Font QUESTION_FONT = new Font( "Courier", Font.PLAIN, 11 ) ;
    public static final Font QUESTION_IMG_FONT = new Font( "Courier", Font.PLAIN, 12 ) ;
    public static final Font SYLLABUS_FONT = new Font( "Courier", Font.BOLD, 14 ) ;
    
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        
        Component comp =  super.getTreeCellRendererComponent(
                tree, value, sel, expanded,
                leaf, row, hasFocus ) ;
        
        comp.setForeground( sel ? Color.WHITE : Color.DARK_GRAY ) ;
        
        if( value instanceof SyllabusNode ) {
            renderSyllabusNode( comp, ((SyllabusNode)value).name );
        }
        else if( value instanceof QuestionNode ) {
            renderQuestionNode( comp, ((QuestionNode)value).question, sel ) ;
        }
        else if( value instanceof QuestionImgNode ) {
            renderQuestionImgNode( comp, ((QuestionImgNode)value).questionImage, sel ) ;
        }
        
        return comp ;
    }
    
    private void renderQuestionImgNode( Component comp, QuestionImage qImg, boolean isSelected ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        String qLabel = qImg.getShortFileName() ;
        label.setFont( QUESTION_IMG_FONT ) ;
        label.setText( qLabel ) ;
        label.setIcon( SwingUtils.getIcon( "sub_image" ) ) ;
    }
    
    private void renderQuestionNode( Component comp, Question question, boolean isSelected ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        String qLabel = question.getQID().toString() ;
        qLabel = qLabel.replaceFirst( "/", " " ) ;
        qLabel = qLabel.replaceFirst( "/", " " ) ;
        qLabel = qLabel.replace( "/", "." ) ;
        label.setFont( QUESTION_FONT ) ;
        label.setText( qLabel ) ;
        label.setIcon( SwingUtils.getIcon( "question" ) ) ;
    }
    
    private void renderSyllabusNode( Component comp, String syllabusName ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( SYLLABUS_FONT ) ;
        label.setText( syllabusName ) ;
        label.setForeground( Color.WHITE ) ;
        label.setIcon( SwingUtils.getIcon( "syllabus" ) );
    }
}
