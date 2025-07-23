package com.sandy.sconsole.qimgextractor.qsrc;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.SaveFnKeyHandler;

import java.util.List;
import java.util.Map;

public abstract class QSrcComponentFactory {

    public abstract QID getNewQIDInstance( QuestionImage qImg ) ;
    
    public abstract List<String> getSaveHelpContents() ;
    
    public abstract Map<Integer, SaveFnKeyHandler> getSaveFnKeyHandlers() ;
}
