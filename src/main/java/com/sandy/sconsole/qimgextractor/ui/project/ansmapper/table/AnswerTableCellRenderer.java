package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;

public class AnswerTableCellRenderer extends DefaultTableCellRenderer {
    
    private final AnswerTableModel tableModel ;
    private Color phyColor = null ;
    private Color chemColor = null ;
    private Color mathsColor = null ;
    
    public AnswerTableCellRenderer( AnswerTableModel tableModel ) {
        this.tableModel = tableModel;
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
        String cellValue = ( String )tableModel.getValueAt( row, column ) ;
        
        if( column == 0 || column == 2 || column == 4 ) {
            if( !cellValue.isEmpty() ) {
                renderQID( (JLabel)comp, cellValue ) ;
            }
        }
        else {
            comp.setBackground( Color.WHITE ) ;
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
    
    private Color getColor( String cellValue ) {
        if( cellValue.startsWith( "P" ) ) {
            if( phyColor == null ) {
                phyColor = Color.decode( "#FFC468" ).brighter().brighter() ;
            }
            return phyColor ;
        }
        else if( cellValue.startsWith( "C" ) ) {
            if( chemColor == null ) {
                chemColor = Color.decode( "#84FF85" ).brighter() ;
            }
            return chemColor ;
        }
        else if( cellValue.startsWith( "M" ) ) {
            if( mathsColor == null ) {
                mathsColor = Color.decode( "#97D6FF" ).brighter() ;
            }
            return mathsColor ;
        }
        return Color.LIGHT_GRAY ;
    }
}
