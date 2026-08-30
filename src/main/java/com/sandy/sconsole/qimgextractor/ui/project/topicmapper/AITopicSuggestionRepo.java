package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicRepo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Loads {@code <project>/.workspace/ai-topic-map.json} and exposes the AI topic
 * recommendations - including the confidence level, which the raw JSON parsing in
 * {@code TopicSelectionPanel} used to discard.
 *
 * The map is keyed by {@code QID.toString()} (e.g. {@code C/SCA/32}); each value is
 * the recommendation list sorted by confidence level, highest first.
 */
@Slf4j
public class AITopicSuggestionRepo {

    private final Map<String, List<AITopicSuggestion>> suggestionsByQId = new HashMap<>() ;

    public void reload() {
        suggestionsByQId.clear() ;

        ProjectModel projectModel = QImgExtractor.getBean( ProjectModel.class ) ;
        File aiTopicMapFile = new File( projectModel.getWorkDir(), "ai-topic-map.json" ) ;
        if( !aiTopicMapFile.exists() ) {
            return ;
        }

        try {
            String      content = FileUtils.readFileToString( aiTopicMapFile, "UTF-8" ) ;
            JSONObject  json = new JSONObject( content ) ;
            Iterator<?> keys = json.keys() ;
            while( keys.hasNext() ) {
                String     key = (String)keys.next() ;
                JSONObject value = json.getJSONObject( key ) ;
                JSONArray  topicMappings = value.getJSONArray( "topicMappings" ) ;
                if( topicMappings.length() > 0 ) {
                    parseSuggestions( key, topicMappings ) ;
                }
            }
        }
        catch( Exception e ) {
            log.error( "Error loading AI topic suggestions", e ) ;
        }
    }

    private void parseSuggestions( String questionId, JSONArray topicMappings )
            throws JSONException {

        TopicRepo topicRepo = QImgExtractor.getBean( TopicRepo.class ) ;

        List<AITopicSuggestion> suggestions = new ArrayList<>() ;
        for( int i=0; i<topicMappings.length(); i++ ) {
            JSONObject mapping = topicMappings.getJSONObject( i ) ;
            int   topicId = mapping.getInt( "topicId" ) ;
            int   confidence = mapping.optInt( "confidenceLevel", 0 ) ;
            Topic topic = topicRepo.getTopicById( topicId ) ;
            suggestions.add( new AITopicSuggestion( topic, confidence ) ) ;
        }

        suggestions.sort( ( a, b ) -> b.getConfidenceLevel() - a.getConfidenceLevel() ) ;
        suggestionsByQId.put( questionId.replace( "_", "/" ), suggestions ) ;
    }

    /**
     * @return the recommendation list (confidence-descending) for the given
     *         question, or {@code null} if the AI map has no entry for it.
     */
    public List<AITopicSuggestion> getSuggestions( Question question ) {
        return suggestionsByQId.get( question.getQID().toString() ) ;
    }
}
