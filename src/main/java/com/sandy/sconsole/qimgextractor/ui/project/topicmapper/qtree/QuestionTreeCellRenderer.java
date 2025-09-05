package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

@Slf4j
public class QuestionTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public static final Font QUESTION_FONT = new Font( "Courier", Font.PLAIN, 11 ) ;

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        
        Component comp =  super.getTreeCellRendererComponent(
                tree, value, sel, expanded,
                leaf, row, hasFocus ) ;
        
        if( value instanceof DefaultMutableTreeNode node ) {
            Object userObj = node.getUserObject() ;
            if( userObj instanceof Question question ) {
                renderQuestionNode( comp, question ) ;
            }
        }
        
        return comp ;
    }
    
    private void renderQuestionNode( Component comp, Question question ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        String qLabel = question.getQID().toString() ;
        qLabel = qLabel.replaceFirst( "/", " " ) ;
        qLabel = qLabel.replaceFirst( "/", " " ) ;
        qLabel = qLabel.replace( "/", "." ) ;
        label.setFont( QUESTION_FONT ) ;
        label.setText( qLabel ) ;
        if( question.getTopic() != null ) {
            label.setIcon( SwingUtils.getIcon( "flag_green" ) ) ;
        }
        else {
            label.setIcon( SwingUtils.getIcon( "question" ) ) ;
        }
    }
}
