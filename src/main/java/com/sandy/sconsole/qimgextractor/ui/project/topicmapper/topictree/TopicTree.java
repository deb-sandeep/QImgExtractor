package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree;

import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.BaseTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.Enumeration;

@Slf4j
public class TopicTree extends BaseTree {
    
    private final TopicTreeModel treeModel ;
    
    public TopicTree( TopicMapperUI topicMapper ) {
        super( topicMapper ) ;
        this.treeModel = new TopicTreeModel( topicMapper.getProjectModel() ) ;
        
        super.setRowHeight( 20 ) ;
        super.setCellRenderer( new TopicTreeCellRenderer() ) ;
        super.setModel( treeModel ) ;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }
    
    public void refreshTree() {
        this.treeModel.buildTree() ;
    }
    
    public boolean selectNextUnclassifiedQuestion() {
        
        DefaultMutableTreeNode unclassifiedNode = treeModel.getUnclassifiedNode() ;
        DefaultMutableTreeNode nextNode ;
        
        if( unclassifiedNode.getChildCount() > 0 ) {
            nextNode = (DefaultMutableTreeNode) unclassifiedNode.getChildAt(0) ;
            TreePath path = new TreePath( nextNode.getPath() ) ;
            super.setSelectionPath(path) ;
            super.makeVisible(path) ;
            super.scrollPathToVisible(path) ;
            return true ;
        }
        return false ;
    }
    
    public void expandTreeIntelligently( Question question ) {
        String syllabusName = question.getTopic() != null ? question.getTopic().getSyllabusName() : "" ;
        if( syllabusName.isEmpty() ) {
            switch( question.getQID().toString().charAt( 0 ) ) {
                case 'P' -> syllabusName = TopicRepo.IIT_PHYSICS ;
                case 'C' -> syllabusName = TopicRepo.IIT_CHEMISTRY ;
                case 'M' -> syllabusName = TopicRepo.IIT_MATHS ;
            }
        }

        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;
        
        Enumeration<TreeNode> syllabusNodes = root.children() ;
        while( syllabusNodes.hasMoreElements() ) {
            DefaultMutableTreeNode syllabusNode = (DefaultMutableTreeNode)syllabusNodes.nextElement() ;
            
            if( syllabusName.equals( syllabusNode.toString() ) ||
                syllabusNode.toString().equals( "Unclassified" ) ) {
                expandSyllabusNode( syllabusNode, true ) ;
            }
            else {
                expandSyllabusNode( syllabusNode, false ) ;
            }
        }
    }
    
    private void expandSyllabusNode( DefaultMutableTreeNode syllabusNode, boolean expanded ) {
        Enumeration<TreeNode> topicNodes = syllabusNode.children() ;
        while( topicNodes.hasMoreElements() ) {
            DefaultMutableTreeNode topicNode = (DefaultMutableTreeNode)topicNodes.nextElement() ;
            if( expanded ) {
                super.expandPath( new TreePath( topicNode.getPath() ) ) ;
            }
            else {
                super.collapsePath( new TreePath( topicNode.getPath() ) ) ;
            }
        }

        if( expanded ) {
            super.expandPath( new TreePath( syllabusNode.getPath() ) ) ;
        }
        else {
            super.collapsePath( new TreePath( syllabusNode.getPath() ) ) ;
        }
    }
    
    public void selectAdjacentQuestion( boolean forward ) {
        TreePath selectedPath = super.getSelectionPath() ;
        DefaultMutableTreeNode nextNode = null;
        
        if( selectedPath != null ) {
            DefaultMutableTreeNode selectedNode = (DefaultMutableTreeNode)selectedPath.getLastPathComponent() ;
            if( forward ) {
                nextNode = selectedNode.getNextLeaf() ;
                while( nextNode != null && !(nextNode.getUserObject() instanceof Question) ) {
                    nextNode = nextNode.getNextLeaf() ;
                }
            }
            else {
                nextNode = selectedNode.getPreviousLeaf() ;
                while( nextNode != null && !(nextNode.getUserObject() instanceof Question) ) {
                    nextNode = nextNode.getPreviousLeaf() ;
                }
            }
        }
        
        if( nextNode == null ) {
            DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
            if( forward ) {
                nextNode = root.getFirstLeaf() ;
            }
            else {
                nextNode = root.getLastLeaf() ;
            }
        }
        
        if( nextNode != null ) {
            TreePath path = new TreePath( nextNode.getPath() ) ;
            super.setSelectionPath(path) ;
            super.makeVisible(path) ;
            super.scrollPathToVisible(path) ;
        }
    }
}
