package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree;

import com.sandy.sconsole.qimgextractor.ui.project.model.*;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class TopicTreeModel extends DefaultTreeModel
    implements ProjectModelListener {
    
    private final ProjectModel projectModel ; // Injected
    
    private final DefaultMutableTreeNode rootNode ;
    
    private final Map<String, Map<Topic, List<Question>>> topicQMap = new HashMap<>() ;
    
    @Getter private DefaultMutableTreeNode phyNode ;
    @Getter private DefaultMutableTreeNode chemNode ;
    @Getter private DefaultMutableTreeNode mathsNode ;
    @Getter private DefaultMutableTreeNode unclassifiedNode ;
    
    public TopicTreeModel( ProjectModel projectModel ) {
        
        super( new DefaultMutableTreeNode( projectModel.getProjectName() ) );
        this.projectModel = projectModel;
        this.projectModel.addListener( this ) ;
        this.rootNode = ( DefaultMutableTreeNode )super.getRoot() ;
        buildTree() ;
    }
    
    public void buildTree() {
        this.refreshTopicQuestionMap() ;
        this.rootNode.removeAllChildren() ;
        this.phyNode = buildRootNode( "IIT Physics" ) ;
        this.chemNode = buildRootNode( "IIT Chemistry" ) ;
        this.mathsNode = buildRootNode( "IIT Maths" ) ;
        this.unclassifiedNode = buildRootNode( "Unclassified" ) ;
        super.nodeStructureChanged( this.rootNode ) ;
    }
    
    private void refreshTopicQuestionMap() {
        this.topicQMap.clear() ;
        for( Question question : projectModel.getQuestionRepo().getQuestionList() ) {
            Topic topic = question.getTopic() ;
            if( topic == null ) {
                addQuestionToTopicQMap( "Unclassified", new Topic( 0, "Unclassified", "Unclassified" ), question ) ;
            }
            else {
                addQuestionToTopicQMap( topic.getSyllabusName(), topic, question ) ;
            }
        }
    }
    
    private void addQuestionToTopicQMap( String syllabusName, Topic topic, Question question ) {
        topicQMap.computeIfAbsent( syllabusName, k -> new HashMap<>() )
                 .computeIfAbsent( topic, k -> new ArrayList<>() )
                 .add( question ) ;
    }
    
    private DefaultMutableTreeNode buildRootNode( String syllabusName ) {
        DefaultMutableTreeNode syllabusNode = new DefaultMutableTreeNode( syllabusName ) ;
        this.rootNode.add( syllabusNode ) ;
        
        for( Topic topic : topicQMap.getOrDefault( syllabusName, new HashMap<>() ).keySet().stream().sorted().toList() ) {
            String topicName = topic.getName() ;
            if( !topicName.equals( "Unclassified" ) ) {
                DefaultMutableTreeNode topicNode = new DefaultMutableTreeNode( topicName ) ;
                syllabusNode.add( topicNode ) ;
                for( Question question : topicQMap.getOrDefault( syllabusName, new HashMap<>() ).get( topic ).stream().sorted().toList() ) {
                    topicNode.add( new DefaultMutableTreeNode( question ) ) ;
                }
            }
            else {
                for( Question question : topicQMap.getOrDefault( syllabusName, new HashMap<>() ).get( topic ).stream().sorted().toList() ) {
                    syllabusNode.add( new DefaultMutableTreeNode( question ) ) ;
                }
            }
        }
        return syllabusNode ;
    }
    
    @Override
    public void newQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {
        buildTree() ;
    }
    
    @Override
    public void questionTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {
        buildTree() ;
    }
    
    @Override
    public void questionImgDeleted( QuestionImage qImg ) {
        buildTree();
    }
    
    @Override
    public void partSelectionModeUpdated( boolean newMode ) {/* Ignore */}
}
