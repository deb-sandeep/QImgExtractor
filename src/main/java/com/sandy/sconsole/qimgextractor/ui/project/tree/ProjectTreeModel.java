package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModelListener;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.MutableTreeNode;

public class ProjectTreeModel extends DefaultTreeModel
    implements ProjectModelListener {
    
    private final ProjectModel projectModel ;
    
    private final DefaultMutableTreeNode rootNode ;
    
    public ProjectTreeModel( ProjectModel projectModel ) {
        
        super( new DefaultMutableTreeNode( projectModel.getProjectName() ) );
        this.projectModel = projectModel;
        this.projectModel.addListener( this ) ;
        this.rootNode = ( DefaultMutableTreeNode )super.getRoot() ;
        buildTree() ;
    }
    
    private void buildTree() {
        this.rootNode.removeAllChildren() ;
        for( PageImage pageImg : projectModel.getPageImages() ) {
            this.rootNode.add( createPageImageNode( pageImg ) );
        } ;
    }
    
    private DefaultMutableTreeNode createPageImageNode( PageImage pageImg ) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( pageImg ) ;
        node.setUserObject( pageImg ) ;
        for( SubImgInfo subImgInfo : pageImg.getSubImgInfoList() ) {
            node.add( createSubImageNode( subImgInfo ) ) ;
        }
        return node ;
    }
    
    private MutableTreeNode createSubImageNode( SubImgInfo subImgInfo ) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode( subImgInfo ) ;
        node.setUserObject( subImgInfo ) ;
        return node ;
    }
    
    @Override
    public void newSubImgAdded( PageImage pageImage, SubImgInfo newRegionInfo ) {
        for( int i = 0; i < rootNode.getChildCount(); i++ ) {
            DefaultMutableTreeNode pageNode = ( DefaultMutableTreeNode )rootNode.getChildAt( i ) ;
            if( pageNode.getUserObject() == pageImage ) {
                pageNode.add( createSubImageNode( newRegionInfo ) ) ;
                nodeStructureChanged( pageNode ) ;
                break;
            }
        }
    }
}
