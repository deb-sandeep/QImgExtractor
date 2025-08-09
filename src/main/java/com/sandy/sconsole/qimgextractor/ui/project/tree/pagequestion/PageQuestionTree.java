package com.sandy.sconsole.qimgextractor.ui.project.tree.pagequestion;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.tree.common.QuestionImgTagNameEditor;
import com.sandy.sconsole.qimgextractor.ui.project.tree.common.ProjectBaseTree;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;


public class PageQuestionTree extends ProjectBaseTree implements ActionListener {
    
    private final PageQuestionTreeModel treeModel ;
    private DefaultMutableTreeNode selectedNode;
    
    public PageQuestionTree( ProjectPanel projectPanel ) {
        super( projectPanel ) ;
        this.treeModel = new PageQuestionTreeModel( projectModel, this ) ;
        
        super.setModel( treeModel ) ;
        super.setCellEditor( new QuestionImgTagNameEditor( this ) ) ;
        
        SwingUtilities.invokeLater( ()-> expandNode( (DefaultMutableTreeNode)treeModel.getRoot() ) ) ;
    }
    
    @Override
    protected void handleRightClickOnNode( DefaultMutableTreeNode node, MouseEvent e ) {
        this.selectedNode = node;
        JPopupMenu popup = new JPopupMenu() ;
        
        if( node.getUserObject() instanceof PageImage ) {
            popup = createPageImagePopupMenu() ;
        }
        
        if( popup.getComponentCount() > 0 ) {
            popup.show( e.getComponent(), e.getX(), e.getY() ) ;
        }
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
    public void actionPerformed( ActionEvent e ) {
        String command = ( ( JMenuItem )e.getSource() ).getText() ;
        Object userObject = selectedNode.getUserObject() ;
        
        switch( command ) {
            case "Close Tab" -> projectPanel.closePageImageTab( (PageImage )userObject ) ;
            case "Close Remaining Tabs" -> projectPanel.closeAllRemainingTabs( (PageImage )userObject ) ;
            case "Open Remaining Tabs" -> projectPanel.openAllRemainingTabs( (PageImage)userObject ) ;
        }
    }
}
