package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;

import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class BaseTree extends JTree implements TreeSelectionListener {
    
    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;
    
    @Getter
    protected final TopicMapperUI topicMapper ;
    
    protected BaseTree( TopicMapperUI topicMapper ) {
        this.topicMapper = topicMapper ;

        super.setRootVisible( false ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        
        addTreeSelectionListener( this ) ;
    }
    
    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        if( !getSelectionModel().isSelectionEmpty() ) {
            Object userObject = SwingUtils.getUserObject( e.getPath() ) ;
            if( userObject instanceof Question question ) {
                topicMapper.questionSelected( question, this ) ;
            }
        }
    }
    
    public void selectQuestion( Question question ) {
        selectQuestion( question, ( DefaultMutableTreeNode )treeModel.getRoot() ) ;
    }
    
    private boolean selectQuestion( Question question, DefaultMutableTreeNode node ) {
        Object userObject = node.getUserObject() ;
        if( userObject instanceof Question q ) {
            if( q.equals( question ) ) {
                super.setSelectionPath( new TreePath( node.getPath() ) ) ;
                super.makeVisible( new TreePath( node.getPath() ) ) ;
                super.scrollPathToVisible( new TreePath( node.getPath() ) ) ;
                return true ;
            }
        }
        else {
            if( node.getChildCount() > 0 ) {
                for( int i = 0 ; i < node.getChildCount() ; i++ ) {
                    if( selectQuestion( question, (DefaultMutableTreeNode)node.getChildAt(i) ) ) {
                        return true ;
                    }
                }
            }
        }
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
}
