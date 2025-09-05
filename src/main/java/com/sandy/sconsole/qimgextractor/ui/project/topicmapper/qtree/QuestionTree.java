package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree;

import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.BaseTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.tree.TreePath;

@Slf4j
public class QuestionTree extends BaseTree {
    
    private final QuestionTreeModel treeModel ;
    
    public QuestionTree( TopicMapperUI topicMapper ) {
        super( topicMapper ) ;
        this.treeModel = new QuestionTreeModel( topicMapper.getProjectModel() ) ;
        
        super.setCellRenderer( new QuestionTreeCellRenderer() ) ;
        super.setModel( treeModel ) ;
        
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }
    
    public void refreshTree() {
        this.treeModel.buildTree() ;
    }
}
