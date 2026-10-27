package com.sandy.sconsole.qimgextractor.ui;

import javax.swing.*;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;

// Toolbar mirroring the Project, View and Sync menu items. The buttons share
// the menu's Actions, so their enabled state follows the menu items.
//
// Each button has a display flag below. Set a flag to false to keep that
// button out of the toolbar (the menu item is not affected).
public class ToolBar extends JToolBar {

    // Project menu -----------------------------------------------------------
    private static final boolean SHOW_OPEN_PROJECT           = true ;
    private static final boolean SHOW_CLOSE_PROJECT          = true ;

    private static final boolean SHOW_RELOAD_AI_SUGGESTIONS  = true ;
    private static final boolean SHOW_AUTO_ASSOC_TOPICS      = true ;
    private static final boolean SHOW_TOGGLE_ANS_MARKER      = true ;
    private static final boolean SHOW_EXTRACT_ANSWERS        = true ;
    private static final boolean SHOW_CLOSE_TAB              = true ;

    private static final boolean SHOW_OPEN_QUESTION_SCRAPER  = true ;
    private static final boolean SHOW_OPEN_ANSWER_MAPPER     = true ;
    private static final boolean SHOW_OPEN_TOPIC_MAPPER      = true ;
    private static final boolean SHOW_OPEN_QUESTION_SYNC     = true ;

    private static final boolean SHOW_SET_IMG_CUTTING_COMPLETE = true ;
    private static final boolean SHOW_SET_ANSWERS_MAPPED       = true ;
    private static final boolean SHOW_SET_TOPICS_MAPPED        = true ;
    private static final boolean SHOW_SET_SERVER_SYNCED        = true ;

    // View menu --------------------------------------------------------------
    private static final boolean SHOW_ZOOM_FIT_PAGE          = true ;
    private static final boolean SHOW_ZOOM_WIDTH             = true ;

    // Sync menu --------------------------------------------------------------
    private static final boolean SHOW_EXPAND_ALL             = true ;
    private static final boolean SHOW_COLLAPSE_ALL           = true ;
    private static final boolean SHOW_EXPAND_SYLLABUS        = true ;
    private static final boolean SHOW_ONLY_UNSYNCED          = true ;
    private static final boolean SHOW_SYNC_ALL_PENDING       = true ;

    // Set when a button is added after the last separator, so that separators
    // are only placed between non-empty button groups.
    private boolean groupHasButtons = false ;

    ToolBar( MenuBar menuBar ) {
        setOrientation( JToolBar.VERTICAL ) ;
        setFloatable( false ) ;
        setRollover( true ) ;

        addButton( SHOW_OPEN_PROJECT,  menuBar.getOpenProjectAction() ) ;
        addButton( SHOW_CLOSE_PROJECT, menuBar.getCloseProjectAction() ) ;
        addGroupSeparator() ;

        addButton( SHOW_RELOAD_AI_SUGGESTIONS, menuBar.getReloadAISuggestionsAction() ) ;
        addButton( SHOW_AUTO_ASSOC_TOPICS,     menuBar.getAutoAssocTopicsAction() ) ;
        addButton( SHOW_TOGGLE_ANS_MARKER,     menuBar.getMarkAnsKeyAction() ) ;
        addButton( SHOW_EXTRACT_ANSWERS,       menuBar.getExtractAnswersAction() ) ;
        addButton( SHOW_CLOSE_TAB,             menuBar.getCloseTabAction() ) ;
        addGroupSeparator() ;

        addButton( SHOW_OPEN_QUESTION_SCRAPER, menuBar.getImageScraperAction() ) ;
        addButton( SHOW_OPEN_ANSWER_MAPPER,    menuBar.getAnsMappingAction() ) ;
        addButton( SHOW_OPEN_TOPIC_MAPPER,     menuBar.getTopicMappingAction() ) ;
        addButton( SHOW_OPEN_QUESTION_SYNC,    menuBar.getQSyncAction() ) ;
        addGroupSeparator() ;

        addButton( SHOW_SET_IMG_CUTTING_COMPLETE, menuBar.getImgCuttingCompleteAction() ) ;
        addButton( SHOW_SET_ANSWERS_MAPPED,       menuBar.getAnswersMappedAction() ) ;
        addButton( SHOW_SET_TOPICS_MAPPED,        menuBar.getTopicsMappedAction() ) ;
        addButton( SHOW_SET_SERVER_SYNCED,        menuBar.getQSyncCompleteAction() ) ;
        addGroupSeparator() ;

        addButton( SHOW_ZOOM_FIT_PAGE, menuBar.getZoomFitPageAction() ) ;
        addButton( SHOW_ZOOM_WIDTH,    menuBar.getZoomWidthAction() ) ;
        addGroupSeparator() ;

        addButton( SHOW_EXPAND_ALL,       menuBar.getExpandAllAction() ) ;
        addButton( SHOW_COLLAPSE_ALL,     menuBar.getCollapseAllAction() ) ;
        addButton( SHOW_EXPAND_SYLLABUS,  menuBar.getExpandSyllabusAction() ) ;
        addToggleButton( SHOW_ONLY_UNSYNCED, menuBar.getShowOnlyUnsyncedAction() ) ;
        addButton( SHOW_SYNC_ALL_PENDING, menuBar.getSyncAllPendingAction() ) ;
    }

    private void addButton( boolean show, Action action ) {
        if( show ) {
            addConfiguredButton( new JButton( action ), action ) ;
        }
    }

    // Toggle button state is bound to the action's SELECTED_KEY, which is
    // shared with the corresponding check box menu item.
    private void addToggleButton( boolean show, Action action ) {
        if( show ) {
            addConfiguredButton( new JToggleButton( action ), action ) ;
        }
    }

    private void addConfiguredButton( AbstractButton btn, Action action ) {
        btn.setHideActionText( true ) ;
        btn.setFocusable( false ) ;
        btn.setToolTipText( getToolTip( action ) ) ;
        super.add( btn ) ;
        groupHasButtons = true ;
    }

    private void addGroupSeparator() {
        if( groupHasButtons ) {
            super.addSeparator() ;
            groupHasButtons = false ;
        }
    }

    private static String getToolTip( Action action ) {
        String name = ( String )action.getValue( Action.NAME ) ;
        KeyStroke accel = ( KeyStroke )action.getValue( Action.ACCELERATOR_KEY ) ;
        if( accel == null ) {
            return name ;
        }
        String modifiers = InputEvent.getModifiersExText( accel.getModifiers() ) ;
        String keyText = KeyEvent.getKeyText( accel.getKeyCode() ) ;
        return name + "  (" + ( modifiers.isEmpty() ? "" : modifiers + "+" ) + keyText + ")" ;
    }
}
