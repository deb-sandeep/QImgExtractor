package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.extract;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

// Works out how the extracted answers apply to the project's questions,
// without changing anything, so that the changes can be previewed before
// they are applied.
//
// A question is identified by its subject and printed question number. Some
// papers restart the numbering for the numeric questions (P_SCA_1..20 then
// P_IVT_1..10), so a number can match one question of each kind. Only then
// is the deduced type of the answer used, to pick the numeric (IVT/NVT) or
// the non-numeric question. A question's type is
// changed only within the numeric types (an integer answer means IVT, a
// decimal means NVT) and from SCA to MCA when the answer has several letters.
// A decimal with a zero fractional part (158.00) counts as an integer and is
// stored without the fraction.
@Slf4j
public class AnswerImportPlan {

    public record Item( String subjectCode, int number,
                        String qId,
                        String oldType, String newType,
                        String oldAnswer, String newAnswer ) {

        boolean isTypeChange() {
            return !oldType.equals( newType ) ;
        }

        boolean isOverwrite() {
            return oldAnswer != null && !oldAnswer.equals( newAnswer ) ;
        }

        boolean isUnchanged() {
            return !isTypeChange() && newAnswer.equals( oldAnswer ) ;
        }
    }

    public record Skipped( String ref, String answer, String reason ) {}

    private static final String INTEGER_PATTERN = "^-?\\d+$" ;
    private static final String DECIMAL_PATTERN = "^-?(\\d+\\.\\d*|\\.\\d+)$" ;
    private static final String WHOLE_DECIMAL_PATTERN = "^-?\\d+\\.0*$" ;
    private static final String RANGE_PATTERN   = "^-?[\\d.]+\\s*-\\s*-?[\\d.]+$" ;
    private static final String MULTI_LETTER_PATTERN = "^[A-Da-d]{2,}$" ;

    @Getter private final List<Item> items = new ArrayList<>() ;
    @Getter private final List<Skipped> skipped = new ArrayList<>() ;
    @Getter private final List<String> warnings = new ArrayList<>() ;

    private AnswerImportPlan() {}

    public static AnswerImportPlan build( ExtractedAnswers extracted, List<Question> questions ) {

        AnswerImportPlan plan = new AnswerImportPlan() ;
        Map<String, List<Question>> questionMap = mapBySubjectAndNumber( questions ) ;

        for( ExtractedAnswers.Entry entry : extracted.getEntries() ) {
            String ref = entry.subjectCode() + "/" + entry.id() ;

            if( entry.isError() || entry.answer() == null ) {
                plan.skipped.add( new Skipped( ref, entry.answer(),
                        entry.answer() == null ? "unreadable in the answer key" : "not an answer" ) ) ;
                continue ;
            }

            List<Question> matches = questionMap.getOrDefault( ref, List.of() ) ;
            if( matches.size() > 1 && entry.deducedType() != null ) {
                boolean numeric = isNumericType( entry.deducedType() ) ;
                matches = matches.stream()
                        .filter( q -> isNumericType( q.getQID().getQuestionType() ) == numeric )
                        .toList() ;
            }
            if( matches.isEmpty() ) {
                plan.skipped.add( new Skipped( ref, entry.answer(), "no such question in the project" ) ) ;
                continue ;
            }
            if( matches.size() > 1 ) {
                plan.skipped.add( new Skipped( ref, entry.answer(),
                        matches.size() + " questions of this kind have this number" ) ) ;
                continue ;
            }

            Question q = matches.get( 0 ) ;
            plan.planEntry( q, entry ) ;
        }

        plan.checkIdSequence( extracted ) ;
        return plan ;
    }

    private static Map<String, List<Question>> mapBySubjectAndNumber( List<Question> questions ) {
        Map<String, List<Question>> map = new HashMap<>() ;
        for( Question q : questions ) {
            String key = q.getQID().getSubjectCode() + "/" + q.getQID().getQuestionNumber() ;
            map.computeIfAbsent( key, k -> new ArrayList<>() ).add( q ) ;
        }
        return map ;
    }

    private static boolean isNumericType( String type ) {
        return QID.IVT.equals( type ) || QID.NVT.equals( type ) ;
    }

    private void planEntry( Question q, ExtractedAnswers.Entry entry ) {

        String qId = q.getQID().toString() ;
        String oldType = q.getQID().getQuestionType() ;
        String answer = entry.answer().trim() ;

        if( oldType.equals( QID.MMT ) ) {
            skipped.add( new Skipped( qId, answer, "MMT answers can't be imported" ) ) ;
            return ;
        }
        if( answer.matches( RANGE_PATTERN ) ) {
            skipped.add( new Skipped( qId, answer, "range answers can't be stored" ) ) ;
            return ;
        }

        String newType = getTargetType( oldType, answer ) ;
        if( newType.equals( QID.IVT ) && answer.matches( WHOLE_DECIMAL_PATTERN ) ) {
            answer = answer.substring( 0, answer.indexOf( '.' ) ) ;
        }
        if( !newType.equals( oldType ) && q.isSyncedToAnyServer() ) {
            skipped.add( new Skipped( qId, answer,
                    "needs type " + newType + " but the question is synced to a server" ) ) ;
            return ;
        }

        String newAnswer ;
        try {
            newAnswer = Question.formatRawAnswer( newType, answer ) ;
        }
        catch( Question.InvalidAnswerException e ) {
            skipped.add( new Skipped( qId, answer, "invalid for " + newType + ". " + e.getMessage() ) ) ;
            return ;
        }

        items.add( new Item( q.getQID().getSubjectCode(), q.getQID().getQuestionNumber(),
                             qId, oldType, newType, q.getAnswer(), newAnswer ) ) ;
    }

    private static String getTargetType( String currentType, String answer ) {
        return switch( currentType ) {
            case QID.IVT, QID.NVT -> {
                if( answer.matches( INTEGER_PATTERN ) ||
                    answer.matches( WHOLE_DECIMAL_PATTERN ) ) yield QID.IVT ;
                if( answer.matches( DECIMAL_PATTERN ) ) yield QID.NVT ;
                yield currentType ;
            }
            case QID.SCA -> answer.matches( MULTI_LETTER_PATTERN ) ? QID.MCA : QID.SCA ;
            default -> currentType ;
        } ;
    }

    // The ids should be contiguous and no id should repeat across the paper.
    // Numeric and non-numeric answers are checked separately as their
    // numbering may restart from 1.
    private void checkIdSequence( ExtractedAnswers extracted ) {

        Map<String, List<Integer>> idsBySection = new LinkedHashMap<>() ;
        Set<String> seen = new HashSet<>() ;
        Set<String> duplicates = new TreeSet<>() ;
        for( ExtractedAnswers.Entry entry : extracted.getEntries() ) {
            String kind = isNumericType( entry.deducedType() ) ? "numeric" : "non-numeric" ;
            idsBySection.computeIfAbsent( entry.section() + " (" + kind + ")",
                                          s -> new ArrayList<>() ).add( entry.id() ) ;
            if( !seen.add( kind + " " + entry.id() ) ) {
                duplicates.add( kind + " " + entry.id() ) ;
            }
        }

        idsBySection.forEach( ( section, ids ) -> {
            List<Integer> missing = new ArrayList<>() ;
            for( int i = 1; i < ids.size(); i++ ) {
                for( int id = ids.get( i-1 ) + 1; id < ids.get( i ); id++ ) {
                    missing.add( id ) ;
                }
            }
            if( !missing.isEmpty() ) {
                warnings.add( section + " has gaps in question numbers: " + missing ) ;
            }
        } ) ;
        if( !duplicates.isEmpty() ) {
            warnings.add( "Question numbers repeated across sections: " + duplicates ) ;
        }
    }

    public boolean hasChanges() {
        return items.stream().anyMatch( i -> !i.isUnchanged() ) ;
    }

    public String toSummary() {

        List<Item> typeChanges = items.stream().filter( Item::isTypeChange ).toList() ;
        List<Item> overwrites  = items.stream().filter( Item::isOverwrite ).toList() ;
        List<Item> newAnswers  = items.stream().filter( i -> i.oldAnswer() == null ).toList() ;
        long unchanged = items.stream().filter( Item::isUnchanged ).count() ;

        StringBuilder sb = new StringBuilder() ;
        sb.append( "New answers       : " ).append( newAnswers.size() ).append( '\n' )
          .append( "Changed answers   : " ).append( overwrites.size() ).append( '\n' )
          .append( "Unchanged answers : " ).append( unchanged ).append( '\n' )
          .append( "Type changes      : " ).append( typeChanges.size() ).append( '\n' )
          .append( "Skipped           : " ).append( skipped.size() ).append( '\n' ) ;

        if( !warnings.isEmpty() ) {
            sb.append( "\nWARNINGS\n" ) ;
            warnings.forEach( w -> sb.append( "  " ).append( w ).append( '\n' ) ) ;
        }
        if( !typeChanges.isEmpty() ) {
            sb.append( "\nTYPE CHANGES (image files will be renamed)\n" ) ;
            typeChanges.forEach( i -> sb.append( String.format( "  %-12s %s -> %s  (answer %s)%n",
                    i.qId(), i.oldType(), i.newType(), i.newAnswer() ) ) ) ;
        }
        if( !overwrites.isEmpty() ) {
            sb.append( "\nCHANGED ANSWERS\n" ) ;
            overwrites.forEach( i -> sb.append( String.format( "  %-12s %s -> %s%n",
                    i.qId(), i.oldAnswer(), i.newAnswer() ) ) ) ;
        }
        if( !skipped.isEmpty() ) {
            sb.append( "\nSKIPPED\n" ) ;
            skipped.forEach( s -> sb.append( String.format( "  %-12s %-10s %s%n",
                    s.ref(), s.answer() == null ? "-" : s.answer(), s.reason() ) ) ) ;
        }
        if( !newAnswers.isEmpty() ) {
            sb.append( "\nNEW ANSWERS\n" ) ;
            newAnswers.forEach( i -> sb.append( String.format( "  %-12s %s%n",
                    i.qId(), i.newAnswer() ) ) ) ;
        }
        return sb.toString() ;
    }

    // Applies the type changes first and then the answers. Type changes
    // rename question images, which rebuilds the question list, so questions
    // are looked up afresh before their answers are set. Returns descriptions
    // of the changes that could not be applied.
    public List<String> apply( ProjectModel projectModel ) {

        List<String> failures = new ArrayList<>() ;

        for( Item item : items ) {
            if( !item.isTypeChange() ) continue ;
            Question q = findQuestion( projectModel, item ) ;
            if( q == null ) {
                failures.add( item.qId() + " : question no longer exists" ) ;
                continue ;
            }
            try {
                projectModel.changeQuestionType( q.getOwnQImgList(), item.newType() ) ;
            }
            catch( RuntimeException e ) {
                log.error( "Could not change type of {}", item.qId(), e ) ;
                failures.add( item.qId() + " : type not changed to " + item.newType() + ". " + e.getMessage() ) ;
            }
        }

        for( Item item : items ) {
            if( item.isUnchanged() ) continue ;
            Question q = findQuestion( projectModel, item ) ;
            if( q == null ) {
                failures.add( item.qId() + " : question no longer exists" ) ;
                continue ;
            }
            try {
                q.setRawAnswer( item.newAnswer() ) ;
            }
            catch( Question.InvalidAnswerException e ) {
                failures.add( q.getQID() + " : answer " + item.newAnswer() + " not set. " + e.getMessage() ) ;
            }
        }

        projectModel.answersChanged() ;
        projectModel.getQuestionRepo().save() ;
        return failures ;
    }

    private static Question findQuestion( ProjectModel projectModel, Item item ) {
        List<Question> matches = projectModel.getQuestionRepo().getQuestionList().stream()
                .filter( q -> q.getQID().getSubjectCode().equals( item.subjectCode() ) &&
                              q.getQID().getQuestionNumber() == item.number() &&
                              isNumericType( q.getQID().getQuestionType() ) == isNumericType( item.oldType() ) )
                .toList() ;
        return matches.size() == 1 ? matches.get( 0 ) : null ;
    }
}
