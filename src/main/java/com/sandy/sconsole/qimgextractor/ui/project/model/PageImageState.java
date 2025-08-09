package com.sandy.sconsole.qimgextractor.ui.project.model;

import lombok.Data;

@Data
public class PageImageState {

    private String fileName ;
    private boolean visible ;
    private boolean selected ;
    
    public PageImageState() {
        this.visible = true ;
        this.selected = false ;
    }
    
    public PageImageState( String fileName ) {
        this() ;
        this.fileName = fileName ;
    }
}
