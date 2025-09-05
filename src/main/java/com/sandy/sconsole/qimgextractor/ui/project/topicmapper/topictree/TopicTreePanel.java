package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingUtilities.invokeLater;

@Slf4j
public class TopicTreePanel extends JPanel {
    
    public static final int PREFERRED_WIDTH = 400 ;
    
    @Getter
    private final TopicMapperUI topicMapperUI ;
    
    @Getter
    private TopicTree tree;
    
    public TopicTreePanel( TopicMapperUI topicMapperUI ) {
        this.topicMapperUI = topicMapperUI ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( createToolbar(), BorderLayout.NORTH ) ;
        add( createTreePanel(), BorderLayout.CENTER ) ;
        setPreferredSize( new Dimension( PREFERRED_WIDTH, 100 ) ) ;
    }
    
    private JPanel createToolbar() {
        
        JButton btnExpandAll = SwingUtils.getToolbarButton( "expand_all" ) ;
        btnExpandAll.addActionListener( e -> tree.setExpanded( true ) ) ;
        
        JButton btnCollapseAll = SwingUtils.getToolbarButton( "collapse_all" ) ;
        btnCollapseAll.addActionListener( e -> tree.setExpanded( false ) ) ;
        
        JPanel toolbarPanel = new JPanel() ;
        toolbarPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 10, 10 ) ) ;
        toolbarPanel.add( btnExpandAll ) ;
        toolbarPanel.add( btnCollapseAll ) ;
        return toolbarPanel ;
    }
    
    private JPanel createTreePanel() {
        
        tree = new TopicTree( topicMapperUI ) ;
        
        JScrollPane sp = new JScrollPane( VERTICAL_SCROLLBAR_AS_NEEDED,
                                          HORIZONTAL_SCROLLBAR_NEVER ) ;
        sp.setViewportView( tree ) ;
        
        JPanel treePanel = new JPanel() ;
        treePanel.setLayout( new BorderLayout() ) ;
        treePanel.add( sp, BorderLayout.CENTER ) ;
        
        return treePanel ;
    }
    
    public void refreshTree() {
        tree.refreshTree() ;
        invokeLater( () -> tree.setExpanded( true ) ) ;
    }
}
