package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
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
public class TopicMapperUI extends JPanel {
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final TopicTreePanel  topicTreePanel ;
    private final QuestionTreePanel questionTreePanel ;
    private final ClassifierPanel classifierPanel ;
    
    private Question selectedQuestion ;
    
    public TopicMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        
        this.topicTreePanel = new TopicTreePanel( this ) ;
        this.questionTreePanel = new QuestionTreePanel( this ) ;
        this.classifierPanel = new ClassifierPanel( this ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( this.topicTreePanel, BorderLayout.WEST ) ;
        add( this.classifierPanel, BorderLayout.CENTER ) ;
        add( this.questionTreePanel, BorderLayout.EAST ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        this.topicTreePanel.refreshTree() ;
        this.questionTreePanel.refreshTree() ;
        this.classifierPanel.displayQuestion( null ) ;
        this.topicTreePanel.getTree().selectNextUnclassifiedQuestion() ;
    }
    
    public void questionSelected( Question question, JTree tree ) {
        this.classifierPanel.displayQuestion( question ) ;
        if( tree instanceof TopicTree ) {
            questionTreePanel.getTree().clearSelection() ;
        }
        else if( tree instanceof QuestionTree ) {
            topicTreePanel.getTree().clearSelection() ;
        }
        this.selectedQuestion = question ;
    }
    
    public void associateTopicToSelectedQuestion( Topic topic ) {
        this.selectedQuestion.setTopic( topic ) ;
        this.topicTreePanel.getTree().refreshTree() ;
        this.topicTreePanel.getTree().setExpanded( true ) ;
        this.questionTreePanel.getTree().refreshTree() ;
        this.topicTreePanel.getTree().selectNextUnclassifiedQuestion() ;
        
        new SwingWorker<>() {
            protected Object doInBackground() {
                projectModel.getQuestionRepo().save() ;
                return null ;
            }
        }.execute() ;
    }
}
