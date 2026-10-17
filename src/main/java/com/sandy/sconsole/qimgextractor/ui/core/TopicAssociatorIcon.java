package com.sandy.sconsole.qimgextractor.ui.core;

import com.sandy.sconsole.qimgextractor.ui.project.model.TopicAssociator;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Ellipse2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

/**
 * A badge icon showing who associated a question with its topic.
 * <ul>
 *   <li>AI : a purple "AI" badge, optionally followed by the confidence level.</li>
 *   <li>User : a green badge with a person glyph.</li>
 *   <li>Unknown (associated before provenance was recorded) : a grey "?" badge.</li>
 * </ul>
 * The icons are drawn rather than loaded since the confidence level varies.
 */
public class TopicAssociatorIcon implements Icon {

    private static final int HEIGHT = 16 ;
    private static final int PAD = 3 ;
    private static final Font FONT = new Font( "SansSerif", Font.BOLD, 9 ) ;
    private static final FontMetrics FM ;

    // Must precede the icon constants below, whose construction needs FM.
    static {
        Graphics2D g = new BufferedImage( 1, 1, BufferedImage.TYPE_INT_ARGB ).createGraphics() ;
        FM = g.getFontMetrics( FONT ) ;
        g.dispose() ;
    }

    private static final Color AI_COLOR      = new Color( 111, 66, 193 ) ;
    private static final Color AI_CONF_COLOR = new Color( 233, 225, 248 ) ;
    private static final Color USER_COLOR    = new Color( 40, 140, 70 ) ;
    private static final Color UNKNOWN_COLOR = Color.GRAY ;

    private static final Map<Integer, TopicAssociatorIcon> AI_ICONS_WITH_CONF = new HashMap<>() ;
    private static final TopicAssociatorIcon AI_ICON      = new TopicAssociatorIcon( Kind.AI, -1 ) ;
    private static final TopicAssociatorIcon USER_ICON    = new TopicAssociatorIcon( Kind.USER, -1 ) ;
    private static final TopicAssociatorIcon UNKNOWN_ICON = new TopicAssociatorIcon( Kind.UNKNOWN, -1 ) ;

    private enum Kind { AI, USER, UNKNOWN }

    private final Kind kind ;
    private final String confText ; // Null if the confidence is not shown
    private final int labelWidth ;
    private final int width ;

    /**
     * @param showConfidence true to show the confidence level inside the AI
     *        badge. Ignored for other actors.
     */
    public static Icon forAssociator( TopicAssociator associator, boolean showConfidence ) {
        if( associator == null ) {
            return UNKNOWN_ICON ;
        }
        if( TopicAssociator.ACTOR_AI.equals( associator.getActor() ) ) {
            if( !showConfidence ) {
                return AI_ICON ;
            }
            synchronized( AI_ICONS_WITH_CONF ) {
                return AI_ICONS_WITH_CONF.computeIfAbsent( associator.getConfidence(),
                        c -> new TopicAssociatorIcon( Kind.AI, c ) ) ;
            }
        }
        if( TopicAssociator.ACTOR_USER.equals( associator.getActor() ) ) {
            return USER_ICON ;
        }
        return UNKNOWN_ICON ;
    }

    private TopicAssociatorIcon( Kind kind, int confidence ) {
        this.kind = kind ;
        this.confText = confidence >= 0 ? String.valueOf( confidence ) : null ;
        this.labelWidth = switch( kind ) {
            case AI      -> FM.stringWidth( "AI" ) + 2*PAD ;
            case USER    -> HEIGHT ;
            case UNKNOWN -> FM.stringWidth( "?" ) + 2*PAD + 2 ;
        } ;
        this.width = confText == null ? labelWidth : labelWidth + FM.stringWidth( confText ) + 2*PAD ;
    }

    @Override
    public void paintIcon( Component c, Graphics g, int x, int y ) {

        Graphics2D g2 = (Graphics2D)g.create() ;
        g2.setRenderingHint( RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON ) ;
        g2.setRenderingHint( RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON ) ;
        g2.translate( x, y ) ;
        g2.setFont( FONT ) ;

        RoundRectangle2D badge = new RoundRectangle2D.Float( 0, 1, width-1, HEIGHT-2, 6, 6 ) ;
        int textY = ( HEIGHT - FM.getHeight() )/2 + FM.getAscent() ;

        switch( kind ) {
            case AI -> {
                if( confText != null ) {
                    g2.setColor( AI_CONF_COLOR ) ;
                    g2.fill( badge ) ;
                    g2.setColor( AI_COLOR ) ;
                    g2.drawString( confText, labelWidth + PAD, textY ) ;
                    g2.draw( badge ) ;
                    g2.setClip( 0, 0, labelWidth, HEIGHT ) ;
                }
                g2.setColor( AI_COLOR ) ;
                g2.fill( badge ) ;
                g2.setColor( Color.WHITE ) ;
                g2.drawString( "AI", PAD, textY ) ;
            }
            case USER -> {
                g2.setColor( USER_COLOR ) ;
                g2.fill( badge ) ;
                g2.clip( badge ) ;
                g2.setColor( Color.WHITE ) ;
                g2.fill( new Ellipse2D.Float( 5.5f, 3, 5, 5 ) ) ;            // Head
                g2.fill( new RoundRectangle2D.Float( 3, 9, 10, 8, 6, 6 ) ) ; // Shoulders
            }
            case UNKNOWN -> {
                g2.setColor( UNKNOWN_COLOR ) ;
                g2.fill( badge ) ;
                g2.setColor( Color.WHITE ) ;
                g2.drawString( "?", PAD+1, textY ) ;
            }
        }
        g2.dispose() ;
    }

    @Override
    public int getIconWidth() {
        return width ;
    }

    @Override
    public int getIconHeight() {
        return HEIGHT ;
    }
}
