package com.sandy.sconsole.qimgextractor.ui.project.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.util.AppState;

import java.util.ArrayList;
import java.util.List;

import static com.sandy.sconsole.qimgextractor.QImgExtractor.getAppState;

public class VerticalGuides {
    
    private final AppState appState ;
    private final ImgCanvas parent ;
    private final List<Integer> markerPositions = new ArrayList<>() ;
    
    public VerticalGuides( ImgCanvas canvas ) {
        this.appState = getAppState() ;
        this.parent = canvas ;
    }
    
    /**
     * Adds a vertical guide at the given x coordinate (translated to
     * absolute coordinates).
     *
     * @param x The x coordinate of the guide in the scaled coordinate
     *          of the canvas. It will be scaled back to the global scale
     *          before adding the guide.
     */
    public void addGuide( int x ) {
        int globalX = (int)( x / parent.getScaleFactor() ) ;
        appState.addVerticalMarker( globalX ) ;
    }
    
    public List<Integer> getGuidePositions() {
        markerPositions.clear() ;
        markerPositions.addAll( appState.getVerticalMarkers() ) ;
        markerPositions.replaceAll( marker -> (int)( marker * parent.getScaleFactor() ) ) ;
        return markerPositions ;
    }
}
