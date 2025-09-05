package com.sandy.sconsole.qimgextractor.ui.project.model;

import jakarta.annotation.PostConstruct;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class TopicRepo {
    
    public static final String IIT_PHYSICS = "IIT Physics" ;
    public static final String IIT_MATHS = "IIT Maths" ;
    public static final String IIT_CHEMISTRY = "IIT Chemistry" ;
    
    private final Map<Integer, Topic> topicById = new HashMap<>();
    private final Map<String, List<Topic>> topicsBySyllabus = new HashMap<>();
    
    @PostConstruct
    public void init() {
        try {
            String content = IOUtils.toString(
                    Objects.requireNonNull( getClass().getResourceAsStream( "/config/topic_master.json" ) ),
                    StandardCharsets.UTF_8 ) ;
            JSONArray topicsArray = new JSONArray( content ) ;
            
            for( int i = 0; i < topicsArray.length(); i++ ) {
                JSONObject topicJson = topicsArray.getJSONObject( i ) ;
                Topic topic = new Topic();
                topic.setId( topicJson.getInt( "id" ) ) ;
                topic.setName( topicJson.getString( "topic_name" ) ) ;
                topic.setSyllabusName( topicJson.getString( "syllabus_name" ) ) ;
                
                topicById.put( topic.getId(), topic );
                topicsBySyllabus.computeIfAbsent( topic.getSyllabusName(), k -> new ArrayList<>() ).add( topic );
            }
        }
        catch( Exception e ) {
            log.error( "Error loading topics", e );
        }
    }
    
    public Topic getTopicById( int id ) {
        return topicById.get( id );
    }
    
    public List<Topic> getTopicsBySyllabus( String syllabusName ) {
        return topicsBySyllabus.getOrDefault( syllabusName, Collections.emptyList() );
    }
}
