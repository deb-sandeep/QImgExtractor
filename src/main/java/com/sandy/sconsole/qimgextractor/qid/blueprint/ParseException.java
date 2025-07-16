package com.sandy.sconsole.qimgextractor.qid.blueprint;

public class ParseException extends Exception {

    public ParseException( SegBP blueprint, String message ) {
        super( message + ". Blueprint: " + blueprint.getName() ) ;
    }
    
    public ParseException( SegBP blueprint, String message, Throwable cause ) {
        super( message + ". Blueprint: " + blueprint.getName(), cause ) ;
    }
}
