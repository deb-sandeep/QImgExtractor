package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgInfo;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

import static javax.swing.BorderFactory.*;

@Slf4j
public class SubImgInfoEditor extends DefaultTreeCellEditor {

    private JTextField editorField;
    private SubImgInfo oldSubImgInfo;
    
    private final SubImgInfoEditCallback callback;

    public SubImgInfoEditor( JTree tree,
                             DefaultTreeCellRenderer renderer,
                             SubImgInfoEditCallback callback ) {
        super( tree, renderer ) ;
        this.callback = callback;
        initEditorComponent();
    }

    private void initEditorComponent() {
        editorField = new JTextField() ;
        editorField.setBorder(
                createCompoundBorder( createLineBorder( Color.GRAY ),
                                      createEmptyBorder( 2, 20, 2, 5 ) )
        ) ;
        editorField.addActionListener( e -> {
            if( callback != null && oldSubImgInfo != null ) {
                SubImgInfo newSubImgInfo = oldSubImgInfo.clone() ;
                newSubImgInfo.setTag( editorField.getText() ) ;
                // TODO: Simplify, populate tree utils, add delete
                callback.onSubImgInfoEdited( oldSubImgInfo, newSubImgInfo ) ;
                stopCellEditing() ;
            }
        } ) ;
    }
    
    @Override
    public Component getTreeCellEditorComponent(
            JTree tree, Object value, boolean isSelected,
            boolean expanded, boolean leaf, int row ) {
        
        if( value instanceof DefaultMutableTreeNode node ){
            Object userObject = node.getUserObject() ;
            if( userObject instanceof SubImgInfo subImgInfo ){
                this.oldSubImgInfo = subImgInfo;
                editorField.setText( subImgInfo.getTag() ) ;
                return editorField;
            }
        }
        return super.getTreeCellEditorComponent( tree, value, isSelected,
                                                 expanded, leaf, row ) ;
    }
    
    @Override
    public Object getCellEditorValue() {
        if( oldSubImgInfo != null ) {
            oldSubImgInfo.setTag( editorField.getText() ) ;
        }
        return oldSubImgInfo ;
    }

    public interface SubImgInfoEditCallback {
        void onSubImgInfoEdited( SubImgInfo oldSubImgInfo, SubImgInfo newSubImgInfo ) ;
    }
}
