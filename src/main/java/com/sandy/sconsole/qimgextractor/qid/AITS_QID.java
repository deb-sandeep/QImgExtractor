package com.sandy.sconsole.qimgextractor.qid;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;

/**
 * For AITS papers (Main and Advanced), each paper has questions for physics,
 * chemistry and maths (in the sequence). Each part has questions numbered as:
 * <p>
 * 2. Q No - Natural number
 * </p>
 *
 * Hence the question ID will have two parts
 * <question number>
 */
@EqualsAndHashCode(callSuper = false)
public class AITS_QID extends QID {
    
    @Getter private int questionNumber = -1 ;
    
    public static final List<String> SAVE_HELP_CONTENTS = new ArrayList<>() ;
    static {
        SAVE_HELP_CONTENTS.addAll( QID.COMMON_SAVE_HELP_CONTENTS ) ;
        SAVE_HELP_CONTENTS.addAll( Arrays.asList(
            "-------- AITS Specific shortcuts --"
        ) ) ;
    }
    
    AITS_QID( QuestionImage qImg, Stack<String> parts ) {
        super( qImg ) ;
        parse( parts ) ;
    }
    
    protected void parse( Stack<String> parts ) {
        
        if( parts.size() != 1 ) {
            throw new IllegalArgumentException( 
                    "Invalid number of AITS QID segments. " +
                    "Should be <questionNumber>" ) ;
        }
        extractQuestionNumber( parts.pop() ) ;
    }
    
    private void extractQuestionNumber( String part ) {
        try {
            questionNumber = Integer.parseInt( part ) ;
        }
        catch( NumberFormatException e ) {
            throw new IllegalArgumentException(
                    "AITS question numner " + part + " is not a number" ) ;
        }
    }
    
    @Override
    public void incrementQuestionNumber() {
        this.questionNumber += 1 ;
    }

    @Override
    public String getFilePartName() {
        return String.valueOf( questionNumber ) ;
    }
}
