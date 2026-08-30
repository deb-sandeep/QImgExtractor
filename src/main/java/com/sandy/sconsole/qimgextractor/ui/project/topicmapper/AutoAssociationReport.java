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
    int failedGating ;

    int notAssociated() {
        return noRecommendation + failedGating ;
    }

    public String toDisplayString() {
        return  "Auto Topic Association complete.\n\n" +
                String.format( "Questions scanned            : %d%n", total ) +
                String.format( "Already classified (skipped) : %d%n", alreadyClassified ) +
                String.format( "Newly associated             : %d%n", mapped ) +
                String.format( "Not associated               : %d%n", notAssociated() ) +
                String.format( "    - had recommendations, failed quality gating : %d%n", failedGating ) +
                String.format( "    - no recommendation available               : %d%n", noRecommendation ) ;
    }
}
