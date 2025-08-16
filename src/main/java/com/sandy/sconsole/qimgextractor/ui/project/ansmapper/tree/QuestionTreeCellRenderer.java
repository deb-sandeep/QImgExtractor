package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

@Slf4j
public class QuestionTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public static final Font QUESTION_FONT = new Font( "Courier", Font.PLAIN, 12 ) ;
    public static final Font Q_IMG_FONT    = new Font( "Helvetica", Font.ITALIC, 11 ) ;

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        
        Component comp =  super.getTreeCellRendererComponent(
                tree, value, sel, expanded,
                leaf, row, hasFocus ) ;
        
        if( value instanceof DefaultMutableTreeNode ) {
            Object userObj = (( DefaultMutableTreeNode )value).getUserObject() ;
            if( userObj instanceof Question question ) {
                renderQuestionNode( comp, question ) ;
            }
            else if( userObj instanceof QuestionImage qImg ) {
                renderQuestionImgNode( comp, qImg ) ;
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
        label.setIcon( SwingUtils.getIcon( "question" ) ) ;
    }
    
    private void renderQuestionImgNode( Component comp, QuestionImage qImg ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( Q_IMG_FONT ) ;
        label.setText( qImg.getImgRegionMetadata().getTag() ) ;
        label.setIcon( SwingUtils.getIcon( "sub_image" ) );
    }
}
