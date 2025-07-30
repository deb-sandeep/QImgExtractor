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
import static java.awt.event.KeyEvent.*;
import static javax.swing.KeyStroke.getKeyStroke;

public class AITSComponentFactory extends QSrcComponentFactory {
    
    private static final SaveFnKeyHandler SUB_ROLL_KH   = QuestionImage::rollSubjectCode ;
    private static final SaveFnKeyHandler QTYPE_ROLL_KH = ( qImg, isShiftPressed ) ->  qImg.getQId().rollQType( isShiftPressed ) ;
    private static final SaveFnKeyHandler PART_SEQ_KH   = QuestionImage::beginOrEndPartSequence;
    private static final SaveFnKeyHandler QNO_KH        = ( qImg, isShiftPressed ) -> qImg.getQId().rollQuestionNumber( isShiftPressed ) ;
    
    @Override
    public QID getNewQIDInstance( QuestionImage qImg ) {
        return new AITS_QID( qImg  ) ;
    }
    
    @Override
    public List<String> getSaveHelpContents() {
        return List.of(
            "[sub]_[<QType>]_[<lct-#>_]?[QNo][(part)]?",
            "--------- Key strokes --------------",
            "[C]  s : Subject code increment",
            "[CS] s : Subject code decrement",
            "[C]  q : Question type increment",
            "[CS] q : Question type decrement",
            "[C]  p : Part sequence start",
            "[CS] p : Part sequence end",
            "[C]  n : Question number increment",
            "[CS] n : Question number decrement"
        );
    }
    
    @Override
    public Map<KeyStroke, SaveFnKeyHandler> getSaveFnKeyHandlers() {
        return Map.of(
             getKeyStroke( VK_S, CTRL_DOWN_MASK ),                   SUB_ROLL_KH,
             getKeyStroke( VK_S, SHIFT_DOWN_MASK | CTRL_DOWN_MASK ), SUB_ROLL_KH,
             
             getKeyStroke( VK_Q, CTRL_DOWN_MASK ),                   QTYPE_ROLL_KH,
             getKeyStroke( VK_Q, SHIFT_DOWN_MASK | CTRL_DOWN_MASK ), QTYPE_ROLL_KH,
                
             getKeyStroke( VK_P, CTRL_DOWN_MASK ),                   PART_SEQ_KH,
             getKeyStroke( VK_P, SHIFT_DOWN_MASK | CTRL_DOWN_MASK ), PART_SEQ_KH,
             
             getKeyStroke( VK_N, CTRL_DOWN_MASK ),                   QNO_KH,
             getKeyStroke( VK_N, SHIFT_DOWN_MASK | CTRL_DOWN_MASK ), QNO_KH
        ) ;
    }
}
