package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.table;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;

import javax.swing.*;

public class AnswerTable extends JTable {
    
    private final ProjectModel projectModel ;
    
    public AnswerTable( ProjectModel projectModel ) {
        this.projectModel = projectModel ;
    }
}
