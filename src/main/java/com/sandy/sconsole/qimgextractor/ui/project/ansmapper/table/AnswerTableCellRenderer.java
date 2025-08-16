package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.util.List;

public class AnswerTableCellRenderer extends DefaultTableCellRenderer {
    
    private final ProjectModel projectModel ;
    private final List<Question> questionList ;
    
    public AnswerTableCellRenderer( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        this.questionList = projectModel.getQuestionRepo().getQuestionList() ;
        setHorizontalAlignment( JLabel.CENTER ) ;
        setVerticalAlignment( JLabel.CENTER ) ;
        setOpaque( true ) ;
        setFont( new Font( "Courier", Font.PLAIN, 12 ) ) ;
    }
    
    @Override
    public Component getTableCellRendererComponent( JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column ) {
        Component comp = super.getTableCellRendererComponent( table, value, isSelected, hasFocus, row, column ) ;
        comp.setBackground( Color.YELLOW ) ;
        return comp ;
    }
}
