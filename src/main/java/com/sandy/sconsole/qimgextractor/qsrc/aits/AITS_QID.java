package com.sandy.sconsole.qimgextractor.qsrc.aits;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import lombok.EqualsAndHashCode;

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
    
    public static final List<String> SAVE_HELP_CONTENTS = new ArrayList<>() ;
    static {
        SAVE_HELP_CONTENTS.addAll( QID.COMMON_SAVE_HELP_CONTENTS ) ;
        SAVE_HELP_CONTENTS.addAll( Arrays.asList(
            "-------- AITS Specific shortcuts --"
        ) ) ;
    }
    
    AITS_QID( QuestionImage qImg ) {
        super( qImg ) ;
    }
    
    public void parse( Stack<String> parts ) {
        super.parseQTypeAndNumber( parts ) ;
    }
}
