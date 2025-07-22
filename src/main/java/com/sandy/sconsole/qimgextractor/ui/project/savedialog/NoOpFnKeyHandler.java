package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import lombok.extern.slf4j.Slf4j;

@Slf4j
class NoOpFnKeyHandler extends SaveFnKeyHandler {

    public NoOpFnKeyHandler() {
        super( "NoOp" ) ;
    }
    
    @Override
    public void handleEvent() {
        log.debug( "No action performed. This is the base function key handler." ) ;
    }
}
