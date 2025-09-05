package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;

import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

@Slf4j
public class QuestionTree extends JTree implements TreeSelectionListener {
    
    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;

    @Getter
    private final TopicMapperUI parentPanel;
    
    private final QuestionTreeModel treeModel ;
    
    public QuestionTree( TopicMapperUI parentPanel ) {
        this.parentPanel = parentPanel;
        this.treeModel = new QuestionTreeModel( parentPanel.getProjectModel() ) ;
        
        super.setRootVisible( false ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        super.setCellRenderer( new QuestionTreeCellRenderer() ) ;
        super.setModel( treeModel ) ;
        
        addTreeSelectionListener( this ) ;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }
    
    public void refreshTree() {
        this.treeModel.buildTree() ;
    }
    
    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        if( !getSelectionModel().isSelectionEmpty() ) {
            Object userObject = SwingUtils.getUserObject( e.getPath() ) ;
            if( userObject instanceof Question question ) {
                parentPanel.questionSelected( question, this ) ;
            }
        }
    }
}
