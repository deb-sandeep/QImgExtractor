package com.sandy.sconsole.qimgextractor.ui.project.tree.pagequestion;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.tree.common.QuestionImgTagNameEditor;
import com.sandy.sconsole.qimgextractor.ui.project.tree.common.ProjectBaseTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class PageQuestionTree extends ProjectBaseTree {
    
    private final PageQuestionTreeModel treeModel ;

    public PageQuestionTree( ProjectPanel projectPanel ) {
        super( projectPanel ) ;
        this.treeModel = new PageQuestionTreeModel( projectModel, this ) ;
        
        super.setModel( treeModel ) ;
        super.setCellEditor( new QuestionImgTagNameEditor( this ) ) ;
        
        SwingUtilities.invokeLater( ()-> expandNode( (DefaultMutableTreeNode)treeModel.getRoot() ) ) ;
    }
}
