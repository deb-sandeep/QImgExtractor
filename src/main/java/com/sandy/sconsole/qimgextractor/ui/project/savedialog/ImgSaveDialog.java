package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import com.sandy.sconsole.qimgextractor.qsrc.QSrcFactory;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectContext;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.Map;

@Slf4j
public class ImgSaveDialog extends JFileChooser {
    
    @Getter
    private final ProjectModel projectModel ;
    
    @Getter
    private final String srcId ;
    
    private final ProjectContext projectContext ;
    
    public ImgSaveDialog( ProjectModel model ) {
        super() ;
        this.projectModel = model ;
        this.projectContext = model.getContext() ;
        this.srcId = model.getProjectName() ;
        
        setCurrentDirectory( model.getExtractedImgDir() ) ;
        setSelectedFile( null ) ;
        setDialogTitle( "Save Image" ) ;
        setApproveButtonText( "Save" ) ;
        setApproveButtonToolTipText( "Save the selected image" ) ;
        setApproveButtonMnemonic( 'S' ) ;
        setDialogType( JFileChooser.SAVE_DIALOG ) ;
        setControlButtonsAreShown( true ) ;
        setPreferredSize( new java.awt.Dimension( 800, 500 ) ) ;
        
        bindKeyStrokesForSaveDialog() ;
        hideOnlyFileFormatSection() ;
        setHelpAccessory() ;
    }
    
    private void setHelpAccessory() {
        List<String> helpContents = QSrcFactory.getQSrcComponentFactory( srcId )
                                               .getSaveHelpContents() ;
        if( helpContents != null && !helpContents.isEmpty() ) {
            setAccessory( new HelpManual( helpContents ) ) ;
        }
    }
    
    private void hideOnlyFileFormatSection() {
        SwingUtilities.invokeLater(() -> {
            removeSpecificComboPanel( this ) ;
            this.revalidate() ;
            this.repaint() ;
        });
    }
    
    private void removeSpecificComboPanel( Container container ) {
        for( Component comp : container.getComponents() ) {
            if( comp instanceof JComboBox ) {
                Container parent = comp.getParent() ;
                if( parent != null && hasFileFormatLabel(parent) ) {
                    Container grandParent = parent.getParent() ;
                    if( grandParent != null ) {
                        grandParent.remove( parent ) ; // remove the "format" subpanel
                        return;
                    }
                }
            }
            else if( comp instanceof Container child ) {
                removeSpecificComboPanel( child ) ; // recursive search
            }
        }
    }
    
    private boolean hasFileFormatLabel( Container parent ) {
        for( Component c : parent.getComponents() ) {
            if (c instanceof JLabel label) {
                String text = label.getText() ;
                if( "File Format:".equals(text) || "Files of Type:".equals( text ) ) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private void bindKeyStrokesForSaveDialog() {
        
        InputMap inputMap = super.getInputMap( JFileChooser.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ) ;
        ActionMap actionMap = super.getActionMap() ;
        Map<KeyStroke, SaveFnKeyHandler> customFnKeyHandlers = QSrcFactory.getQSrcComponentFactory( this.srcId )
                                                                          .getSaveFnKeyHandlers() ;
        
        if( customFnKeyHandlers != null && !customFnKeyHandlers.isEmpty() ) {
            customFnKeyHandlers.forEach( (keyStroke,handler ) -> {
                
                inputMap.put( keyStroke, keyStroke.toString() ) ;
                SaveFnKeyHandlerWrapper wrapper = new SaveFnKeyHandlerWrapper( this, handler ) ;
                
                actionMap.put( keyStroke.toString(), wrapper ) ;
            } ) ;
        }
    }
    
    public void updateRecommendedFileName() {
        log.debug( "  Updating recommended file name in save dialog." ) ;
        QuestionImage lastSavedImage = projectContext.getLastSavedImg() ;
        if( lastSavedImage != null ) {
            QuestionImage nextQ = lastSavedImage.nextQuestion() ;
            setSelectedFile( new File( getCurrentDirectory(), nextQ.getShortFileName() ) ) ;
            log.debug( "    Recommended file name: {}", nextQ.getShortFileName() ) ;
        }
        else {
            log.debug( "    No last saved image found. Recommended file name will be blank." ) ;
        }
    }
    
    public int showSaveDialog( Component parent ) throws HeadlessException {
        return super.showSaveDialog( parent ) ;
    }
}
