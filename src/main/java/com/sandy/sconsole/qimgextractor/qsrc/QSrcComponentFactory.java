package com.sandy.sconsole.qimgextractor.qsrc;

import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.SaveFnKeyHandler;

import javax.swing.*;
import java.util.List;
import java.util.Map;

public abstract class QSrcComponentFactory {

    public abstract QID getNewQIDInstance( QuestionImage qImg ) ;
    
    public abstract List<String> getSaveHelpContents() ;
    
    public abstract Map<KeyStroke, SaveFnKeyHandler> getSaveFnKeyHandlers() ;
}
