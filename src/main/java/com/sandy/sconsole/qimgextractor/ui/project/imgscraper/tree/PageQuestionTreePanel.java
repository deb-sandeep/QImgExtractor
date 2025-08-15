package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.tree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

@Slf4j
public class PageQuestionTreePanel extends JPanel {
    
    public static final int PREFERRED_WIDTH = 300 ;
    
    @Getter
    private final ImageScraperUI scraperUI;
    
    @Getter
    private PageQuestionTree tree;
    
    public PageQuestionTreePanel( ImageScraperUI scraperUI ) {
        this.scraperUI = scraperUI;
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
        btnExpandAll.addActionListener( e -> tree.expandAll() ) ;
        
        JButton btnCollapseAll = SwingUtils.getToolbarButton( "collapse_all" ) ;
        btnCollapseAll.addActionListener( e -> tree.collapseAll() );
        
        JPanel toolbarPanel = new JPanel() ;
        toolbarPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 10, 10 ) ) ;
        toolbarPanel.add( btnExpandAll ) ;
        toolbarPanel.add( btnCollapseAll ) ;
        return toolbarPanel ;
    }
    
    private JPanel createTreePanel() {
        
        tree = new PageQuestionTree( scraperUI ) ;
        
        JScrollPane sp = new JScrollPane( VERTICAL_SCROLLBAR_AS_NEEDED,
                                          HORIZONTAL_SCROLLBAR_NEVER ) ;
        sp.setViewportView( tree ) ;
        
        JPanel treePanel = new JPanel() ;
        treePanel.setLayout( new BorderLayout() ) ;
        treePanel.add( sp, BorderLayout.CENTER ) ;
        
        return treePanel ;
    }
}
