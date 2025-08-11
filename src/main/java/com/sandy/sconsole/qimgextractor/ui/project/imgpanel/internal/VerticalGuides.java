package com.sandy.sconsole.qimgextractor.ui.project.imgpanel.internal;

import com.sandy.sconsole.qimgextractor.util.AppState;

import java.awt.*;
import java.awt.event.MouseEvent;
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
    
    public void clear() {
        markerPositions.clear() ;
        appState.clearVerticalMarkers() ;
    }
    
    private int getClosestGuidePosLessThanOrEqualTo( int x, List<Integer> positions ) {
        
        int minDist = Integer.MAX_VALUE ;
        int closestPos = -1 ;
        
        for( int i=0; i<positions.size(); i++ ) {
            int guideX = positions.get( i ) ;
            if( guideX <= x ) {
                int dist = x - guideX ;
                if( dist < minDist ) {
                    minDist = dist ;
                    closestPos = guideX ;
                }
            }
        }
        return closestPos ;
    }
    
    private int getClosestGuidePosGreaterThanOrEqualTo( int x, List<Integer> positions ) {
        
        int minDist = Integer.MAX_VALUE ;
        int closestPos = -1 ;
        
        for( int i=0; i<positions.size(); i++ ) {
            int guideX = positions.get( i ) ;
            if( guideX >= x ) {
                int dist = guideX - x ;
                if( dist < minDist ) {
                    minDist = dist ;
                    closestPos = guideX ;
                }
            }
        }
        return closestPos ;
    }
    
    public Point getStartSnapPoint( MouseEvent event ) {
        List<Integer> positions = getGuidePositions() ;
        if( getGuidePositions().isEmpty() || event.isControlDown() ) {
            return event.getPoint() ;
        }
        
        int y = event.getY() ;
        int x = getClosestGuidePosLessThanOrEqualTo( event.getX(), positions ) ;
        if( x < 0 ) {
            x = getClosestGuidePosGreaterThanOrEqualTo( event.getX(), positions ) ;
        }
        return new Point( x, y ) ;
    }
    
    public Point getEndSnapPoint( MouseEvent event ) {
        List<Integer> positions = getGuidePositions() ;
        if( getGuidePositions().isEmpty() || event.isControlDown() ) {
            return event.getPoint() ;
        }

        int y = event.getY() ;
        int x = getClosestGuidePosGreaterThanOrEqualTo( event.getX(), positions ) ;
        if( x < 0 ) {
            x = getClosestGuidePosLessThanOrEqualTo( event.getX(), positions ) ;
        }
        return new Point( x, y ) ;
    }
}
