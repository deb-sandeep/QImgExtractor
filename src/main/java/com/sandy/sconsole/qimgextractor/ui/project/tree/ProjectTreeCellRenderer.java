package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

@Slf4j
public class ProjectTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public static final Font PAGE_FONT  = new Font( "Helvetica", Font.BOLD, 12 ) ;
    public static final Font SUB_IMG_FONT  = new Font( "Helvetica", Font.PLAIN, 11 ) ;

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        
        Component comp =  super.getTreeCellRendererComponent(
                tree, value, sel, expanded,
                leaf, row, hasFocus ) ;
        
        if( value instanceof DefaultMutableTreeNode ) {
            Object userObj = (( DefaultMutableTreeNode )value).getUserObject() ;
            if( userObj instanceof PageImage pageImage ) {
                renderPageImgNode( comp, pageImage ) ;
            }
            else if( userObj instanceof SubImgInfo subImgInfo ) {
                renderSubImgNode( comp, subImgInfo ) ;
            }
        }
        
        return comp ;
    }
    
    private void renderPageImgNode( Component comp, PageImage pageImage ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( PAGE_FONT ) ;
        label.setText( AppUtil.stripExtension( pageImage.getImgFile() ) ) ;
        label.setIcon( SwingUtils.getIcon( "page_image" ) );
    }
    
    private void renderSubImgNode( Component comp, SubImgInfo subImgInfo ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( SUB_IMG_FONT ) ;
        label.setText( subImgInfo.getTag() ) ;
        label.setIcon( SwingUtils.getIcon( "sub_image" ) );
    }
}
