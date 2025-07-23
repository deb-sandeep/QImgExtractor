package com.sandy.sconsole.qimgextractor.qsrc.aits;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.qsrc.QSrcComponentFactory;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.SaveFnKeyHandler;

import java.awt.event.KeyEvent;
import java.util.List;
import java.util.Map;

public class AITSComponentFactory extends QSrcComponentFactory {
    
    private static final SaveFnKeyHandler SUB_ROLL_FN_KEY_HANDLER = new SaveFnKeyHandler( "rollSubject" ) {
        protected QuestionImage mutateQuestionImage( QuestionImage qImg ) {
            qImg.rollForwardSubjectCode() ;
            return qImg ;
        }
    } ;
    
    private static final SaveFnKeyHandler QTYPE_ROLL_FN_KEY_HANDLER = new SaveFnKeyHandler( "rollQType" ) {
        protected QuestionImage mutateQuestionImage( QuestionImage qImg ) {
            qImg.getQId().rollForwardQType() ;
            return qImg ;
        }
    } ;
    
    public AITSComponentFactory() {
        super() ;
    }
    
    @Override
    public QID getNewQIDInstance( QuestionImage qImg ) {
        return new AITS_QID( qImg  ) ;
    }
    
    @Override
    public List<String> getSaveHelpContents() {
        return List.of();
    }
    
    @Override
    public Map<Integer, SaveFnKeyHandler> getSaveFnKeyHandlers() {
        return Map.of(
             KeyEvent.VK_1, SUB_ROLL_FN_KEY_HANDLER,
             KeyEvent.VK_2, QTYPE_ROLL_FN_KEY_HANDLER
        ) ;
    }
}
