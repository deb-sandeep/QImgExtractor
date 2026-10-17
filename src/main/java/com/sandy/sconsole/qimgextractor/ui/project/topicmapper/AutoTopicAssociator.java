package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.TopicAssociator;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Walks every unclassified question in the project and assigns the top AI
 * recommended topic, irrespective of its confidence level. There is deliberately
 * no quality gating here - the associations are reviewed later by a stronger model.
 *
 * Recommendations are confidence-descending; the first one whose topic id resolves
 * against the topic master is chosen. Questions with no recommendation are left
 * untouched.
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

        suggestionRepo.reload() ;

        AutoAssociationReport report = new AutoAssociationReport() ;

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

            AITopicSuggestion chosen = topSuggestionWithTopic( suggestions ) ;
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
     * @param sortedSuggestions recommendations for the question, confidence-descending.
     * @return the highest-confidence recommendation whose topic resolved, or
     *         {@code null} if none of the recommended topic ids are known.
     */
    private AITopicSuggestion topSuggestionWithTopic( List<AITopicSuggestion> sortedSuggestions ) {
        for( AITopicSuggestion suggestion : sortedSuggestions ) {
            if( suggestion.getTopic() != null ) {
                return suggestion ;
            }
        }
        return null ;
    }
}
