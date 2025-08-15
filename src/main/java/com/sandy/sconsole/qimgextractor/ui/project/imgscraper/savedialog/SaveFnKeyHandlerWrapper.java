package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.savedialog;

import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;

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
            fqImgFileName = saveDialog.getSrcId() + "." +
                            String.format( "%03d", saveDialog.getProjectModel().getContext().getSelectedPageNumber() ) + "." +
                            fileName ;
        }
        File fqImgFile = new File( selFile.getParentFile(), fqImgFileName ) ;
        
        // 3. Parse the file to see if it meets the file name criteria.
        // If not, then an exception will be thrown.
        PageImage selPageImg = saveDialog.getProjectModel().getContext().getSelectedPageImg() ;
        QuestionImage mutableImgFile = new QuestionImage( selPageImg, fqImgFile, null ) ;
        fnKeyHandler.mutateQuestionImage( mutableImgFile, isShiftPressed( e ) ) ;
        
        File newFile = new File( selFile.getParentFile(), mutableImgFile.getShortFileName() ) ;
        saveDialog.setSelectedFile( newFile );
    }
    
    private boolean isShiftPressed( ActionEvent e ) {
        return ( e.getModifiers() & ActionEvent.SHIFT_MASK ) == ActionEvent.SHIFT_MASK ;
    }
}
