package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectPanel;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier.ClassifierPanel;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree.QuestionTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.qtree.QuestionTreePanel;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree.TopicTree;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.topictree.TopicTreePanel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.util.List;

@Slf4j
public class TopicMapperUI extends JPanel {
    
    @Getter
    private final ProjectPanel projectPanel ; // Injected
    
    @Getter
    private final ProjectModel projectModel ; // Injected
    
    private final TopicTreePanel  topicTreePanel ;
    private final QuestionTreePanel questionTreePanel ;
    private final ClassifierPanel classifierPanel ;
    private final AITopicSuggestionRepo aiSuggestionRepo ;

    private Question selectedQuestion ;

    public TopicMapperUI( ProjectPanel projectPanel ) {
        this.projectPanel = projectPanel ;
        this.projectModel = projectPanel.getProjectModel() ;
        this.aiSuggestionRepo = new AITopicSuggestionRepo() ;

        this.topicTreePanel = new TopicTreePanel( this ) ;
        this.questionTreePanel = new QuestionTreePanel( this ) ;
        this.classifierPanel = new ClassifierPanel( this, aiSuggestionRepo ) ;

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
            showAISuggestionsInStatusBar( question ) ;
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
        boolean wasUnclassified = selectedQuestion.getTopic() == null ;
        Question nextQuestion = wasUnclassified ?
            topicTreePanel.getTree().getNextUnclassifiedQuestion( selectedQuestion ) : null ;

        selectedQuestion.setTopic( topic ) ;
        topicTreePanel.getTree().refreshTree() ;
        questionTreePanel.getTree().refreshTree() ;

        if( nextQuestion != null ) {
            topicTreePanel.getTree().selectQuestion( nextQuestion ) ;
        }
        else if( !topicTreePanel.getTree().selectNextUnclassifiedQuestion() ) {
            topicTreePanel.getTree().selectQuestion( selectedQuestion ) ;
        }

        saveQuestionsInBackground() ;
    }

    public void associateTopicToAllUnclassifiedQuestions( Topic topic ) {

        String syllabusName = topic.getSyllabusName() ;
        List<Question> targets = projectModel.getQuestionRepo().getQuestionList()
                .stream()
                .filter( q -> q.getTopic() == null )
                .filter( q -> syllabusName.equals( syllabusForSubject( q.getQID().getSubjectCode() ) ) )
                .toList() ;

        if( targets.isEmpty() ) {
            QImgExtractor.logStatusMsg( "No unclassified " + syllabusName + " questions" ) ;
            return ;
        }

        int choice = JOptionPane.showConfirmDialog( SwingUtilities.getWindowAncestor( this ),
                "Associate \"" + topic.getName() + "\" with " + targets.size() +
                " unclassified " + syllabusName + " question(s)?",
                "Bulk Topic Association", JOptionPane.OK_CANCEL_OPTION ) ;
        if( choice != JOptionPane.OK_OPTION ) {
            return ;
        }

        targets.forEach( q -> q.setTopic( topic ) ) ;

        topicTreePanel.refreshTree() ;
        questionTreePanel.refreshTree() ;
        if( !topicTreePanel.getTree().selectNextUnclassifiedQuestion() ) {
            classifierPanel.displayQuestion( null ) ;
        }

        saveQuestionsInBackground() ;
        QImgExtractor.logStatusMsg( "Associated \"" + topic.getName() + "\" with " +
                                    targets.size() + " question(s)" ) ;
    }

    private static String syllabusForSubject( String subjectCode ) {
        return switch( subjectCode ) {
            case "P" -> TopicRepo.IIT_PHYSICS ;
            case "C" -> TopicRepo.IIT_CHEMISTRY ;
            case "M" -> TopicRepo.IIT_MATHS ;
            default -> null ;
        } ;
    }

    private void saveQuestionsInBackground() {
        new SwingWorker<>() {
            protected Object doInBackground() {
                projectModel.getQuestionRepo().save() ;

                // If we are going back to a more nascent stage, then
                // erase the advanced stage markers
                if( projectModel.getState().isSavedToServer() ) {
                    projectModel.getState().setTopicsMapped( true ) ;
                }
                return null ;
            }
        }.execute() ;
    }

    private static final String AI_SUGGESTION_SEPARATOR = "      |      " ;

    private void showAISuggestionsInStatusBar( Question question ) {
        if( question == null ) {
            QImgExtractor.logAISuggestionMsg( " " ) ;
            return ;
        }

        List<AITopicSuggestion> suggestions = aiSuggestionRepo.getSuggestions( question ) ;
        if( suggestions == null || suggestions.isEmpty() ) {
            QImgExtractor.logAISuggestionMsg( "No AI recommendations" ) ;
            return ;
        }

        StringBuilder sb = new StringBuilder() ;
        for( int i=0; i<suggestions.size(); i++ ) {
            AITopicSuggestion s = suggestions.get( i ) ;
            if( i > 0 ) {
                sb.append( AI_SUGGESTION_SEPARATOR ) ;
            }
            String name = s.getTopic() != null ? s.getTopic().getName() : "?" ;
            sb.append( name ).append( " (" ).append( s.getConfidenceLevel() ).append( ')' ) ;
        }
        QImgExtractor.logAISuggestionMsg( sb.toString() ) ;
    }

    public void selectAdjacentQuestion( boolean forward ) {
        topicTreePanel.getTree().selectAdjacentQuestion( forward ) ;
    }
    
    public void reloadAISuggestions() {
        classifierPanel.reloadAISuggestions() ;
    }

    public void autoAssociateTopics() {

        AutoAssociationReport report =
                new AutoTopicAssociator( projectModel, aiSuggestionRepo ).run() ;

        topicTreePanel.refreshTree() ;
        questionTreePanel.refreshTree() ;
        if( !topicTreePanel.getTree().selectNextUnclassifiedQuestion() ) {
            classifierPanel.displayQuestion( null ) ;
        }

        saveQuestionsInBackground() ;

        QImgExtractor.logStatusMsg( "Auto-associated " + report.mapped + " topic(s)" ) ;
        showReport( report ) ;
    }

    private void showReport( AutoAssociationReport report ) {
        JTextArea textArea = new JTextArea( report.toDisplayString() ) ;
        textArea.setEditable( false ) ;
        textArea.setFont( new Font( "Courier New", Font.PLAIN, 13 ) ) ;
        textArea.setColumns( 60 ) ;
        textArea.setRows( 8 ) ;

        JOptionPane.showMessageDialog( SwingUtilities.getWindowAncestor( this ),
                new JScrollPane( textArea ), "Auto Topic Association",
                JOptionPane.INFORMATION_MESSAGE ) ;
    }
}
