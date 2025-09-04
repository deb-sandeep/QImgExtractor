package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.tree;

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
public class TopicTree extends JTree implements TreeSelectionListener {
    
    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;

    @Getter
    private final TopicMapperUI parentPanel;
    
    private final TopicTreeModel treeModel ;
    
    public TopicTree( TopicMapperUI parentPanel ) {
        this.parentPanel = parentPanel;
        this.treeModel = new TopicTreeModel( parentPanel.getProjectModel() ) ;
        
        super.setRootVisible( false ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        super.setCellRenderer( new TopicTreeCellRenderer() ) ;
        super.setModel( treeModel ) ;
        
        addTreeSelectionListener( this ) ;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }
    
    public void setExpanded( boolean expanded ) {
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;
        
        Enumeration<TreeNode> syllabusNodes = root.children() ;
        while( syllabusNodes.hasMoreElements() ) {
            DefaultMutableTreeNode syllabusNode = (DefaultMutableTreeNode)syllabusNodes.nextElement() ;
            if( expanded ) {
                super.expandPath( new TreePath( syllabusNode.getPath() ) ) ;
            }
            else {
                super.collapsePath( new TreePath( syllabusNode.getPath() ) ) ;
            }
            
            Enumeration<TreeNode> topicNodes = syllabusNode.children() ;
            while( topicNodes.hasMoreElements() ) {
                DefaultMutableTreeNode topicNode = (DefaultMutableTreeNode)topicNodes.nextElement() ;
                if( !(topicNode.getUserObject() instanceof Question) ) {
                    if( expanded ) {
                        super.expandPath( new TreePath( topicNode.getPath() ) ) ;
                    }
                    else {
                        super.collapsePath( new TreePath( topicNode.getPath() ) ) ;
                    }
                }
            }
        }
    }
    
    public void refreshTree() {
        this.treeModel.buildTree() ;
    }
    
    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        Object userObject = SwingUtils.getUserObject( e.getPath() ) ;
        if( userObject instanceof Question question ) {
            parentPanel.questionSelected( question ) ;
        }
    }
}
