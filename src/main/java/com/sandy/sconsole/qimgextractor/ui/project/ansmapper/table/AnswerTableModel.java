package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;

import javax.swing.table.DefaultTableModel;
import java.util.List;

public class AnswerTableModel extends DefaultTableModel {
    
    private final ProjectModel projectModel ;
    private final List<Question> questionList ;
    
    public AnswerTableModel( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        this.questionList = projectModel.getQuestionRepo().getQuestionList() ;
    }
    
    @Override
    public int getRowCount() {
        return super.getRowCount();
    }
    
    @Override
    public int getColumnCount() {
        return super.getColumnCount();
    }
    
    @Override
    public Object getValueAt( int row, int column ) {
        return super.getValueAt( row, column );
    }
    
    @Override
    public boolean isCellEditable( int row, int column ) {
        return super.isCellEditable( row, column );
    }
}
