package com.sandy.sconsole.qimgextractor.qsrc.manual;

import com.sandy.sconsole.qimgextractor.qsrc.GenericComponentFactory;
import com.sandy.sconsole.qimgextractor.qsrc.Generic_QID;
import com.sandy.sconsole.qimgextractor.qsrc.QSrcComponentFactory;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog.SaveFnKeyHandler;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.InputEvent.SHIFT_DOWN_MASK;
import static java.awt.event.KeyEvent.*;
import static javax.swing.KeyStroke.getKeyStroke;

public class MNComponentFactory extends GenericComponentFactory {
    
    @Override
    public QID getNewQIDInstance( QuestionImage qImg ) {
        return new MN_QID( qImg  ) ;
    }
    
    @Override
    public List<String> getSaveHelpContents() {
        ArrayList<String> contents = new ArrayList<>( super.getSaveHelpContents() ) ;
        contents.remove( 0 ) ;
        contents.add( 0, "[sub]_[bk_cd]_[ch_no]_[ex_name]_[<QType>]_[<lct-#>_]?[QNo][(part)]?" );
        return contents ;
    }
}
