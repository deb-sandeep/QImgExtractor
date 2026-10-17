package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.BaseTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

// Once the topic mapper is shown, the syllabus and topic nodes are expanded
// or collapsed only by the user. Programmatic selections don't expand the
// tree and a refresh restores the expansion state from before the refresh.
@Slf4j
public class TopicTree extends BaseTree {
    
    private final TopicTreeModel treeModel ;
    
    public TopicTree( TopicMapperUI topicMapper ) {
        super( topicMapper ) ;
        this.treeModel = new TopicTreeModel( topicMapper.getProjectModel() ) ;
        
        super.setCellRenderer( new TopicTreeCellRenderer() ) ;
        ToolTipManager.sharedInstance().registerComponent( this ) ;
        super.setModel( treeModel ) ;
        super.setExpandsSelectedPaths( false ) ;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }
    
    // Rebuilding the tree replaces all the nodes, which collapses them. The
    // nodes expanded before are expanded again, matched by their path.
    public void refreshTree() {
        TreePath rootPath = new TreePath( treeModel.getRoot() ) ;
        Set<String> expandedPaths = new HashSet<>() ;
        Enumeration<TreePath> expanded = super.getExpandedDescendants( rootPath ) ;
        while( expanded != null && expanded.hasMoreElements() ) {
            expandedPaths.add( expanded.nextElement().toString() ) ;
        }
        
        this.treeModel.buildTree() ;
        
        expandPath( rootPath ) ;
        restoreExpansion( ( DefaultMutableTreeNode )treeModel.getRoot(), expandedPaths ) ;
    }
    
    // Parents are expanded before their children as expanding a child also
    // expands its collapsed ancestors.
    private void restoreExpansion( DefaultMutableTreeNode node, Set<String> expandedPaths ) {
        for( int i = 0; i < node.getChildCount(); i++ ) {
            DefaultMutableTreeNode child = ( DefaultMutableTreeNode )node.getChildAt( i ) ;
            if( child.getUserObject() instanceof Question ) {
                continue ;
            }
            TreePath path = new TreePath( child.getPath() ) ;
            if( expandedPaths.contains( path.toString() ) ) {
                expandPath( path ) ;
                restoreExpansion( child, expandedPaths ) ;
            }
        }
    }
    
    // Selected paths under collapsed nodes are left hidden.
    @Override
    protected void showSelectedPath( TreePath path ) {
        if( super.isVisible( path ) ) {
            super.scrollPathToVisible( path ) ;
        }
    }
    
    public boolean selectNextUnclassifiedQuestion() {

        DefaultMutableTreeNode unclassifiedNode = treeModel.getUnclassifiedNode() ;
        DefaultMutableTreeNode nextNode ;

        if( unclassifiedNode.getChildCount() > 0 ) {
            nextNode = (DefaultMutableTreeNode) unclassifiedNode.getChildAt(0) ;
            TreePath path = new TreePath( nextNode.getPath() ) ;
            super.setSelectionPath( path ) ;
            showSelectedPath( path ) ;
            return true ;
        }
        return false ;
    }

    public Question getNextUnclassifiedQuestion( Question current ) {
        return treeModel.getNextUnclassifiedQuestion( current ) ;
    }
    
    // Collapses the syllabus nodes and expands only the unclassified node,
    // keeping the selected question in view.
    public void expandOnlyUnclassified() {
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;
        
        Enumeration<TreeNode> syllabusNodes = root.children() ;
        while( syllabusNodes.hasMoreElements() ) {
            DefaultMutableTreeNode syllabusNode = (DefaultMutableTreeNode)syllabusNodes.nextElement() ;
            expandSyllabusNode( syllabusNode, syllabusNode == treeModel.getUnclassifiedNode() ) ;
        }
        
        TreePath selectedPath = super.getSelectionPath() ;
        if( selectedPath != null ) {
            super.scrollPathToVisible( selectedPath ) ;
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
    
    // Moves to the next or previous question among the visible rows,
    // wrapping around at the ends. Questions under collapsed nodes are skipped.
    public void selectAdjacentQuestion( boolean forward ) {
        List<TreePath> qPaths = new ArrayList<>() ;
        for( int row = 0; row < super.getRowCount(); row++ ) {
            TreePath path = super.getPathForRow( row ) ;
            if( SwingUtils.getUserObject( path ) instanceof Question ) {
                qPaths.add( path ) ;
            }
        }
        if( qPaths.isEmpty() ) {
            return ;
        }
        
        int index = qPaths.indexOf( super.getSelectionPath() ) ;
        if( index < 0 ) {
            index = forward ? 0 : qPaths.size()-1 ;
        }
        else {
            index = ( index + ( forward ? 1 : -1 ) + qPaths.size() ) % qPaths.size() ;
        }
        
        TreePath path = qPaths.get( index ) ;
        super.setSelectionPath( path ) ;
        super.scrollPathToVisible( path ) ;
    }
}
