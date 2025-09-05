package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

@Slf4j
public class TopicTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public static final Font QUESTION_FONT = new Font( "Courier", Font.PLAIN, 11 ) ;
    public static final Font TOPIC_FONT = new Font( "Courier", Font.PLAIN, 11 ) ;
    public static final Font SYLLABUS_FONT = new Font( "Courier", Font.PLAIN, 12 ) ;

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
            else {
                String nodeName = node.toString() ;
                if( nodeName.startsWith( "IIT " ) || nodeName.equals( "Unclassified" ) ) {
                    renderSyllabusNode( comp, nodeName ) ;
                }
                else {
                    renderTopicNode( comp, nodeName ) ;
                }
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
    
    private void renderSyllabusNode( Component comp, String syllabusName ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( SYLLABUS_FONT ) ;
        label.setText( syllabusName ) ;
        label.setIcon( SwingUtils.getIcon( "syllabus" ) );
    }
    
    private void renderTopicNode( Component comp, String topicName ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( TOPIC_FONT ) ;
        label.setText( topicName ) ;
        label.setIcon( SwingUtils.getIcon( "topic" ) );
    }
}
