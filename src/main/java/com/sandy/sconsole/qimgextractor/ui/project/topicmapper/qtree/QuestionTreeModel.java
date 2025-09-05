package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree;

import com.sandy.sconsole.qimgextractor.ui.project.model.*;
import lombok.extern.slf4j.Slf4j;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class QuestionTreeModel extends DefaultTreeModel
    implements ProjectModelListener {
    
    private final ProjectModel projectModel ; // Injected
    
    private final DefaultMutableTreeNode rootNode ;
    
    public QuestionTreeModel( ProjectModel projectModel ) {
        
        super( new DefaultMutableTreeNode( projectModel.getProjectName() ) );
        this.projectModel = projectModel;
        this.projectModel.addListener( this ) ;
        this.rootNode = ( DefaultMutableTreeNode )super.getRoot() ;
        buildTree() ;
    }
    
    public void buildTree() {
        this.rootNode.removeAllChildren() ;
        for( Question question : projectModel.getQuestionRepo().getQuestionList() ) {
            this.rootNode.add( new DefaultMutableTreeNode( question ) ) ;
        }
        this.reload() ;
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
