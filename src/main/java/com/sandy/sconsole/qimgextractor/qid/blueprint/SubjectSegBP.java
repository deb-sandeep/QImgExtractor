package com.sandy.sconsole.qimgextractor.qid.blueprint;

public class SubjectSegBP extends StringSegBP {
    
    public static final SubjectSegBP INSTANCE = new SubjectSegBP() ;
    
    private SubjectSegBP() {
        super( "Subject Name", "P", "C", "M" ) ;
    }
}
