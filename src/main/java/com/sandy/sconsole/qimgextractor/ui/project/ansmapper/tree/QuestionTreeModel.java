package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree;

import com.sandy.sconsole.qimgextractor.ui.project.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

@Slf4j
public class QuestionTreeModel extends DefaultTreeModel
    implements ProjectModelListener {
    
    private final JTree tree ; // Injected
    private final ProjectModel projectModel ; // Injected
    
    private final DefaultMutableTreeNode rootNode ;
    
    public QuestionTreeModel( ProjectModel projectModel, JTree tree ) {
        
        super( new DefaultMutableTreeNode( projectModel.getProjectName() ) );
        this.projectModel = projectModel;
        this.projectModel.addListener( this ) ;
        this.tree = tree ;
        this.rootNode = ( DefaultMutableTreeNode )super.getRoot() ;
        buildTree() ;
    }
    
    public void buildTree() {
        this.rootNode.removeAllChildren() ;
        for( Question question : projectModel.getQuestionRepo().getQuestionList() ) {
            this.rootNode.add( createQuestionNode( question ) );
        }
    }
    
    private DefaultMutableTreeNode createQuestionNode( Question question ) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( question ) ;
        for( QuestionImage qImg : question.getQImgList() ) {
            node.add( createQuestionImgNode( qImg ) ) ;
        }
        return node ;
    }
    
    private DefaultMutableTreeNode createQuestionImgNode( QuestionImage qImg ) {
        return new DefaultMutableTreeNode( qImg ) ;
    }
    
    @Override
    public void newQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {
        buildTree() ;
    }
    
    @Override
    public void questionTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        buildTree() ;
    }
    
    @Override
    public void questionImgDeleted( QuestionImage qImg ) {
        buildTree();
    }
    
    @Override
    public void partSelectionModeUpdated( boolean newMode ) {/* Ignore */}
}
