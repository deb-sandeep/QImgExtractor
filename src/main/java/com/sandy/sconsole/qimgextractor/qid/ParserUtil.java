package com.sandy.sconsole.qimgextractor.qid;

import java.io.File;
import java.util.Stack;

public class ParserUtil {

    public static void checkNullFile( File file ) {
        assertCondition( file == null, 
                         "File specified is null." ) ;
    }

    public static void validateSubjectCode( String subjectCode ) {
        assertCondition( subjectCode == null ||
                         subjectCode.trim().isEmpty() ||
                         !QuestionImage.SUB_SEQ.contains( subjectCode ),
                         "Invalid subject code - " + subjectCode + "." ) ;
    }

    public static void validateQuestionType( String questionType ) {
        assertCondition( questionType == null ||
                         questionType.trim().isEmpty() ||
                         !QID.Q_TYPE_SEQ.contains( questionType ),
                         "Invalid question type - " + questionType + "." ) ;
    }
    
    private static void assertCondition( boolean flag, String message ) {
        if( flag ) {
            throw new IllegalArgumentException( message ) ;
        }
    }
    
    public static int getInt( String field, String intStr ) {
        intStr = intStr.trim() ;
        int val = 0 ;
        try {
            val = Integer.parseInt( intStr ) ;
        }
        catch( Exception e ) {
            throw new IllegalArgumentException( field + " expects an int value and " +
                    intStr + " is not an int value." ) ;
        }
        return val ;
    }
    
    public static Stack<String> parseToStack( String input ) {
        String[] parts = input.split("_");
        Stack<String> stack = new Stack<>();
        
        // Push elements in reverse order so the first part is on top
        for( int i = parts.length - 1; i >= 0; i-- ) {
            stack.push( parts[i].trim() );
        }
        
        return stack;
    }
}
