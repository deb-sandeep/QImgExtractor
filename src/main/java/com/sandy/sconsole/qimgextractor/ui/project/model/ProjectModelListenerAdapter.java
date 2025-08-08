package com.sandy.sconsole.qimgextractor.ui.project.model;

public class ProjectModelListenerAdapter implements ProjectModelListener {
    
    @Override
    public void newQuestionImgAdded( PageImage pageImage, QuestionImage qImg ) {}
    
    @Override
    public void questionTagNameChanged( QuestionImage qImg, String oldTagName, String newTagName ) {}
    
    @Override
    public void questionImgDeleted( QuestionImage qImg ) {}
    
    @Override
    public void partSelectionModeUpdated( boolean newMode ) {}
}
