package com.sandy.sconsole.qimgextractor.ui.project.savedialog;

import com.sandy.sconsole.qimgextractor.qid.AITS_QID;
import com.sandy.sconsole.qimgextractor.qid.QuestionImage;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.awt.event.InputEvent.CTRL_DOWN_MASK;
import static java.awt.event.KeyEvent.VK_0;
import static java.awt.event.KeyEvent.VK_9;

@Slf4j
public class ImgSaveDialog extends JFileChooser {
    
    private final String srcId ;
    private QuestionImage lastSavedImage = null ;
    
    public ImgSaveDialog( File curDir, String srcId ) {
        super() ;
        this.srcId = srcId ;
        
        setCurrentDirectory( curDir ) ;
        bindKeyStrokesForSaveDialog() ;
        setDialogTitle( "Save Image" ) ;
        setApproveButtonText( "Save" ) ;
        setApproveButtonToolTipText( "Save the selected image" ) ;
        setApproveButtonMnemonic( 'S' ) ;
        setDialogType( JFileChooser.SAVE_DIALOG ) ;
        setControlButtonsAreShown( true ) ;
        setPreferredSize( new java.awt.Dimension( 800, 500 ) ) ;
        
        hideOnlyFileFormatSection() ;
        
        if( this.srcId.startsWith( "AITS" ) ) {
            setAccessory( new HelpManual( AITS_QID.SAVE_HELP_CONTENTS ) ) ;
        }
        
        updateLastSavedImage() ;
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
        
        for( int i = 0; i <= 9; i++ ) {
            
            int keyCode = VK_0 + i ;
            String keyHandlerID = getKeyHandlerID( i ) ;
            
            KeyStroke keyStroke = KeyStroke.getKeyStroke( keyCode, CTRL_DOWN_MASK  ) ;
            SaveFnKeyHandler handler = new NoOpFnKeyHandler() ;
            
            inputMap.put( keyStroke, keyHandlerID ) ;
            actionMap.put( keyHandlerID, handler ) ;
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
        if( savedImageFiles != null ) {
            List<QuestionImage> images = new ArrayList<>() ;
            for( File file : savedImageFiles ) {
                images.add( new QuestionImage( file ) ) ;
            }
            Collections.sort( images ) ;
            this.lastSavedImage = images.get( images.size()-1 ) ;
        }
    }
    
    public void registerSaveFnHandler( int vkCode, SaveFnKeyHandler handler ) {
        if( vkCode < VK_0 || vkCode > VK_9 ) {
            throw new IllegalArgumentException( "VK not in set (VK_0 ... VK_9)" ) ;
        }
        
        String keyHandlerID = getKeyHandlerID( vkCode - VK_0 ) ;
        ActionMap actionMap = super.getActionMap() ;
        
        actionMap.put( keyHandlerID, handler ) ;
        
        log.info( "Installed save fn key handler = {}", handler.getName() );
    }
    
    public void updateRecommendedFileName() {
        if( this.lastSavedImage != null ) {
            QuestionImage nextQ = this.lastSavedImage.nextQuestion() ;
            setSelectedFile( new File( getCurrentDirectory(), nextQ.getFileName() ) );
        }
    }
}
