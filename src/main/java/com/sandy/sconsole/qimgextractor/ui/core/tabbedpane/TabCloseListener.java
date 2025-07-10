package com.sandy.sconsole.qimgextractor.ui.core.tabbedpane;

import java.awt.*;

public interface TabCloseListener {
    
    /**
     * This method in invoked when a tab is about to be closed, i.e., before
     * it has been removed from the tab pane but after the user has
     * pressed the close button on the tab.
     */
    void tabClosing( int tabIndex, Component tabComponent ) ;
}
