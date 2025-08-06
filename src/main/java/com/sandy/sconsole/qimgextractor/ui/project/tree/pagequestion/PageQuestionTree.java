package com.sandy.sconsole.qimgextractor.ui.project.tree.pagequestion;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.tree.QuestionImgTagNameEditor;
import com.sandy.sconsole.qimgextractor.ui.project.tree.TreeUtil;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import static com.sandy.sconsole.qimgextractor.ui.project.tree.TreeUtil.getUserObject;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class PageQuestionTree extends JTree
        implements TreeSelectionListener {
    
    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;
    
    @Getter
    private final ProjectPanel projectPanel ;
    
    @Getter
    private final ProjectModel projectModel ;
    
    private final PageQuestionTreeModel treeModel ;

    public PageQuestionTree( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.treeModel = new PageQuestionTreeModel( projectModel, this ) ;
        
        super.setModel( treeModel ) ;
        super.setRootVisible( true ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        super.setCellRenderer( new PageQuestionTreeCellRenderer() ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        super.setCellEditor( new QuestionImgTagNameEditor( this ) ) ;
        
        addTreeListeners() ;
        SwingUtilities.invokeLater( ()-> expandNode( (DefaultMutableTreeNode)treeModel.getRoot() ) ) ;
    }
    
    private void addTreeListeners() {
        super.addTreeSelectionListener( this ) ;
        super.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                TreePath path = getPathForLocation( e.getX(), e.getY() );
                if( path != null ) {
                    setSelectionPath( path ) ;
                    handleMouseClickOnNode( path, e.getClickCount() ) ;
                }
            }
        } ) ;
        
        super.addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
                TreePath path = getSelectionPath() ;
                if( path != null ) {
                    handleKeyPressedOnNode( path, e ) ;
                }
            }
        } ) ;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return getUserObject( path ) instanceof QuestionImage;
    }
    
    private void handleMouseClickOnNode( TreePath path, int clickCount ) {
        DefaultMutableTreeNode lastNode = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        Object userObj  = lastNode.getUserObject() ;
        
        if( userObj instanceof QuestionImage && clickCount == 2 ) {
            super.startEditingAtPath( path ) ;
        }
    }
    
    private void handleKeyPressedOnNode( TreePath path, KeyEvent e ) {
        DefaultMutableTreeNode lastNode = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        Object userObj = lastNode.getUserObject() ;
        if( userObj instanceof QuestionImage qImg ) {
            switch( e.getKeyCode() ) {
                case KeyEvent.VK_F2 -> super.startEditingAtPath( path ) ;
                case KeyEvent.VK_DELETE -> projectPanel.questionImgDeleted( qImg ) ;
            }
        }
    }
    
    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        
        TreePath selPath = e.getNewLeadSelectionPath() ;
        if( selPath != null ) {
            Object userObj = TreeUtil.getUserObject( selPath ) ;
            if( userObj instanceof PageImage pageImg ) {
                projectPanel.activatePageImg( pageImg ) ;
            }
            else if( userObj instanceof QuestionImage qImg ) {
                projectPanel.activatePageImg( qImg.getPageImg() ) ;
            }
        }
    }
    
    private void expandNode( DefaultMutableTreeNode node ) {
        if( node.getChildCount() > 0 && node.getDepth() > 1 ) {
            super.expandPath( new TreePath( node.getPath() ) ) ;
            Enumeration<TreeNode> children = node.children() ;
            while( children.hasMoreElements() ) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement() ;
                if( child.getUserObject() instanceof PageImage ) {
                    super.expandPath( new TreePath( child.getPath() ) ) ;
                }
            }
        }
    }
}
