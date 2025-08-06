package com.sandy.sconsole.qimgextractor.ui.project.tree.common;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

import static com.sandy.sconsole.qimgextractor.util.AppUtil.showErrorMsg;
import static javax.swing.BorderFactory.*;

@Slf4j
public class QuestionImgTagNameEditor extends DefaultTreeCellEditor {

    private final ProjectBaseTree tree ;
    
    private JTextField editorField ;
    private QuestionImage questionImg ;
    
    public QuestionImgTagNameEditor( ProjectBaseTree tree ) {
        super( tree, (DefaultTreeCellRenderer)tree.getCellRenderer() ) ;
        this.tree = tree ;
        initEditorComponent() ;
    }

    private void initEditorComponent() {
        editorField = new JTextField() ;
        editorField.setBorder( createCompoundBorder(
                createLineBorder( Color.GRAY ),
                createEmptyBorder( 2, 20, 2, 5 ) ) ) ;
        
        editorField.addActionListener( e -> {
            boolean validEdit = questionImg.isValidTagName( editorField.getText() ) ;
            if( validEdit ) {
                tree.getProjectPanel().questionImgTagNameChanged( questionImg, editorField.getText() ) ;
                stopCellEditing() ;
            }
            else {
                showErrorMsg( tree.getProjectPanel(), "Invalid sub image tag name!" ) ;
            }
        } ) ;
    }
    
    @Override
    public Component getTreeCellEditorComponent(
            JTree tree, Object value, boolean isSelected,
            boolean expanded, boolean leaf, int row ) {
        
        if( value instanceof DefaultMutableTreeNode node ) {
            Object userObject = node.getUserObject() ;
            if( userObject instanceof QuestionImage qImg ){
                this.questionImg = qImg ;
                editorField.setText( qImg.getImgRegionMetadata().getTag() ) ;
                return editorField;
            }
        }
        return super.getTreeCellEditorComponent( tree, value, isSelected,
                                                 expanded, leaf, row ) ;
    }
    
    @Override
    public Object getCellEditorValue() {
        return questionImg ;
    }
}
