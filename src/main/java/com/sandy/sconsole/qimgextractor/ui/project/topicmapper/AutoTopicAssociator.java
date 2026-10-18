package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicAssociator;
import com.sandy.sconsole.topicresolver.TopicResolver;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Walks every unclassified question in the project and associates the topic
 * {@link TopicResolver} chooses from its AI recommendations, irrespective of
 * its confidence level. There is deliberately no quality gating here - the
 * associations are reviewed later by a stronger model.
 *
 * Only recommendations whose topic id resolves against the topic master are
 * given to the resolver. Questions with no recommendation are left untouched.
 * If the topic sequence file can't be read, nothing is associated and the
 * failure is recorded in the report.
 */
@Slf4j
public class AutoTopicAssociator {

    private final ProjectModel projectModel ;
    private final AITopicSuggestionRepo suggestionRepo ;

    public AutoTopicAssociator( ProjectModel projectModel,
                                AITopicSuggestionRepo suggestionRepo ) {
        this.projectModel = projectModel ;
        this.suggestionRepo = suggestionRepo ;
    }

    public AutoAssociationReport run() {

        AutoAssociationReport report = new AutoAssociationReport() ;

        TopicResolver resolver ;
        try {
            resolver = TopicResolver.current() ;
        }
        catch( IOException e ) {
            log.error( "Could not load the topic sequence {}. No topics associated.",
                       TopicResolver.SEQUENCE_FILE, e ) ;
            report.failure = "Could not load the topic sequence " +
                             TopicResolver.SEQUENCE_FILE + "\n" + e.getMessage() ;
            return report ;
        }

        suggestionRepo.reload() ;

        for( Question question : projectModel.getQuestionRepo().getQuestionList() ) {
            report.total++ ;

            if( question.getTopic() != null ) {
                report.alreadyClassified++ ;
                continue ;
            }

            List<AITopicSuggestion> suggestions = suggestionRepo.getSuggestions( question ) ;
            if( suggestions == null || suggestions.isEmpty() ) {
                report.noRecommendation++ ;
                continue ;
            }

            AITopicSuggestion chosen = chooseSuggestion( resolver, suggestions ) ;
            if( chosen != null ) {
                AIClassifiedBy classifiedBy = suggestionRepo.getClassifiedBy( question ) ;
                String model = classifiedBy != null ? classifiedBy.getModel() : null ;
                question.setTopic( chosen.getTopic(),
                                   TopicAssociator.ai( model, chosen.getConfidenceLevel() ) ) ;
                report.mapped++ ;
            }
            else {
                report.unresolvedTopic++ ;
            }
        }

        return report ;
    }

    /**
     * @param suggestions recommendations for the question, in any order.
     * @return the recommendation the resolver chooses among those whose topic
     *         resolved, or {@code null} if none of the recommended topic ids
     *         are known.
     */
    public static AITopicSuggestion chooseSuggestion( TopicResolver resolver,
                                                      List<AITopicSuggestion> suggestions ) {

        List<TopicResolver.Recommendation> recommendations = new ArrayList<>() ;
        for( AITopicSuggestion suggestion : suggestions ) {
            if( suggestion.getTopic() != null ) {
                recommendations.add( new TopicResolver.Recommendation(
                        suggestion.getTopic().getId(), suggestion.getConfidenceLevel() ) ) ;
            }
        }

        TopicResolver.Resolution resolution = resolver.resolve( recommendations ) ;
        if( resolution == null ) {
            return null ;
        }
        for( AITopicSuggestion suggestion : suggestions ) {
            if( suggestion.getTopic() != null &&
                suggestion.getTopic().getId() == resolution.topicId() ) {
                return suggestion ;
            }
        }
        return null ;
    }
}
