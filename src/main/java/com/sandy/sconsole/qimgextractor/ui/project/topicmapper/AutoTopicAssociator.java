package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.Topic;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Walks every unclassified question in the project and, where an AI recommendation
 * clears the per-subject confidence quality gate, assigns the top recommended topic.
 *
 * Quality gate (recommendations are confidence-descending; the second recommendation's
 * confidence is treated as 0 when there is only one):
 * <ul>
 *   <li>Any subject : associate the top topic if its confidence &ge; 90 and it leads
 *       the second recommendation by 10 or more.</li>
 *   <li>Physics ("P") / Maths ("M") : otherwise associate the top topic iff its
 *       confidence &ge; 95.</li>
 *   <li>Chemistry ("C") : otherwise associate the top topic iff its confidence &ge; 95
 *       and it leads the second recommendation by more than 5.</li>
 *   <li>No recommendation for the question : left untouched.</li>
 * </ul>
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

            Topic chosen = evaluate( question.getQID().getSubjectCode(), suggestions ) ;
            if( chosen != null ) {
                question.setTopic( chosen ) ;
                report.mapped++ ;
            }
            else {
                report.failedGating++ ;
            }
        }

        return report ;
    }

    /**
     * @param sortedSuggestions recommendations for the question, confidence-descending.
     * @return the topic to assign, or {@code null} if the quality gate is not cleared.
     */
    private Topic evaluate( String subjectCode, List<AITopicSuggestion> sortedSuggestions ) {

        AITopicSuggestion top = sortedSuggestions.get( 0 ) ;
        if( top.getTopic() == null ) {
            return null ;
        }

        int topConfidence = top.getConfidenceLevel() ;
        int secondConfidence = sortedSuggestions.size() > 1
                ? sortedSuggestions.get( 1 ).getConfidenceLevel() : 0 ;

        // Subject-agnostic rule: a high-confidence top match that clearly leads the field.
        if( topConfidence >= 90 && ( topConfidence - secondConfidence ) >= 10 ) {
            return top.getTopic() ;
        }
        
        return switch( subjectCode ) {
            case "P", "M" ->
                    topConfidence >= 95 ? top.getTopic() : null;
            case "C" ->
                    topConfidence >= 95 && ( topConfidence - secondConfidence ) > 5 ?
                            top.getTopic() : null;
            default -> null;
        };
    }
}
