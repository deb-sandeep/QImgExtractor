package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.qid.QID;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.extern.slf4j.Slf4j;

import javax.swing.table.DefaultTableModel;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

@Slf4j
public class AnswerTableModel extends DefaultTableModel {
    
    public static final int COL_COUNT = 6 ;
    
    private final ProjectModel projectModel ;
    private final List<Question> questionList ;
    private final AnswerTable table ;
    
    private final List<Question> phyQuestions = new ArrayList<>() ;
    private final List<Question> chemQuestions = new ArrayList<>() ;
    private final List<Question> mathsQuestions = new ArrayList<>() ;
    
    private int rowCount = 0 ;
    
    public AnswerTableModel( ProjectModel projectModel, AnswerTable table ) {
        this.projectModel = projectModel ;
        this.table = table ;
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
        List<Question> questions = getQuestionsForColumn( column ) ;
        if( row < questions.size() ) {
            Question q = questions.get( row ) ;
            if( column == 0 || column == 2 || column == 4 ) {
                return q.getQID().toString() ;
            }
            else {
                if( q.getQID().getQuestionType().equals( QID.MMT ) ) {
                    return q.getMmtAnswer() ;
                }
                else {
                    return q.getAnswer() ;
                }
            }
        }
        return "" ;
    }
    
    @Override
    public void setValueAt( Object value, int row, int col ) {
        if( value instanceof String ansText ) {
            Stack<String> ansStack = new Stack<>() ;
            if( ansText.indexOf( ' ' ) != -1 ) {
                String[] parts = ansText.split( " " ) ;
                for( int i=parts.length-1; i>=0; i-- ) {
                    String part = parts[i].trim() ;
                    if( !part.isEmpty() ) {
                        ansStack.push( part.trim() ) ;
                    }
                }
            }
            else if( ansText.length() > 3 && ansText.matches( "[A-Da-d1-4]+" ) ) {
                for( int i=ansText.length()-1; i>=0; i-- ) {
                    char c =  ansText.charAt( i ) ;
                    if( c == 'a' || c == 'b' || c == 'c' || c == 'd' ) {
                        c = Character.toUpperCase( c ) ;
                    }
                    ansStack.push( c + "" ) ;
                }
            }
            else {
                ansStack.push( ansText ) ;
            }
            
            try {
                while( !ansStack.isEmpty() ) {
                    setRawAnswer( row, col, ansStack ) ;
                    row++ ;
                }
                table.setSelectedCell( row, col ) ;
                projectModel.getQuestionRepo().save() ;
            }
            catch( Question.InvalidAnswerException e ) {
                AppUtil.showErrorMsg( e.getMessage() ) ;
            }
        }
    }
    
    public Question getQuestionAt( int row, int column ) {
        List<Question> questions = getQuestionsForColumn( column ) ;
        if( row < questions.size() ) {
            return questions.get( row ) ;
        }
        return null ;
    }
    
    private List<Question> getQuestionsForColumn( int column ) {
        List<Question> questions ;
        switch( column ) {
            case 0,1 -> questions = phyQuestions ;
            case 2,3 -> questions = chemQuestions ;
            case 4,5 -> questions = mathsQuestions ;
            default -> questions = Collections.emptyList() ;
        }
        return questions ;
    }
    
    @Override
    public boolean isCellEditable( int row, int column ) {
        if( isAnswerColumn( column ) ) {
            List<Question> questions = getQuestionsForColumn( column ) ;
            return row < questions.size() ;
        }
        return false ;
    }
    
    public void setRawAnswer( int row, int col, Stack<String> ansStack )
        throws Question.InvalidAnswerException {
        
        List<Question> questions = getQuestionsForColumn( col ) ;
        if( row < questions.size() ) {
            Question q = questions.get( row ) ;
            StringBuilder ansText = new StringBuilder();
            if( q.getQID().getQuestionType().equals( QID.MMT ) ) {
                if( ansStack.size() == 1 && ansStack.peek().contains( "#" )) {
                    ansText.append( ansStack.pop().trim() ) ;
                }
                else if( ansStack.size() > 3 ) {
                    for( int i=0; i<4; i++ ) {
                        ansText.append( ansStack.pop().trim() ).append( "#" );
                    }
                    if( ansText.charAt( ansText.length()-1 ) == '#' ) {
                        ansText.deleteCharAt( ansText.length()-1 ) ;
                    }
                }
                else {
                    throw new Question.InvalidAnswerException(
                            "Insufficient text for creating answer to question " + q.getQID() ) ;
                }
            }
            else {
                ansText.append( ansStack.pop() ) ;
            }
            q.setRawAnswer( ansText.toString() ) ;
            markAnswersChanged() ;
            fireTableCellUpdated( row, col ) ;
        }
        else {
            throw new Question.InvalidAnswerException( "Invalid row: " + row + ", column: " + col ) ;
        }
    }

    // If we are going back to a more nascent stage, then
    // erase the advanced stage markers
    private void markAnswersChanged() {
        if( projectModel.getState().isTopicsMapped() ||
            projectModel.getState().isSavedToServer() ) {
            projectModel.getState().setAnswersMapped( true ) ;
        }
    }

    public boolean isAnswerColumn( int column ) {
        return column == 1 || column == 3 || column == 5 ;
    }

    // Returns the answer of the last question in the column, which will be
    // lost if the answers are pushed down.
    public String getLastAnswerInColumn( int col ) {
        List<Question> questions = getQuestionsForColumn( col ) ;
        return questions.isEmpty() ? null : questions.get( questions.size()-1 ).getAnswer() ;
    }

    // Clears the answer at the given cell and shifts all the answers below
    // it one row down. The answer of the last question falls off. Answers
    // which don't validate against the target question type are dropped.
    // Returns descriptions of the dropped answers.
    public List<String> pushAnswersDown( int row, int col ) {

        List<Question> questions = getQuestionsForColumn( col ) ;
        List<String> droppedAnswers = new ArrayList<>() ;
        if( !isAnswerColumn( col ) || row >= questions.size() ) {
            return droppedAnswers ;
        }

        List<String> answers = questions.stream().map( Question::getAnswer ).toList() ;
        for( int i=questions.size()-1; i>row; i-- ) {
            transferAnswer( answers.get( i-1 ), questions.get( i ), droppedAnswers ) ;
        }
        questions.get( row ).clearAnswer() ;

        postAnswerShift() ;
        return droppedAnswers ;
    }

    // Deletes the answer at the given cell and shifts all the answers below
    // it one row up. The last question's answer is cleared. Answers which
    // don't validate against the target question type are dropped.
    // Returns descriptions of the dropped answers.
    public List<String> pullAnswersUp( int row, int col ) {

        List<Question> questions = getQuestionsForColumn( col ) ;
        List<String> droppedAnswers = new ArrayList<>() ;
        if( !isAnswerColumn( col ) || row >= questions.size() ) {
            return droppedAnswers ;
        }

        List<String> answers = questions.stream().map( Question::getAnswer ).toList() ;
        for( int i=row; i<questions.size()-1; i++ ) {
            transferAnswer( answers.get( i+1 ), questions.get( i ), droppedAnswers ) ;
        }
        questions.get( questions.size()-1 ).clearAnswer() ;

        postAnswerShift() ;
        return droppedAnswers ;
    }

    private void transferAnswer( String answer, Question target, List<String> droppedAnswers ) {
        target.clearAnswer() ;
        if( answer != null ) {
            try {
                target.setRawAnswer( answer ) ;
            }
            catch( Question.InvalidAnswerException e ) {
                droppedAnswers.add( target.getQID() + " <- " + answer ) ;
            }
        }
    }

    private void postAnswerShift() {
        markAnswersChanged() ;
        fireTableDataChanged() ;
        projectModel.getQuestionRepo().save() ;
    }
}
