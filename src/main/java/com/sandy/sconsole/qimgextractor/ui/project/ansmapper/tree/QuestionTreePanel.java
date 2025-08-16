package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;

@Slf4j
public class QuestionTreePanel extends JPanel {
    
    public static final int PREFERRED_WIDTH = 250 ;
    
    @Getter
    private final AnswerMapperUI answerMapperUI ;
    
    @Getter
    private QuestionTree tree;
    
    public QuestionTreePanel( AnswerMapperUI answerMapperUI ) {
        this.answerMapperUI = answerMapperUI ;
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
        btnCollapseAll.addActionListener( e -> tree.setExpanded( false ) );
        
        JPanel toolbarPanel = new JPanel() ;
        toolbarPanel.setLayout( new FlowLayout( FlowLayout.LEFT, 10, 10 ) ) ;
        toolbarPanel.add( btnExpandAll ) ;
        toolbarPanel.add( btnCollapseAll ) ;
        return toolbarPanel ;
    }
    
    private JPanel createTreePanel() {
        
        tree = new QuestionTree( answerMapperUI ) ;
        
        JScrollPane sp = new JScrollPane( VERTICAL_SCROLLBAR_AS_NEEDED,
                                          HORIZONTAL_SCROLLBAR_NEVER ) ;
        sp.setViewportView( tree ) ;
        
        JPanel treePanel = new JPanel() ;
        treePanel.setLayout( new BorderLayout() ) ;
        treePanel.add( sp, BorderLayout.CENTER ) ;
        
        return treePanel ;
    }
}
