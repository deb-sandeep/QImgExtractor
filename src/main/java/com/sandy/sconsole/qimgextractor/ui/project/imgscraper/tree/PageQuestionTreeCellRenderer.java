package com.sandy.sconsole.qimgextractor.ui.project.imgscraper.tree;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;
import java.awt.image.BufferedImage;

@Slf4j
public class PageQuestionTreeCellRenderer extends DefaultTreeCellRenderer {
    
    public static final Font PAGE_FONT  = new Font( "Helvetica", Font.BOLD, 12 ) ;
    public static final Font SUB_IMG_FONT  = new Font( "Helvetica", Font.PLAIN, 11 ) ;
    
    // Same color as the tab foreground of a page marked as having answer keys.
    public static final Color ANS_KEY_PAGE_COLOR = Color.RED ;
    
    private final ImageIcon pageIcon = SwingUtils.getIcon( "page_image" ) ;
    private final ImageIcon ansKeyPageIcon = createTintedIcon( "page_image", ANS_KEY_PAGE_COLOR ) ;

    @Override
    public Component getTreeCellRendererComponent(
            JTree tree, Object value, boolean sel, boolean expanded,
            boolean leaf, int row, boolean hasFocus ) {
        
        Component comp =  super.getTreeCellRendererComponent(
                tree, value, sel, expanded,
                leaf, row, hasFocus ) ;
        
        if( value instanceof DefaultMutableTreeNode ) {
            Object userObj = (( DefaultMutableTreeNode )value).getUserObject() ;
            if( userObj instanceof PageImage pageImage ) {
                renderPageImgNode( comp, pageImage ) ;
            }
            else if( userObj instanceof QuestionImage qImg ) {
                renderQuestionImgNode( comp, qImg ) ;
            }
        }
        
        return comp ;
    }
    
    private void renderPageImgNode( Component comp, PageImage pageImage ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( PAGE_FONT ) ;
        label.setText( AppUtil.stripExtension( pageImage.getImgFile() ) ) ;
        if( pageImage.getState().isHasAnswerKeys() ) {
            label.setIcon( ansKeyPageIcon ) ;
            if( !selected ) label.setForeground( ANS_KEY_PAGE_COLOR ) ;
        }
        else {
            label.setIcon( pageIcon ) ;
        }
    }
    
    private void renderQuestionImgNode( Component comp, QuestionImage qImg ) {
        DefaultTreeCellRenderer label = ( DefaultTreeCellRenderer )comp ;
        label.setFont( SUB_IMG_FONT ) ;
        label.setText( qImg.getImgRegionMetadata().getTag() ) ;
        label.setIcon( SwingUtils.getIcon( "sub_image" ) );
    }
    
    // Recolors the icon with the given color, retaining the alpha channel and
    // using the pixel luminance as shading so that the icon's details survive.
    private static ImageIcon createTintedIcon( String iconName, Color tint ) {
        BufferedImage src = SwingUtils.getIconImage( iconName ) ;
        BufferedImage dest = new BufferedImage( src.getWidth(), src.getHeight(),
                                                BufferedImage.TYPE_INT_ARGB ) ;
        for( int y=0; y<src.getHeight(); y++ ) {
            for( int x=0; x<src.getWidth(); x++ ) {
                int argb  = src.getRGB( x, y ) ;
                int alpha = ( argb >>> 24 ) ;
                int r = ( argb >> 16 ) & 0xFF ;
                int g = ( argb >>  8 ) & 0xFF ;
                int b = argb & 0xFF ;
                
                // Dark pixels take the full tint, light pixels fade towards white.
                float lum = ( 0.299f*r + 0.587f*g + 0.114f*b ) / 255f ;
                int tr = (int)( tint.getRed()   + ( 255 - tint.getRed()   )*lum*0.25f ) ;
                int tg = (int)( tint.getGreen() + ( 255 - tint.getGreen() )*lum*0.25f ) ;
                int tb = (int)( tint.getBlue()  + ( 255 - tint.getBlue()  )*lum*0.25f ) ;
                dest.setRGB( x, y, ( alpha << 24 ) | ( tr << 16 ) | ( tg << 8 ) | tb ) ;
            }
        }
        return new ImageIcon( dest ) ;
    }
}
