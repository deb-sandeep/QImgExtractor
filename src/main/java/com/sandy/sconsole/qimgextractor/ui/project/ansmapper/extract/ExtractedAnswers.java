package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.extract;

import lombok.Getter;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// The answers returned by the extract-answers skill. The reply carries them
// as JSON inside a single <extracted-answers> block, everything outside the
// block is a human-readable summary. See OUTPUT-FORMAT.md in the skill folder.
public class ExtractedAnswers {

    public record Entry( String section,
                         String subjectCode,
                         int id,
                         String deducedType,
                         String answer,
                         boolean isError ) {}

    private static final Pattern BLOCK_PATTERN = Pattern.compile(
            "<extracted-answers>\\s*(.*?)\\s*</extracted-answers>", Pattern.DOTALL ) ;

    private static final Map<String, String> SUBJECT_CODES = Map.of(
            "Physics",   "P",
            "Chemistry", "C",
            "Maths",     "M" ) ;

    // In the order of the sections on the page and ascending id order within
    // a section.
    @Getter
    private final List<Entry> entries = new ArrayList<>() ;

    @Getter
    private final List<String> sectionNames = new ArrayList<>() ;

    private ExtractedAnswers() {}

    public static ExtractedAnswers parse( String replyText ) {

        Matcher matcher = BLOCK_PATTERN.matcher( replyText ) ;
        if( !matcher.find() ) {
            throw new IllegalArgumentException(
                    "The reply has no <extracted-answers> block. The reply text " +
                    "says why the skill stopped." ) ;
        }

        ExtractedAnswers result = new ExtractedAnswers() ;
        try {
            JSONArray sections = new JSONObject( matcher.group( 1 ) ).getJSONArray( "sections" ) ;
            for( int i = 0; i < sections.length(); i++ ) {
                JSONObject section = sections.getJSONObject( i ) ;
                String name = section.getString( "name" ) ;
                String subjectCode = SUBJECT_CODES.get( name ) ;
                if( subjectCode == null ) {
                    throw new IllegalArgumentException( "Unknown section name '" + name + "'" ) ;
                }
                result.sectionNames.add( name ) ;

                JSONArray answers = section.getJSONArray( "answers" ) ;
                for( int j = 0; j < answers.length(); j++ ) {
                    JSONObject ans = answers.getJSONObject( j ) ;
                    result.entries.add( new Entry(
                            name,
                            subjectCode,
                            ans.getInt( "id" ),
                            ans.isNull( "deducedType" ) ? null : ans.getString( "deducedType" ),
                            ans.isNull( "answer" ) ? null : ans.getString( "answer" ),
                            ans.getBoolean( "isError" ) ) ) ;
                }
            }
        }
        catch( IllegalArgumentException e ) {
            throw e ;
        }
        catch( Exception e ) {
            throw new IllegalArgumentException( "Malformed <extracted-answers> JSON. " + e.getMessage(), e ) ;
        }
        return result ;
    }
}
