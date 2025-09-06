package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

import static javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER;
import static javax.swing.ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED;
import static javax.swing.SwingUtilities.invokeLater;

@Slf4j
public class QuestionTreePanel extends JPanel {
    
    public static final int PREFERRED_WIDTH = 150 ;
    
    @Getter
    private final TopicMapperUI topicMapperUI ;
    
    @Getter
    private QuestionTree tree;
    
    public QuestionTreePanel( TopicMapperUI topicMapperUI ) {
        this.topicMapperUI = topicMapperUI ;
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( createTreePanel(), BorderLayout.CENTER ) ;
        setPreferredSize( new Dimension( PREFERRED_WIDTH, 100 ) ) ;
    }
    
    private JPanel createTreePanel() {
        
        tree = new QuestionTree( topicMapperUI ) ;
        
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
    }
}
