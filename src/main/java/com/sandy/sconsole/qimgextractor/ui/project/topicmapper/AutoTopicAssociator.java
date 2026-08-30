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
 * Quality gate (recommendations are confidence-descending):
 * <ul>
 *   <li>Physics ("P") / Maths ("M") : associate the top topic iff its confidence &gt; 95.</li>
 *   <li>Chemistry ("C")             : associate the top topic iff its confidence &ge; 95
 *       and it leads the second recommendation by more than 5 (a lone recommendation
 *       &ge; 95 passes).</li>
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

        switch( subjectCode ) {
            case "P" :
            case "M" :
                return top.getConfidenceLevel() >= 95 ? top.getTopic() : null ;
            case "C" : {
                int second = sortedSuggestions.size() > 1
                        ? sortedSuggestions.get( 1 ).getConfidenceLevel() : 0 ;
                boolean pass = top.getConfidenceLevel() >= 95
                        && ( top.getConfidenceLevel() - second ) > 5 ;
                return pass ? top.getTopic() : null ;
            }
            default :
                return null ;
        }
    }
}
