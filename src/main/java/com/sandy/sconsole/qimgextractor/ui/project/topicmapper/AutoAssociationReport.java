package com.sandy.sconsole.qimgextractor.ui.project.topicmapper;

/**
 * Running tallies for one {@link AutoTopicAssociator} pass, and the text shown to
 * the user once it completes.
 */
public class AutoAssociationReport {

    int total ;
    int alreadyClassified ;
    int mapped ;
    int noRecommendation ;
    int unresolvedTopic ;

    int notAssociated() {
        return noRecommendation + unresolvedTopic ;
    }

    public String toDisplayString() {
        return  "Auto Topic Association complete.\n\n" +
                String.format( "Questions scanned            : %d%n", total ) +
                String.format( "Already classified (skipped) : %d%n", alreadyClassified ) +
                String.format( "Newly associated             : %d%n", mapped ) +
                String.format( "Not associated               : %d%n", notAssociated() ) +
                String.format( "    - recommended topic ids not in topic master  : %d%n", unresolvedTopic ) +
                String.format( "    - no recommendation available               : %d%n", noRecommendation ) ;
    }
}
