package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.configurationprocessor.json.JSONObject;

/**
 * Records who associated a question with its current topic - a user, or an AI
 * model with a stated confidence. Persisted as {@code topic.topicAssociator} in
 * {@code question-info.json} and sent to the server on question sync.
 *
 * The associator belongs to the question's topic assignment, not to the topic
 * master, hence it lives on {@link Question} and not on {@link Topic}.
 */
@Getter
@RequiredArgsConstructor
public class TopicAssociator {

    public static final String ACTOR_AI   = "AI" ;
    public static final String ACTOR_USER = "User" ;

    private final String  actor ;
    private final String  model ;      // Null if not applicable or not known
    private final int     confidence ; // 0 - 100

    public static TopicAssociator user() {
        return new TopicAssociator( ACTOR_USER, null, 100 ) ;
    }

    public static TopicAssociator ai( String model, int confidence ) {
        return new TopicAssociator( ACTOR_AI, model, confidence ) ;
    }

    public static TopicAssociator deserialize( JSONObject json ) throws Exception {
        String actor = json.getString( "actor" ) ;
        String model = json.isNull( "model" ) ? null : json.getString( "model" ) ;
        int    confidence = json.getInt( "confidence" ) ;
        return new TopicAssociator( actor, model, confidence ) ;
    }

    public JSONObject getSerializedForm() throws Exception {
        JSONObject json = new JSONObject() ;
        json.put( "actor", actor ) ;
        json.put( "model", model != null ? model : JSONObject.NULL ) ;
        json.put( "confidence", confidence ) ;
        return json ;
    }

    @Override
    public String toString() {
        return actor + ":" + model + ":" + confidence ;
    }
}
