package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree;

import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.Enumeration;

import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class QuestionTree extends JTree {
    
    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;

    @Getter
    private final AnswerMapperUI parentPanel;
    
    private final QuestionTreeModel treeModel ;
    
    public QuestionTree( AnswerMapperUI parentPanel ) {
        this.parentPanel = parentPanel;
        this.treeModel = new QuestionTreeModel( parentPanel.getProjectModel(), this ) ;
        
        super.setRootVisible( true ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        super.setCellRenderer( new QuestionTreeCellRenderer() ) ;

        super.setModel( treeModel ) ;
        
        SwingUtilities.invokeLater( () -> setExpanded( false ) ) ;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }
    
    public void setExpanded( boolean expanded ) {
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;
        
        Enumeration<TreeNode> pageNodes = root.children() ;
        while( pageNodes.hasMoreElements() ) {
            DefaultMutableTreeNode pageNode = (DefaultMutableTreeNode)pageNodes.nextElement() ;
            if( pageNode.getUserObject() instanceof Question ) {
                if( expanded ) {
                    super.expandPath( new TreePath( pageNode.getPath() ) ) ;
                }
                else {
                    super.collapsePath( new TreePath( pageNode.getPath() ) ) ;
                }
            }
        }
    }
}
