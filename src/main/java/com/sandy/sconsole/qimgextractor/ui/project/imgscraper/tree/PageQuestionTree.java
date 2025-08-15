package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.tree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import lombok.Getter;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.*;
import java.util.Enumeration;

import static javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION;

public class PageQuestionTree extends JTree implements ActionListener, TreeSelectionListener {
    
    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;

    @Getter
    private final ImageScraperUI parentPanel;
    
    private final PageQuestionTreeModel treeModel ;
    private final JPopupMenu popupMenu ;
    
    private DefaultMutableTreeNode selectedNode;
    
    public PageQuestionTree( ImageScraperUI parentPanel ) {
        this.parentPanel = parentPanel;
        this.treeModel = new PageQuestionTreeModel( parentPanel.getProjectModel(), this ) ;
        
        super.setRootVisible( true ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( SINGLE_TREE_SELECTION ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        super.setCellRenderer( new PageQuestionTreeCellRenderer() ) ;

        super.setModel( treeModel ) ;
        super.setCellEditor( new QuestionImgTagNameEditor( this ) ) ;
        
        addTreeListeners() ;
        this.popupMenu = createPageImagePopupMenu() ;
        
        SwingUtilities.invokeLater( this::expandAll ) ;
    }
    
    private void addTreeListeners() {
        super.addTreeSelectionListener( this ) ;
        super.addMouseListener( new MouseAdapter() {
            public void mouseClicked( MouseEvent e ) {
                if( SwingUtilities.isRightMouseButton( e ) ) {
                    int row = getClosestRowForLocation( e.getX(), e.getY() );
                    setSelectionRow( row );
                    DefaultMutableTreeNode node = ( DefaultMutableTreeNode )getLastSelectedPathComponent();
                    if( node != null ) {
                        selectedNode = node;
                        if( node.getUserObject() instanceof PageImage ) {
                            popupMenu.show( e.getComponent(), e.getX(), e.getY() ) ;
                        }
                    }
                }
                else {
                    TreePath path = getPathForLocation( e.getX(), e.getY() );
                    if( path != null ) {
                        setSelectionPath( path ) ;
                        handleMouseClickOnNode( path, e.getClickCount() ) ;
                    }
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
    
    private JPopupMenu createPageImagePopupMenu() {
        JPopupMenu popup = new JPopupMenu() ;
        JMenuItem closeTab = new JMenuItem( "Close Tab" ) ;
        JMenuItem closeRemaining = new JMenuItem( "Close Remaining Tabs" ) ;
        JMenuItem openRemaining = new JMenuItem( "Open Remaining Tabs" ) ;
        
        closeTab.addActionListener( this ) ;
        closeRemaining.addActionListener( this ) ;
        openRemaining.addActionListener( this ) ;
        
        popup.add( closeTab ) ;
        popup.add( closeRemaining ) ;
        popup.add( openRemaining ) ;
        return popup;
    }
    
    @Override
    public boolean isPathEditable( TreePath path ) {
        return SwingUtils.getUserObject( path ) instanceof QuestionImage;
    }
    
    @Override
    public void valueChanged( TreeSelectionEvent e ) {
        TreePath selPath = e.getNewLeadSelectionPath() ;
        if( selPath != null ) {
            Object userObj = SwingUtils.getUserObject( selPath ) ;
            if( userObj instanceof PageImage pageImg ) {
                parentPanel.activatePageImg( pageImg ) ;
            }
            else if( userObj instanceof QuestionImage qImg ) {
                parentPanel.activatePageImg( qImg.getPageImg() ) ;
            }
        }
    }
    
    protected void handleMouseClickOnNode( TreePath path, int clickCount ) {
        DefaultMutableTreeNode lastNode = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        Object userObj  = lastNode.getUserObject() ;
        
        if( userObj instanceof QuestionImage && clickCount == 2 ) {
            super.startEditingAtPath( path ) ;
        }
    }
    
    protected void handleKeyPressedOnNode( TreePath path, KeyEvent e ) {
        DefaultMutableTreeNode lastNode = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        Object userObj = lastNode.getUserObject() ;
        if( userObj instanceof QuestionImage qImg ) {
            switch( e.getKeyCode() ) {
                case KeyEvent.VK_F2 -> super.startEditingAtPath( path ) ;
                case KeyEvent.VK_DELETE -> parentPanel.questionImgDeleted( qImg ) ;
            }
        }
    }
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        String command = ( ( JMenuItem )e.getSource() ).getText() ;
        Object userObject = selectedNode.getUserObject() ;
        
        switch( command ) {
            case "Close Tab" -> parentPanel.closePageImageTab( (PageImage )userObject ) ;
            case "Close Remaining Tabs" -> parentPanel.closeAllRemainingTabs( (PageImage )userObject ) ;
            case "Open Remaining Tabs" -> parentPanel.openAllRemainingTabs( (PageImage)userObject ) ;
        }
    }
    
    public void expandAll() {
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;
        
        Enumeration<TreeNode> pageNodes = root.children() ;
        while( pageNodes.hasMoreElements() ) {
            DefaultMutableTreeNode pageNode = (DefaultMutableTreeNode)pageNodes.nextElement() ;
            if( pageNode.getUserObject() instanceof PageImage ) {
                super.expandPath( new TreePath( pageNode.getPath() ) ) ;
            }
        }
    }
    
    public void collapseAll() {
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;
        
        Enumeration<TreeNode> pageNodes = root.children() ;
        while( pageNodes.hasMoreElements() ) {
            DefaultMutableTreeNode pageNode = (DefaultMutableTreeNode)pageNodes.nextElement() ;
            if( pageNode.getUserObject() instanceof PageImage ) {
                super.collapsePath( new TreePath( pageNode.getPath() ) ); ;
            }
        }
    }
}
