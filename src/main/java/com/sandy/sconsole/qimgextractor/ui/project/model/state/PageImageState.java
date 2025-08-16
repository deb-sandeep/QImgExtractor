package com.sandy.sconsole.qimgextractor.ui.project.model.state;

import lombok.Data;

@Data
public class PageImageState {

    private String fileName ;
    private boolean visible ;
    private boolean selected ;
    private boolean hasAnswerKeys ;
    
    public PageImageState() {
        this.visible = true ;
        this.selected = false ;
        this.hasAnswerKeys = false ;
    }
    
    public PageImageState( String fileName ) {
        this() ;
        this.fileName = fileName ;
    }
}
