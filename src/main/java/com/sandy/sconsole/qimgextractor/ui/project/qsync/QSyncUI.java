package com.sandy.sconsole.qimgextractor.ui.project.qsync;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSTreeTableModel;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSyncTreeTable;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class QSyncUI extends JPanel {
    
    public static final String AC_SHOW_ALL          = "QSyncUI.showAll" ;
    public static final String AC_SHOW_SYNC_PENDING = "QSyncUI.showSyncPending" ;
    public static final String AC_COLLAPSE_ALL      = "QSyncUI.collapseAll" ;
    public static final String AC_EXPAND_ALL        = "QSyncUI.expandAll" ;
    public static final String AC_EXPAND_SYLLABUS   = "QSyncUI.expandSyllabus" ;
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final QSTreeTableModel model ;
    private final QSyncTreeTable treeTable ;
    
    public QSyncUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        
        this.model = new QSTreeTableModel( this.projectModel ) ;
        this.treeTable = new QSyncTreeTable( this.model ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        
        this.treeTable.setRootVisible( false ) ;
        this.treeTable.expandSyllabus() ;
        JScrollPane sp = new JScrollPane( treeTable ) ;
        add( sp, BorderLayout.CENTER ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
    }
    
    public void handleMenuAction( String actionCommand ) {
        if( AC_EXPAND_ALL.equals( actionCommand ) ) {
            treeTable.expandAll() ;
        }
        else if( AC_COLLAPSE_ALL.equals( actionCommand ) ) {
            treeTable.collapseAll() ;
        }
        else if( AC_EXPAND_SYLLABUS.equals( actionCommand ) ) {
            treeTable.expandSyllabus() ;
        }
    }
}
