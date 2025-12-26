package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

import static com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSyncTreeTable.*;

@Slf4j
public class QSyncTreeCellRenderer extends DefaultTreeCellRenderer {
    
    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        
        Component comp =  super.getTreeCellRendererComponent(
                tree, value, sel, expanded,
                leaf, row, hasFocus ) ;
        
        if( value instanceof SyllabusNode ) {
            renderSyllabusNode( comp, ((SyllabusNode)value).name );
        }
        else if( value instanceof QuestionNode ) {
            renderQuestionNode( comp, ((QuestionNode)value).question ) ;
        }
        else if( value instanceof QuestionImgNode ) {
            renderQuestionImgNode( comp, ((QuestionImgNode)value).questionImage ) ;
        }
        
        if( sel ) {
            comp.setForeground( Color.WHITE ) ;
        }
        
        return comp ;
    }
    
    private void renderSyllabusNode( Component comp, String syllabusName ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( SYLLABUS_ROW_FONT ) ;
        label.setText( syllabusName ) ;
        label.setForeground( Color.WHITE ) ;
        label.setIcon( SwingUtils.getIcon( "syllabus" ) );
    }
    
    private void renderQuestionNode( Component comp, Question question ) {
        
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        String qLabel = question.getQID().toString() ;
        qLabel = qLabel.replaceFirst( "/", " " ) ;
        qLabel = qLabel.replaceFirst( "/", " " ) ;
        qLabel = qLabel.replace( "/", "." ) ;
        label.setFont( QUESTION_ROW_FONT ) ;
        label.setText( qLabel ) ;
        
        if( !question.isSynced() ) {
            if( question.isReadyForServerSync() ) {
                label.setIcon( SwingUtils.getIcon( "question_unsynced" ) ) ;
                label.setForeground( Color.BLUE ) ;
            }
            else {
                label.setIcon( SwingUtils.getIcon( "question_wip" ) ) ;
            }
        }
        else {
            if( question.isModifiedAfterSync() ) {
                label.setIcon( SwingUtils.getIcon( "question_dirty" ) ) ;
                label.setForeground( Color.RED ) ;
            }
            else if( !question.isReadyForServerSync() ) {
                label.setIcon( SwingUtils.getIcon( "question_invalid" ) ) ;
                label.setForeground( Color.RED ) ;
            }
            else {
                label.setIcon( SwingUtils.getIcon( "question_synced" ) ) ;
            }
        }
    }
    
    private void renderQuestionImgNode( Component comp, QuestionImage qImg ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        String qLabel = qImg.getShortFileName() ;
        label.setFont( QUESTION_IMG_ROW_FONT ) ;
        label.setText( qLabel ) ;
        label.setIcon( SwingUtils.getIcon( "sub_image" ) ) ;
    }
}
