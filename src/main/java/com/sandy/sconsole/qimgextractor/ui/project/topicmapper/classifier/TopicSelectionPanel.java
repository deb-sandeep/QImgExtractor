package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo;
import com.sandy.sconsole.qimgextractor.ui.project.topicmapper.TopicMapperUI;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.*;
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
    
    private final Map<String, List<Topic>> aiTopicMap = new HashMap<>() ;
    
    public TopicSelectionPanel( TopicMapperUI parent ) {
        this.parent = parent ;
        prepareTopicsPanel( IIT_PHYSICS,   physicsTopicsPanel ) ;
        prepareTopicsPanel( IIT_CHEMISTRY, chemistryTopicsPanel ) ;
        prepareTopicsPanel( IIT_MATHS,     mathsTopicsPanel ) ;
        setUpUI() ;
        loadAITopicMappings() ;
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
        button.setFont( BTN_FONT ) ;
        button.setForeground( Color.GRAY ) ;
        button.setOpaque( true ) ;
        button.setContentAreaFilled( true ) ;
        button.setFocusPainted( true ) ;
        button.setMargin( new Insets( 5, 5, 5, 5 ) ) ;
        button.setBackground( getColor( topic ) ) ;
        button.addActionListener( e -> {
            parent.associateTopicToSelectedQuestion( topic ) ;
            resetButtonForegrounds( topicsPanel ) ;
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
    
    private void loadAITopicMappings() {
        ProjectModel projectModel = QImgExtractor.getBean( ProjectModel.class ) ;
        File aiTopicMapFile = new File( projectModel.getWorkDir(), "ai-topic-map.json" ) ;
        if( aiTopicMapFile.exists() ) {
            try {
                String      content = FileUtils.readFileToString( aiTopicMapFile, "UTF-8" ) ;
                JSONObject  json = new JSONObject( content ) ;
                Iterator<?> keys = json.keys() ;
                while( keys.hasNext() ) {
                    String key = (String)keys.next() ;
                    JSONObject value = json.getJSONObject( key ) ;
                    JSONArray topicMappings = value.getJSONArray( "topicMappings" ) ;
                    if( topicMappings.length() > 0 ) {
                        populateAITopicMap( key, topicMappings ) ;
                    }
                }
            }
            catch( Exception e ) {
                log.error( "Error synchronizing with persisted state", e );
            }
        }
    }
    
    private void populateAITopicMap( String questionId, JSONArray topicMappings )
            throws JSONException {
        
        TopicRepo topicRepo = QImgExtractor.getBean( TopicRepo.class ) ;
        
        List<Topic> suggestedTopics = new ArrayList<>() ;
        for( int i=0; i<topicMappings.length(); i++ ) {
            JSONObject mapping = topicMappings.getJSONObject( i ) ;
            int topicId = mapping.getInt( "topicId" ) ;
            suggestedTopics.add( topicRepo.getTopicById( topicId ) ) ;
        }
        
        aiTopicMap.put( questionId.replace( "_", "/" ), suggestedTopics ) ;
    }
    
    public void showTopics( Question question ) {
        String subjectCode = "B" ;
        List<Topic> suggestedTopics = null ;
        
        if( question != null ) {
            subjectCode = question.getQID().getSubjectCode() ;
            suggestedTopics = aiTopicMap.get( question.getQID().toString() ) ;
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
                           List<Topic> suggestedTopics ) {
        
        Topic topic = question.getTopic() ;
        
        if( topic == null ) {
            if( suggestedTopics != null && !suggestedTopics.isEmpty() ) {
                Topic suggestedTopic = suggestedTopics.get( 0 ) ;
                for( int j = 0; j < topicPanel.getComponentCount(); j++ ) {
                    JButton button = ( JButton )topicPanel.getComponent( j );
                    if( button.getText().contains( suggestedTopic.getName().substring( 1 ) ) ) {
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
                if( button.getText().contains( topic.getName().substring( 1 ) ) ) {
                    button.requestFocus() ;
                    return ;
                }
            }
        }
    }
}
