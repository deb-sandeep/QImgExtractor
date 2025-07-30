package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import com.sandy.sconsole.qimgextractor.qid.QuestionImage;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;

class SaveFnKeyHandlerWrapper extends AbstractAction {

    private final ImgSaveDialog saveDialog ;
    private final SaveFnKeyHandler fnKeyHandler ;
    
    public SaveFnKeyHandlerWrapper( ImgSaveDialog saveDialog,
                                    SaveFnKeyHandler fnKeyHandler ) {
        this.saveDialog = saveDialog ;
        this.fnKeyHandler = fnKeyHandler ;
    }
    
    @Override
    public void actionPerformed( ActionEvent e ) {
        
        File selFile = saveDialog.getSelectedFile() ;
        String fileName = selFile.getName() ;
        
        // 2. Prepend the source id to make the file name fully qualified
        // before creating a temporary question image for mutation
        String fqImgFileName = fileName ;
        if( !fileName.startsWith( saveDialog.getSrcId() ) ) {
            fqImgFileName = saveDialog.getSrcId() + "." + fileName ;
            // TODO: How to get the file name. Also, FQN of a file should be made into a utility function
        }
        File fqImgFile = new File( selFile.getParentFile(), fqImgFileName ) ;
        
        // 3. Parse the file to see if it meets the file name criteria.
        // If not, then an exception will be thrown.
        QuestionImage mutableImgFile = new QuestionImage( fqImgFile ) ;
        fnKeyHandler.mutateQuestionImage( mutableImgFile, ( e.getModifiers() & ActionEvent.SHIFT_MASK ) == ActionEvent.SHIFT_MASK ) ;
        
        File newFile = new File( selFile.getParentFile(), mutableImgFile.getShortFileName() ) ;
        saveDialog.setSelectedFile( newFile );
    }
}
