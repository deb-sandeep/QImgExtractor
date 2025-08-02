package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.core.imgpanel.SubImgInfo;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Enumeration;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class ProjectTreePanel extends JPanel
    implements TreeSelectionListener {
    
    public static final Font TREE_FONT  = new Font( "Helvetica", Font.PLAIN, 12 ) ;
    
    public static final int PREFERRED_WIDTH = 400 ;
    
    private final ProjectPanel projectPanel ;
    
    private ProjectTreeModel treeModel ;
    private JTree tree ;
    
    public ProjectTreePanel( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        setUpUI() ;
        SwingUtilities.invokeLater( ()-> expandNode( (DefaultMutableTreeNode)treeModel.getRoot() ) ) ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( createToolbar(), BorderLayout.NORTH ) ;
        add( createTreePanel(), BorderLayout.CENTER ) ;
        setPreferredSize( new Dimension( PREFERRED_WIDTH, 100 ) ) ;
    }
    
    private JPanel createToolbar() {
        JPanel toolbarPanel = new JPanel() ;
        return toolbarPanel ;
    }
    
    private JPanel createTreePanel() {
        
        treeModel = new ProjectTreeModel( projectPanel.getProjectModel() ) ;

        tree = new JTree( treeModel ) {
            public boolean isPathEditable( TreePath path ) {
                DefaultMutableTreeNode node = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
                return node.getUserObject() instanceof SubImgInfo ;
            }
        };
        tree.setRootVisible( true ) ;
        tree.setFont( TREE_FONT ) ;
        tree.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        tree.addTreeSelectionListener( this ) ;
        tree.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                TreePath path = tree.getPathForLocation( e.getX(), e.getY() );
                if( path != null ) {
                    tree.setSelectionPath( path ) ;
                    handleMouseClickOnNode( path, e.getClickCount() ) ;
                }
            }
        } ) ;
        tree.setCellRenderer( new ProjectTreeCellRenderer() ) ;
        tree.setRowHeight( 25 ) ;
        tree.setEditable( true ) ;
        
        // Set the custom cell editor with SubImgInfoEditCallback
        tree.setCellEditor( new SubImgInfoEditor(
                tree,
                ( DefaultTreeCellRenderer ) tree.getCellRenderer(),
                (oldSubImgInfo, newSubImgInfo) -> {
                    // Handle the callback with old and new values
                    System.out.println("Old Value: " + oldSubImgInfo.getTag());
                    System.out.println("New Value: " + newSubImgInfo.getTag());
                    
                    // Optionally update the model or UI
                }
        ));
        
        JScrollPane sp = new JScrollPane( VERTICAL_SCROLLBAR_AS_NEEDED,
                                          HORIZONTAL_SCROLLBAR_NEVER ) ;
        sp.setViewportView( tree ) ;
        
        JPanel treePanel = new JPanel() ;
        treePanel.setLayout( new BorderLayout() ) ;
        treePanel.add( sp, BorderLayout.CENTER ) ;
        
        return treePanel ;
    }
    
    private void handleMouseClickOnNode( TreePath path, int clickCount ) {
        
        DefaultMutableTreeNode lastNode = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        Object userObj = lastNode.getUserObject() ;
        if( userObj instanceof SubImgInfo && clickCount == 2 ) {
            tree.startEditingAtPath( path ) ;
        }
    }
    
    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        
        DefaultMutableTreeNode lastNode ;
        Object userObj ;
        
        TreePath selPath = e.getNewLeadSelectionPath() ;
        if( selPath != null ) {
            lastNode = ( DefaultMutableTreeNode )selPath.getLastPathComponent() ;
            userObj  = lastNode.getUserObject() ;
        }
    }
    
    private void expandNode( DefaultMutableTreeNode node ) {
        
        if( node.getChildCount() > 0 && node.getDepth() > 1 ) {
            tree.expandPath( new TreePath( node.getPath() ) ) ;
            
            Enumeration<TreeNode> children = node.children() ;
            while( children.hasMoreElements() ) {
                DefaultMutableTreeNode child = (DefaultMutableTreeNode)children.nextElement() ;
                if( child.getUserObject() instanceof PageImage ) {
                    tree.expandPath( new TreePath( child.getPath() ) ) ;
                }
            }
        }
    }
}
