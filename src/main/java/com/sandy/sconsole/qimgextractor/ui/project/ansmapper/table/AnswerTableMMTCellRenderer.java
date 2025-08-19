package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.MMTAnswer;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

@Slf4j
public class AnswerTableMMTCellRenderer extends DefaultTableCellRenderer {
    
    private static final int MARGIN = 2 ;
    
    private MMTAnswer currentAnswer = null ;
    
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
        this.currentAnswer = null ;
        if( value instanceof MMTAnswer answer ) {
            this.currentAnswer = answer ;
        }
        return label ;
    }
    
    @Override
    public void paint( Graphics g ) {
        super.paint( g ) ;
        Graphics2D g2d = ( Graphics2D )g ;
        if( currentAnswer != null ) {
            drawAnswerMappings( g2d ) ;
        }
        drawGrid( g2d ) ;
    }
    
    private void drawGrid( Graphics2D g ) {
        
        int cellWidth = getCellWidth() ;
        int cellHeight = getCellHeight() ;
        int numRows = getNumRows() ;
        int numCols = getNumCols() ;
        
        g.setColor( Color.GRAY ) ;
        g.drawRect( MARGIN, MARGIN, getWidth() - 2*MARGIN, getHeight() - 2*MARGIN ) ;
        
        for( int row=1; row<numRows; row++ ) {
            int gridY = MARGIN + row*cellHeight ;
            g.drawLine( MARGIN, gridY, getWidth()-MARGIN, gridY ) ;
        }
        for( int col=1; col<numCols; col++ ) {
            int gridX = MARGIN + col*cellWidth ;
            g.drawLine( gridX, MARGIN, gridX, getHeight()-MARGIN ) ;
        }
    }
    
    private void drawAnswerMappings( Graphics2D g ) {
        
        int cellWidth = getCellWidth() ;
        int cellHeight = getCellHeight() ;
        int numRows = getNumRows() ;
        int numCols = getNumCols() ;
        
        for( int row=0; row<numRows; row++ ) {
            for( int col=0; col<numCols; col++ ) {
                if( currentAnswer.isCorrectMapping( row, col ) ) {
                    int gridX = MARGIN + col*cellWidth ;
                    int gridY = MARGIN + row*cellHeight ;
                    g.setColor( Color.GREEN ) ;
                    g.fillRect( gridX, gridY, cellWidth, cellHeight ) ;
                }
            }
        }
    }
    
    private int getNumRows() {
        return currentAnswer == null ? 4 : currentAnswer.getNumRows() ;
    }
    
    private int getNumCols() {
        return currentAnswer == null ? 4 : currentAnswer.getNumCols() ;
    }
    
    private int getCellWidth() {
        return (int)Math.round( (double)( getWidth() - 2*MARGIN )/getNumCols() ) ;
    }
    
    private int getCellHeight() {
        return (int)Math.round( (double)( getHeight() - 2*MARGIN )/getNumRows() ) ;
    }
}
