package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModelListener;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreePath;

@Slf4j
public class ProjectTreeModel extends DefaultTreeModel
    implements ProjectModelListener {
    
    private final JTree tree ; // Injected
    private final ProjectModel projectModel ; // Injected
    
    private final DefaultMutableTreeNode rootNode ;
    
    public ProjectTreeModel( ProjectModel projectModel, JTree tree ) {
        
        super( new DefaultMutableTreeNode( projectModel.getProjectName() ) );
        this.projectModel = projectModel;
        this.projectModel.addListener( this ) ;
        this.tree = tree ;
        this.rootNode = ( DefaultMutableTreeNode )super.getRoot() ;
        buildTree() ;
    }
    
    private void buildTree() {
        this.rootNode.removeAllChildren() ;
        for( PageImage pageImg : projectModel.getPageImages() ) {
            this.rootNode.add( createPageImageNode( pageImg ) );
        }
    }
    
    private DefaultMutableTreeNode createPageImageNode( PageImage pageImg ) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( pageImg ) ;
        node.setUserObject( pageImg ) ;
        for( QuestionImage qImg : pageImg.getQImgList() ) {
            node.add( createQuestionImgNode( qImg ) ) ;
        }
        return node ;
    }
    
    private MutableTreeNode createQuestionImgNode( QuestionImage qImg ) {
        return new DefaultMutableTreeNode( qImg ) ;
    }
    
    @Override
    public void newQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {
        for( int i = 0; i < rootNode.getChildCount(); i++ ) {
            DefaultMutableTreeNode pageNode = ( DefaultMutableTreeNode )rootNode.getChildAt( i ) ;
            if( pageNode.getUserObject() == pageImage ) {
                pageNode.add( createQuestionImgNode( qImg ) ) ;
                nodeStructureChanged( pageNode ) ;
                
                TreePath path = new TreePath( pageNode.getPath() ) ;
                tree.expandPath( path ) ;
                tree.scrollPathToVisible( path ) ;
                break;
            }
        }
    }
    
    @Override
    public void questionTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        for( int i = 0; i < rootNode.getChildCount(); i++ ) {
            DefaultMutableTreeNode pageNode = ( DefaultMutableTreeNode )rootNode.getChildAt( i ) ;
            for( int j = 0; j < pageNode.getChildCount(); j++ ) {
                DefaultMutableTreeNode qImgNode = ( DefaultMutableTreeNode )pageNode.getChildAt( j ) ;
                if( qImgNode.getUserObject() == qImg ) {
                    nodeChanged( qImgNode ) ;
                    break ;
                }
            }
        }
    }
    
    @Override
    public void questionImgDeleted( QuestionImage qImg ) {
        for( int i = 0; i < rootNode.getChildCount(); i++ ) {
            DefaultMutableTreeNode pageNode = ( DefaultMutableTreeNode )rootNode.getChildAt( i ) ;
            for( int j = 0; j < pageNode.getChildCount(); j++ ) {
                DefaultMutableTreeNode qImgNode = ( DefaultMutableTreeNode )pageNode.getChildAt( j ) ;
                if( qImgNode.getUserObject() == qImg ) {
                    pageNode.remove( qImgNode ) ;
                    nodeStructureChanged( pageNode ) ;
                    break ;
                }
            }
        }
    }
}
