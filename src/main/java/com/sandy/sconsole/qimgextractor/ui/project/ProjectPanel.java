package com.sandy.sconsole.qimgextractor.ui.project;

import com.sandy.sconsole.qimgextractor.ui.MainFrame;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.imgscraper.ImageScraperUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.swing.*;
import java.awt.*;

@Slf4j
public class ProjectPanel extends JPanel {
    
    @Getter
    private final MainFrame mainFrame ;
    
    @Getter
    private final ProjectModel projectModel ;

    private final ImageScraperUI imgScraperUI ;
    
    public ProjectPanel( MainFrame mainFrame, ProjectModel model ) {
        this.projectModel = model ;
        this.mainFrame = mainFrame ;
        this.imgScraperUI = new ImageScraperUI( this ) ;
        
        setUpUI() ;
    }
    
    private void setUpUI() {
        setLayout( new BorderLayout() ) ;
        add( imgScraperUI, BorderLayout.CENTER ) ;
    }
    
    // This is called once the project panel is successfully attached to the
    // main frame. This is where any long-running operations like loading of
    // page images etc should be done.
    public void performPostInitOperations() {
        imgScraperUI.loadPageImages() ;
    }
    
    public void destroy() {
        this.imgScraperUI.destroy() ;
    }
    
    public void activateQuestionScraperUI() {
    }
    
    public void activateAnswerMapperUI() {
    }
}
