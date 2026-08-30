package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.AITopicSuggestion;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.AITopicSuggestionRepo;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo.* ;

@Slf4j
public class TopicSelectionPanel extends JPanel {
    
    private static final String BTN_HTML_PREFIX = "<html><div style='text-align:center'><span style='color:black'>" ;
    private static final String BTN_HTML_SUFFIX = "</div></html>" ;
    
    private static final int NUM_COLS = 4 ;
    private static final Font BTN_FONT = new Font( "SansSerif", Font.PLAIN, 18 ) ;
    
    private final TopicMapperUI parent ;
    
    private final CardLayout cardLayout = new CardLayout() ;
    private final JPanel physicsTopicsPanel = new JPanel() ;
    private final JPanel chemistryTopicsPanel = new JPanel() ;
    private final JPanel mathsTopicsPanel = new JPanel() ;
    
    private final AITopicSuggestionRepo aiSuggestionRepo ;

    public TopicSelectionPanel( TopicMapperUI parent, AITopicSuggestionRepo aiSuggestionRepo ) {
        this.parent = parent ;
        this.aiSuggestionRepo = aiSuggestionRepo ;
        prepareTopicsPanel( IIT_PHYSICS,   physicsTopicsPanel ) ;
        prepareTopicsPanel( IIT_CHEMISTRY, chemistryTopicsPanel ) ;
        prepareTopicsPanel( IIT_MATHS,     mathsTopicsPanel ) ;
        setUpUI() ;
        reloadAISuggestions() ;
    }
    
    private void prepareTopicsPanel( String syllabusName, JPanel topicsPanel ) {
        TopicRepo  topicRepo = QImgExtractor.getBean( TopicRepo.class ) ;
        List<Topic> topics = topicRepo.getTopicsBySyllabus( syllabusName ) ;
        int numRows = (topics.size()) / NUM_COLS + 1 ;
        if( topics.size() % (numRows-1) == 0 ) {
            numRows-- ;
        }
        
        topicsPanel.setLayout( new GridLayout( numRows, NUM_COLS, 5, 5 ) ) ;
        topicsPanel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) ;
        
        for( Topic topic : topics ) {
            topicsPanel.add( createTopicButton( topic, topicsPanel ) );
        }
    }
    
    private JButton createTopicButton( Topic topic, JPanel topicsPanel ) {
        JButton button = new JButton( getButtonText( topic ) ) ;
        button.setActionCommand( topic.getName() ) ;
        button.setFont( BTN_FONT ) ;
        button.setForeground( Color.GRAY ) ;
        button.setOpaque( true ) ;
        button.setContentAreaFilled( true ) ;
        button.setFocusPainted( true ) ;
        button.setMargin( new Insets( 5, 5, 5, 5 ) ) ;
        button.setBackground( getColor( topic ) ) ;
        button.addActionListener( e -> {
            resetButtonForegrounds( topicsPanel ) ;
            parent.associateTopicToSelectedQuestion( topic ) ;
        } ) ;
        button.addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
                if( e.getKeyCode() == KeyEvent.VK_UP ) {
                    parent.selectAdjacentQuestion( false ) ;
                }
                else if( e.getKeyCode() == KeyEvent.VK_DOWN ) {
                    parent.selectAdjacentQuestion( true ) ;
                }
                else {
                    highlightButtonsWithMatchingFirstCharacter( e, topicsPanel, topic ) ;
                    transferFocusToNextButton( e, topicsPanel, button ) ;
                }
            }
        } ) ;
        return button ;
    }
    
    private String getButtonText( Topic topic ) {
        return BTN_HTML_PREFIX +
                topic.getName().charAt( 0 ) +
                "</span>" +
                topic.getName().substring( 1 ) +
                BTN_HTML_SUFFIX;
    }
    
    private void resetButtonForegrounds( JPanel topicsPanel ) {
        for( int i=0; i<topicsPanel.getComponentCount(); i++ ) {
            JButton button = (JButton) topicsPanel.getComponent( i ) ;
            button.setForeground( Color.GRAY ) ;
        }
    }
    
    private void highlightButtonsWithMatchingFirstCharacter( KeyEvent ke, JPanel topicsPanel, Topic topic ) {
        char keyChar = Character.toLowerCase( ke.getKeyChar() ) ;
        int numButtons = topicsPanel.getComponentCount() ;
        
        for( int i=0; i<numButtons; i++ ) {
            JButton button = (JButton) topicsPanel.getComponent( i ) ;
            button.setForeground( Color.LIGHT_GRAY ) ;
            String btnText = button.getText().substring( BTN_HTML_PREFIX.length() ) ;
            if( btnText.toLowerCase().charAt( 0 ) == keyChar ) {
                button.setForeground( Color.GRAY ) ;
                button.setBackground( Color.GREEN.brighter() ) ;
            }
            else {
                button.setBackground( getColor( topic ) ) ;
            }
        }
    }
    
    private void transferFocusToNextButton( KeyEvent ke, JPanel topicsPanel, JButton currentButton ) {
        char keyChar = ke.getKeyChar() ;
        int numButtons = topicsPanel.getComponentCount() ;
        int currentButtonIndex = 0 ;
        
        for( int i=0; i<numButtons; i++ ) {
            JButton button = (JButton) topicsPanel.getComponent( i ) ;
            if( button == currentButton ) {
                currentButtonIndex = i ;
                break ;
            }
        }
        
        if( ke.isShiftDown() ) {
            for( int i=currentButtonIndex-1; i>=0; i-- ) {
                if( transferFocusIfFirstCharMatches( Character.toLowerCase( keyChar ), topicsPanel, i ) ) {
                    return ;
                }
            }
            for( int i=numButtons-1; i>currentButtonIndex; i-- ) {
                if( transferFocusIfFirstCharMatches( Character.toLowerCase( keyChar ), topicsPanel, i ) ) {
                    return ;
                }
            }
        }
        else {
            for( int i=currentButtonIndex+1; i<numButtons; i++ ) {
                if( transferFocusIfFirstCharMatches( keyChar, topicsPanel, i ) ) {
                    return ;
                }
            }
            for( int i=0; i<currentButtonIndex; i++ ) {
                if( transferFocusIfFirstCharMatches( keyChar, topicsPanel, i ) ) {
                    return ;
                }
            }
        }
    }
    
    private boolean transferFocusIfFirstCharMatches( char keyChar, JPanel topicsPanel, int btnIndex ) {
        JButton button = (JButton) topicsPanel.getComponent( btnIndex ) ;
        String btnText = button.getText().substring( BTN_HTML_PREFIX.length() ) ;
        if( btnText.toLowerCase().charAt( 0 ) == keyChar ) {
            button.setForeground( Color.RED ) ;
            button.requestFocus() ;
            return true ;
        }
        return false ;
    }
    
    private Color getColor( Topic topic ) {
        String syllabusName = topic.getSyllabusName() ;
        return switch( syllabusName ) {
            case IIT_PHYSICS -> Color.decode( "#FFC468" ).brighter() ;
            case IIT_CHEMISTRY -> Color.decode( "#84FF85" ).brighter() ;
            case IIT_MATHS -> Color.decode( "#97D6FF" ).brighter() ;
            default -> Color.LIGHT_GRAY;
        };
    }
    
    private void setUpUI() {
        setLayout( cardLayout ) ;
        add( physicsTopicsPanel, IIT_PHYSICS ) ;
        add( chemistryTopicsPanel, IIT_CHEMISTRY ) ;
        add( mathsTopicsPanel, IIT_MATHS ) ;
        add( new JPanel(), "Blank" ) ;
    }
    
    public void reloadAISuggestions() {
        aiSuggestionRepo.reload() ;
    }

    public void showTopics( Question question ) {
        String subjectCode = "B" ;
        List<AITopicSuggestion> suggestedTopics = null ;

        if( question != null ) {
            subjectCode = question.getQID().getSubjectCode() ;
            suggestedTopics = aiSuggestionRepo.getSuggestions( question ) ;
        }
        
        switch( subjectCode ) {
            case "P" -> {
                cardLayout.show( this, IIT_PHYSICS ) ;
                setFocus( physicsTopicsPanel, question, suggestedTopics ) ;
            }
            case "C" -> {
                cardLayout.show( this, IIT_CHEMISTRY ) ;
                setFocus( chemistryTopicsPanel, question, suggestedTopics ) ;
            }
            case "M" -> {
                cardLayout.show( this, IIT_MATHS ) ;
                setFocus( mathsTopicsPanel, question, suggestedTopics ) ;
            }
            case "B" -> cardLayout.show( this, "Blank" ) ;
        }
    }
    
    private void setFocus( JPanel topicPanel, Question question,
                           List<AITopicSuggestion> suggestedTopics ) {

        Topic topic = question.getTopic() ;
        resetButtonForegrounds( topicPanel ) ;

        if( topic == null ) {
            if( suggestedTopics != null && !suggestedTopics.isEmpty()
                    && suggestedTopics.get( 0 ).getTopic() != null ) {
                Topic suggestedTopic = suggestedTopics.get( 0 ).getTopic() ;
                for( int j = 0; j < topicPanel.getComponentCount(); j++ ) {
                    JButton button = ( JButton )topicPanel.getComponent( j );
                    if( button.getActionCommand().equals( suggestedTopic.getName() ) ) {
                        button.setForeground( Color.RED ) ;
                        button.requestFocus();
                        return;
                    }
                }
            }
            else {
                topicPanel.getComponent( 0 ).requestFocus() ;
            }
        }
        else {
            for( int i=0; i<topicPanel.getComponentCount(); i++ ) {
                JButton button = (JButton) topicPanel.getComponent( i ) ;
                if( button.getActionCommand().equals( topic.getName() ) ) {
                    button.requestFocus() ;
                    return ;
                }
            }
        }
    }
    
}
