package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicAssociator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Records the topic associator of questions whose topics were associated before
 * provenance was recorded. Those topics were associated from the gemma4
 * recommendations in {@code ai-topic-map.json}, so a topic equal to the top
 * recommendation is attributed to the AI with that recommendation's confidence.
 * Any other topic was picked by the user.
 *
 * Projects and questions already synced to a server are left untouched, as
 * changing their provenance would mark them as modified after sync.
 */
@Slf4j
public class TopicAssociatorBackfill {

    // The model that produced the recommendations before provenance was recorded
    private static final String LEGACY_AI_MODEL = "gemma4" ;

    /**
     * @return the number of questions whose topic associator was set.
     */
    public static int run( ProjectModel projectModel ) {

        if( projectModel.getState().isSavedToServer() ) {
            return 0 ;
        }

        AITopicSuggestionRepo suggestionRepo = new AITopicSuggestionRepo() ;
        suggestionRepo.reload() ;

        int numAI = 0 ;
        int numUser = 0 ;
        for( Question q : projectModel.getQuestionRepo().getQuestionList() ) {
            if( q.getTopic() == null ||
                q.getTopicAssociator() != null ||
                q.isSyncedToAnyServer() ) {
                continue ;
            }

            List<AITopicSuggestion> suggestions = suggestionRepo.getSuggestions( q ) ;
            AITopicSuggestion top = suggestions != null && !suggestions.isEmpty() ?
                                    suggestions.get( 0 ) : null ;

            if( top != null && top.getTopic() != null &&
                top.getTopic().getId() == q.getTopic().getId() ) {
                AIClassifiedBy classifiedBy = suggestionRepo.getClassifiedBy( q ) ;
                String model = classifiedBy != null && classifiedBy.getModel() != null ?
                               classifiedBy.getModel() : LEGACY_AI_MODEL ;
                q.setTopic( q.getTopic(), TopicAssociator.ai( model, top.getConfidenceLevel() ) ) ;
                numAI++ ;
            }
            else {
                q.setTopic( q.getTopic(), TopicAssociator.user() ) ;
                numUser++ ;
            }
        }

        if( numAI + numUser > 0 ) {
            log.info( "    Backfilled topic associators. AI = {}, User = {}", numAI, numUser ) ;
        }
        return numAI + numUser ;
    }
}
