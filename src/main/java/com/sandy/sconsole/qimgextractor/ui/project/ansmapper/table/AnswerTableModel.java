package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.extern.slf4j.Slf4j;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
public class AnswerTableModel extends DefaultTableModel {
    
    public static final int COL_COUNT = 6 ;
    
    private final ProjectModel projectModel ;
    private final List<Question> questionList ;
    
    private final List<Question> phyQuestions = new ArrayList<>() ;
    private final List<Question> chemQuestions = new ArrayList<>() ;
    private final List<Question> mathsQuestions = new ArrayList<>() ;
    
    private int rowCount = 0 ;
    
    public AnswerTableModel( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
        this.questionList = projectModel.getQuestionRepo().getQuestionList() ;
        super.setColumnIdentifiers( new String[] { "Physics", "Ans", "Chemistry", "Ans", "Maths", "Ans" } ) ;
        refreshModel() ;
    }
    
    public void refreshModel() {
        phyQuestions.clear() ;
        chemQuestions.clear() ;
        mathsQuestions.clear() ;
        
        for( Question q : questionList ) {
            String qId = q.getQID().toString() ;
            if( qId.startsWith( "P" ) ) {
                phyQuestions.add( q ) ;
            }
            else if( qId.startsWith( "C" ) ) {
                chemQuestions.add( q ) ;
            }
            else if( qId.startsWith( "M" ) ) {
                mathsQuestions.add( q ) ;
            }
        }
        rowCount = Math.max( phyQuestions.size(), Math.max( chemQuestions.size(), mathsQuestions.size() ) ) ;
        fireTableDataChanged() ;
    }

    @Override
    public int getRowCount() {
        return rowCount ;
    }
    
    @Override
    public int getColumnCount() {
        return COL_COUNT ;
    }
    
    @Override
    public Object getValueAt( int row, int column ) {
        List<Question> questions ;
        switch( column ) {
            case 0,1 -> questions = phyQuestions ;
            case 2,3 -> questions = chemQuestions ;
            case 4,5 -> questions = mathsQuestions ;
            default -> questions = Collections.emptyList() ;
        }
        
        if( row < questions.size() ) {
            Question q = questions.get( row ) ;
            if( column == 0 || column == 2 || column == 4 ) {
                return q.getQID().toString() ;
            }
            else {
                return q.getAnswer() ;
            }
        }
        return "" ;
    }
    
    @Override
    public void setValueAt( Object value, int row, int column ) {
        log.debug( "Setting value at row: {}, col: {}, value: {}", row, column, value ) ;
    }
    
    @Override
    public boolean isCellEditable( int row, int column ) {
        return column == 1 || column == 3 || column == 5 ;
    }
}
