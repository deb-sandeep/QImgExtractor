package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;

import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.EventObject;

public class QSyncTableSyncButtonEditor extends AbstractCellEditor implements TableCellEditor {
    
    private final JButton button = new JButton();
    private final JTable table ;
    
    private int row ;
    
    public interface ButtonClickHandler {
        void syncButtonClick( int row ) ;
    }
    
    public QSyncTableSyncButtonEditor( QSyncTreeTable table ) {
        this.table = table;
        
        button.addActionListener((ActionEvent e) -> {
            ((ButtonClickHandler)this.table).syncButtonClick( row ) ;
            fireEditingStopped();
        });
        button.setIcon( SwingUtils.getIcon( "sync_button_pressed" ) ) ;
    }
    
    @Override
    public Component getTableCellEditorComponent(
            JTable table, Object value, boolean isSelected,
            int row, int column) {
        
        this.row = row ;
        return button ;
    }
    
    @Override
    public Object getCellEditorValue() {
        // Button column usually doesn’t change underlying value
        return null ;
    }
    
    // Make it respond to single-click, not double-click
    @Override
    public boolean isCellEditable( EventObject e ) {
        return true ;
    }
}
