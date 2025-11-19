package com.sandy.sconsole.qimgextractor.ui.project.qsync;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSTreeTableModel;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSyncTreeTable;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier.ClassifierPanel;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree.QuestionTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree.QuestionTreePanel;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree.TopicTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree.TopicTreePanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class QSyncUI extends JPanel {
    
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
        this.treeTable.expandAll() ;
        JScrollPane sp = new JScrollPane( treeTable ) ;
        add( sp, BorderLayout.CENTER ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
    }
}
