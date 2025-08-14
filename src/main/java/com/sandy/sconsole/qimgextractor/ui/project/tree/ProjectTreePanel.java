package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.tree.pagequestion.PageQuestionTree;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

@Slf4j
public class ProjectTreePanel extends JPanel {
    
    public static final int PREFERRED_WIDTH = 300 ;
    
    @Getter
    private final ProjectPanel projectPanel ;
    
    @Getter
    private PageQuestionTree pageQuestionTree;
    
    public ProjectTreePanel( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        setUpUI() ;
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
        
        pageQuestionTree = new PageQuestionTree( projectPanel ) ;
        
        JScrollPane sp = new JScrollPane( VERTICAL_SCROLLBAR_AS_NEEDED,
                                          HORIZONTAL_SCROLLBAR_NEVER ) ;
        sp.setViewportView( pageQuestionTree ) ;
        
        JPanel treePanel = new JPanel() ;
        treePanel.setLayout( new BorderLayout() ) ;
        treePanel.add( sp, BorderLayout.CENTER ) ;
        
        return treePanel ;
    }
}
