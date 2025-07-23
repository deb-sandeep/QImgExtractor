package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import com.sandy.sconsole.qimgextractor.qsrc.QSrcFactory;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.ProjectContext;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_9;

@Slf4j
public class ImgSaveDialog extends JFileChooser {
    
    private final ProjectContext projectContext ;
    
    @Getter
    private final String srcId ;
    
    @Getter
    private QuestionImage lastSavedImage = null ;
    
    public ImgSaveDialog( File curDir, ProjectContext projectContext ) {
        super() ;
        this.srcId = projectContext.getProjectName() ;
        this.projectContext = projectContext ;
        
        setCurrentDirectory( curDir ) ;
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
        updateLastSavedImage() ;
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
                        grandParent.remove( parent ) ; // just remove the "format" subpanel
                        return;
                    }
                }
            }
            else if( comp instanceof Container child ) {
                removeSpecificComboPanel( child ) ; // recursive search
            }
        }
    }
    
    private boolean hasFileFormatLabel(Container parent) {
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
    
    // There are ten keystroke bound for the input map of the save dialog.
    // Each are bound by Ctrl+[0,1,2,3...9]. By default they do nothing.
    // Each can be overridden by the subclass by attaching a new handler
    private void bindKeyStrokesForSaveDialog() {
        
        InputMap inputMap = super.getInputMap( JFileChooser.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT ) ;
        ActionMap actionMap = super.getActionMap() ;
        Map<Integer, SaveFnKeyHandler> cusomFnKeyHandlers = QSrcFactory.getQSrcComponentFactory( this.srcId )
                                                                       .getSaveFnKeyHandlers() ;
        
        for( int i = 0; i <= 9; i++ ) {
            int keyCode = VK_0 + i ;
            String keyHandlerID = getKeyHandlerID( i ) ;
            
            KeyStroke keyStroke = KeyStroke.getKeyStroke( keyCode, CTRL_DOWN_MASK  ) ;
            
            inputMap.put( keyStroke, keyHandlerID ) ;
            if( cusomFnKeyHandlers != null && cusomFnKeyHandlers.containsKey( keyCode ) ) {
                SaveFnKeyHandler handler = cusomFnKeyHandlers.get( keyCode ) ;
                SaveFnKeyHandlerWrapper wrapper = new SaveFnKeyHandlerWrapper( this, handler ) ;
                
                actionMap.put( keyHandlerID, wrapper ) ;
                log.info( "Installed save fn key handler = {}", handler.getName() );
            }
            else {
                actionMap.put( keyHandlerID, new SaveFnKeyHandlerWrapper( this, new NoOpFnKeyHandler() ) ) ;
            }
        }
    }
    
    private String getKeyHandlerID( int keyIndex ) {
        if( keyIndex < 0 || keyIndex > 9 ) {
            throw new IllegalArgumentException( "VK not in set (VK_0 ... VK_9)" ) ;
        }
        return "SDHandler[Ctrl + VK_" + keyIndex + "]" ;
    }
    
    private void updateLastSavedImage() {
        File[] savedImageFiles = getCurrentDirectory().listFiles( ( dir, name ) -> name.endsWith( ".png" ) ) ;
        if( savedImageFiles != null && savedImageFiles.length > 0 ) {
            List<QuestionImage> images = new ArrayList<>() ;
            for( File file : savedImageFiles ) {
                QuestionImage qImg = new QuestionImage( file ) ;
                images.add( qImg ) ;
                projectContext.setLastSavedImage( qImg ) ;
            }
            Collections.sort( images ) ;
            this.lastSavedImage = images.get( images.size()-1 ) ;
        }
    }
    
    public void updateRecommendedFileName() {
        if( this.lastSavedImage != null ) {
            QuestionImage nextQ = this.lastSavedImage.nextQuestion() ;
            setSelectedFile( new File( getCurrentDirectory(), nextQ.getShortFileName() ) );
        }
    }
    
    public void updateLastSavedImage( QuestionImage img ) {
        this.lastSavedImage = img ;
        projectContext.setLastSavedImage( img ) ;
    }
}
