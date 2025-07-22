package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.log4j.Logger;

import javax.swing.*;
import java.awt.event.ActionEvent;

@Data
@EqualsAndHashCode( callSuper = false )
@Slf4j
public abstract class SaveFnKeyHandler extends AbstractAction {
    
    protected final String name ;
    
    public SaveFnKeyHandler( String name ) {
        this.name = name ;
    }
    
    public final void actionPerformed( ActionEvent e ) {
        handleEvent() ;
    }
    
    protected abstract void handleEvent() ;
}
