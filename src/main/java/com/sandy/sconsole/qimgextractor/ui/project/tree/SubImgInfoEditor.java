package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.project.imgpanel.SubImgInfo;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.showErrorMsg;
import static javax.swing.BorderFactory.*;

@Slf4j
public class SubImgInfoEditor extends DefaultTreeCellEditor {

    private final ProjectTreePanel treePanel ;
    
    private JTextField editorField ;
    private SubImgInfo subImgInfo ;
    
    public SubImgInfoEditor( ProjectTreePanel treePanel ) {
        super( treePanel.getTree(), ( DefaultTreeCellRenderer )treePanel.getTree().getCellRenderer() ) ;
        this.treePanel = treePanel ;
        
        initEditorComponent() ;
    }

    private void initEditorComponent() {
        editorField = new JTextField() ;
        editorField.setBorder( createCompoundBorder(
                createLineBorder( Color.GRAY ), createEmptyBorder( 2, 20, 2, 5 ) ) ) ;
        editorField.addActionListener( e -> {
            boolean validEdit = treePanel.subImgTagNameChanged( subImgInfo, editorField.getText() ) ;
            if( validEdit ) {
                subImgInfo.setTag( editorField.getText() ) ;
                stopCellEditing() ;
            }
            else {
                showErrorMsg( treePanel, "Invalid sub image tag name!" ) ;
            }
        } ) ;
    }
    
    @Override
    public Component getTreeCellEditorComponent(
            JTree tree, Object value, boolean isSelected,
            boolean expanded, boolean leaf, int row ) {
        
        if( value instanceof DefaultMutableTreeNode node ) {
            Object userObject = node.getUserObject() ;
            if( userObject instanceof SubImgInfo info ){
                this.subImgInfo = info ;
                editorField.setText( info.getTag() ) ;
                return editorField;
            }
        }
        return super.getTreeCellEditorComponent( tree, value, isSelected,
                                                 expanded, leaf, row ) ;
    }
    
    @Override
    public Object getCellEditorValue() {
        return subImgInfo ;
    }
}
