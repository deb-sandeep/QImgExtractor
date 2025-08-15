package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;

public interface SaveFnKeyHandler {
    // Note that the qImg passed to this function is a temprary copy with
    // sub-image info not populated. Hence, any implementors are advised
    // not to rely on the sub-image info. This method should only be used
    // to compute the changes to the file name.
    void mutateQuestionImage( QuestionImage qImg, boolean isShiftPressed ) ;
}
