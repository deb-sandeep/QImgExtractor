package com.sandy.sconsole.qimgextractor.qsrc.aits;

import com.sandy.sconsole.qimgextractor.qid.QID;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.qsrc.QSrcComponentFactory;
import com.sandy.sconsole.qimgextractor.ui.project.savedialog.SaveFnKeyHandler;

import javax.swing.*;
import java.util.List;
import java.util.Map;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_Q;
import static java.awt.event.KeyEvent.VK_S;
import static javax.swing.KeyStroke.getKeyStroke;

public class AITSComponentFactory extends QSrcComponentFactory {
    
    private static final SaveFnKeyHandler SUB_ROLL_FN_KEY_HANDLER = QuestionImage::rollSubjectCode ;
    private static final SaveFnKeyHandler QTYPE_ROLL_FN_KEY_HANDLER = (qImg, isShiftPressed ) ->  qImg.getQId().rollQType( isShiftPressed ) ;
    
    @Override
    public QID getNewQIDInstance( QuestionImage qImg ) {
        return new AITS_QID( qImg  ) ;
    }
    
    @Override
    public List<String> getSaveHelpContents() {
        return List.of(
                "[C]  s : Subject code increment",
                "[CS] s : Subject code decrement",
                "[C]  q : Question type increment",
                "[CS] q : Question type decrement"
        );
    }
    
    @Override
    public Map<KeyStroke, SaveFnKeyHandler> getSaveFnKeyHandlers() {
        return Map.of(
             getKeyStroke( VK_S, CTRL_DOWN_MASK ),                   SUB_ROLL_FN_KEY_HANDLER,
             getKeyStroke( VK_S, SHIFT_DOWN_MASK | CTRL_DOWN_MASK ), SUB_ROLL_FN_KEY_HANDLER,
             
             getKeyStroke( VK_Q, CTRL_DOWN_MASK ),                    QTYPE_ROLL_FN_KEY_HANDLER,
             getKeyStroke( VK_Q, SHIFT_DOWN_MASK | CTRL_DOWN_MASK ), QTYPE_ROLL_FN_KEY_HANDLER
        ) ;
    }
}
