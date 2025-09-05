package com.sandy.sconsole.qimgextractor.ui.project.topicmapper.classifier;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo;
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
    
    private static final int NUM_COLS = 4 ;
    private static final Font BTN_FONT = new Font( "SansSerif", Font.PLAIN, 18 ) ;
    
    private final TopicMapperUI parent ;
    
    private final CardLayout cardLayout = new CardLayout() ;
    private final JPanel physicsTopicsPanel = new JPanel() ;
    private final JPanel chemistryTopicsPanel = new JPanel() ;
    private final JPanel mathsTopicsPanel = new JPanel() ;
    
    
    public TopicSelectionPanel( TopicMapperUI parent ) {
        this.parent = parent ;
        prepareTopicsPanel( IIT_PHYSICS,   physicsTopicsPanel ) ;
        prepareTopicsPanel( IIT_CHEMISTRY, chemistryTopicsPanel ) ;
        prepareTopicsPanel( IIT_MATHS,     mathsTopicsPanel ) ;
        setUpUI() ;
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
        JButton button = new JButton() ;
        button.setText( "<html><div style='text-align:center'>" + topic.getName() + "</div></html>" ) ;
        button.setFont( BTN_FONT ) ;
        button.setOpaque( true ) ;
        button.setContentAreaFilled( true ) ;
        button.setBackground( getColor( topic ) ) ;
        button.addActionListener( e -> parent.associateTopicToSelectedQuestion( topic ) ) ;
        button.addKeyListener( new KeyAdapter() {
            public void keyPressed( KeyEvent e ) {
                transferFocusToNextButton( e.getKeyChar(), topicsPanel, button ) ;
            }
        } );
        return button ;
    }
    
    private void transferFocusToNextButton( char keyChar, JPanel topicsPanel, JButton currentButton ) {
        int numButtons = topicsPanel.getComponentCount() ;
        int currentButtonIndex = 0 ;
        
        for( int i=0; i<numButtons; i++ ) {
            JButton button = (JButton) topicsPanel.getComponent( i ) ;
            if( button == currentButton ) {
                currentButtonIndex = i ;
                break ;
            }
        }
        
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
    
    private boolean transferFocusIfFirstCharMatches( char keyChar, JPanel topicsPanel, int btnIndex ) {
        JButton button = (JButton) topicsPanel.getComponent( btnIndex ) ;
        String btnText = button.getText().substring( "<html><div style='text-align:center'>".length() ) ;
        if( btnText.toLowerCase().charAt( 0 ) == keyChar ) {
            button.requestFocus() ;
            return true ;
        }
        return false ;
    }
    
    private Color getColor( Topic topic ) {
        String syllabusName = topic.getSyllabusName() ;
        return switch( syllabusName ) {
            case IIT_PHYSICS -> Color.decode( "#FFC468" ).brighter().brighter();
            case IIT_CHEMISTRY -> Color.decode( "#84FF85" ).brighter();
            case IIT_MATHS -> Color.decode( "#97D6FF" ).brighter();
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
    
    public void showTopics( Question question ) {
        String subjectCode = "B" ;
        if( question != null ) {
            subjectCode = question.getQID().getSubjectCode() ;
        }
        switch( subjectCode ) {
            case "P" -> {
                cardLayout.show( this, IIT_PHYSICS ) ;
                physicsTopicsPanel.getComponent( 0 ).requestFocus() ;
            }
            case "C" -> {
                cardLayout.show( this, IIT_CHEMISTRY ) ;
                chemistryTopicsPanel.getComponent( 0 ).requestFocus() ;
            }
            case "M" -> {
                cardLayout.show( this, IIT_MATHS ) ;
                mathsTopicsPanel.getComponent( 0 ).requestFocus() ;
            }
            case "B" -> cardLayout.show( this, "Blank" ) ;
        }
    }
}
