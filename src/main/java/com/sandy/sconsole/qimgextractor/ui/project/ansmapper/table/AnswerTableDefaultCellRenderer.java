package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AnswerTableDefaultCellRenderer extends DefaultTableCellRenderer {
    
    private Color phyColor = null ;
    private Color chemColor = null ;
    private Color mathsColor = null ;
    
    public AnswerTableDefaultCellRenderer() {
        setHorizontalAlignment( JLabel.LEFT ) ;
        setVerticalAlignment( JLabel.CENTER ) ;
        setOpaque( true ) ;
        setFont( new Font( "Courier", Font.PLAIN, 12 ) ) ;
    }
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value,
                                                    boolean isSelected, boolean hasFocus,
                                                    int row, int column ) {
        Component comp = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column ) ;
        if( column == 0 || column == 2 || column == 4 ) {
            if( !value.toString().isEmpty() ) {
                renderQID( (JLabel)comp, value.toString() ) ;
            }
        }
        else {
            renderAnswer( ( JLabel )comp, (String)value ) ;
        }
        return comp ;
    }
    
    private void renderQID( JLabel comp, String cellValue ) {
        comp.setBackground( getColor( cellValue ) ) ;
        comp.setForeground( Color.DARK_GRAY ) ;
        
        cellValue = cellValue.replaceFirst( "/", " " ) ;
        cellValue = cellValue.replaceFirst( "/", " " ) ;
        cellValue = cellValue.replace( "/", "." ) ;
        comp.setText( " " + cellValue ) ;
    }
    
    private void renderAnswer( JLabel label, String value ) {
        label.setBackground( Color.WHITE ) ;
        if( value == null ) {
            label.setBackground( Color.PINK ) ;
            label.setText( "" ) ;
        }
        else {
            label.setText( " " + value ) ;
        }
        label.setForeground( Color.DARK_GRAY ) ;
    }
    
    private Color getColor( String cellValue ) {
        if( cellValue.startsWith( "P" ) ) {
            if( phyColor == null ) {
                phyColor = SwingUtils.PHY_COLOR.brighter().brighter() ;
            }
            return phyColor ;
        }
        else if( cellValue.startsWith( "C" ) ) {
            if( chemColor == null ) {
                chemColor = SwingUtils.CHEM_COLOR.brighter() ;
            }
            return chemColor ;
        }
        else if( cellValue.startsWith( "M" ) ) {
            if( mathsColor == null ) {
                mathsColor = SwingUtils.MATHS_COLOR.brighter() ;
            }
            return mathsColor ;
        }
        return Color.LIGHT_GRAY ;
    }
}
