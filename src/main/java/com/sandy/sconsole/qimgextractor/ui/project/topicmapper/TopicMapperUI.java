package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
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
        add( topicTreePanel, BorderLayout.WEST ) ;
        add( classifierPanel, BorderLayout.CENTER ) ;
        add( questionTreePanel, BorderLayout.EAST ) ;
    }
    
    // This method is called just before the panel is made visible. Can be used
    // to update the UI state based on any changes that have happened through
    // other project modules.
    public void handlePreActivation() {
        topicTreePanel.refreshTree() ;
        questionTreePanel.refreshTree() ;
        boolean nextQSelected = topicTreePanel.getTree().selectNextUnclassifiedQuestion() ;
        if( !nextQSelected ) {
            classifierPanel.displayQuestion( null ) ;
        }
    }
    
    public void questionSelected( Question question, JTree tree ) {
        if( selectedQuestion != question ) {
            selectedQuestion = question ;
            classifierPanel.displayQuestion( question ) ;
            topicTreePanel.getTree().expandTreeIntelligently( question ) ;
            if( tree instanceof TopicTree ) {
                questionTreePanel.getTree().selectQuestion( question ) ;
            }
            else if( tree instanceof QuestionTree ) {
                topicTreePanel.getTree().selectQuestion( question ) ;
            }
        }
    }
    
    public void associateTopicToSelectedQuestion( Topic topic ) {
        selectedQuestion.setTopic( topic ) ;
        topicTreePanel.getTree().refreshTree() ;
        questionTreePanel.getTree().refreshTree() ;
        if( !topicTreePanel.getTree().selectNextUnclassifiedQuestion() ) {
            topicTreePanel.getTree().selectQuestion( selectedQuestion ) ;
        }
        
        new SwingWorker<>() {
            protected Object doInBackground() {
                projectModel.getQuestionRepo().save() ;
                
                // If we are going back to a more nascent stage, then
                // erase the advanced stage markers
                if( projectModel.getState().isSavedToServer() ) {
                    projectModel.getState().setTopicsMapped( true ); ;
                }
                
                return null ;
            }
        }.execute() ;
    }
    
    public void selectAdjacentQuestion( boolean forward ) {
        topicTreePanel.getTree().selectAdjacentQuestion( forward ) ;
    }
}
