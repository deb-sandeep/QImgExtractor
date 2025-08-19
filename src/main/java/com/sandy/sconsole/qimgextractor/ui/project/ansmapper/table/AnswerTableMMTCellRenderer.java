package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

@Slf4j
public class AnswerTableMMTCellRenderer extends DefaultTableCellRenderer {
    
    private static final int MARGIN = 2 ;
    
    public AnswerTableMMTCellRenderer() {
        setHorizontalAlignment( JLabel.LEFT ) ;
        setVerticalAlignment( JLabel.CENTER ) ;
        setOpaque( true ) ;
        setFont( new Font( "Courier", Font.PLAIN, 12 ) ) ;
    }
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column ) {
        
        JLabel label = (JLabel)super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column ) ;
        label.setBackground( Color.WHITE ) ;
        label.setText( "" ) ;
        return label ;
    }
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        Graphics2D g2d = ( Graphics2D )g ;
        drawGrid( g2d ) ;
    }
    
    private void drawGrid( Graphics2D g ) {
        
        int cellWidth = getCellWidth() ;
        int cellHeight = getCellHeight() ;
        
        g.setColor( Color.LIGHT_GRAY ) ;
        g.drawRect( MARGIN, MARGIN, getWidth() - 2*MARGIN, getHeight() - 2*MARGIN ) ;
        
        for( int row=1; row<4; row++ ) {
            int gridY = MARGIN + row*cellHeight ;
            g.drawLine( MARGIN, gridY, getWidth()-MARGIN, gridY ) ;
        }
        for( int col=1; col<4; col++ ) {
            int gridX = MARGIN + col*cellWidth ;
            g.drawLine( gridX, MARGIN, gridX, getHeight()-MARGIN ) ;
        }
    }
    
    private int getCellWidth() {
        return (int)Math.round( (double)( getWidth() - 2*MARGIN )/4 ) ;
    }
    
    private int getCellHeight() {
        return (int)Math.round( (double)( getHeight() - 2*MARGIN )/4 ) ;
    }
}
