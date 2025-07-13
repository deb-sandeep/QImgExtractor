package com.sandy.sconsole.qimgextractor.ui.project.tree;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;

import javax.swing.*;
import java.awt.*;

public class ProjectPageTree extends JPanel {
    
    public static final int PREFERRED_WIDTH = 300 ;
    
    private final ProjectPanel projectPanel ;
    
    public ProjectPageTree( ProjectPanel projectPanel ) {
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
        JPanel treePanel = new JPanel() ;
        return treePanel ;
    }
}
