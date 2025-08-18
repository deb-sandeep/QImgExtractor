package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.img;

import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.PageImage;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.util.AppUtil;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ImgPanel extends JPanel {

    private final AnswerMapperUI parent ;
    private final ProjectModel projectModel ;
    
    private final JTabbedPane tabbedPane ;
    
    public ImgPanel( AnswerMapperUI parent ) {
        this.parent = parent ;
        this.projectModel = parent.getProjectModel() ;
        this.tabbedPane = new JTabbedPane() ;
        
        setPreferredSize( new Dimension( 1000, 300 ) ) ;
        setLayout( new BorderLayout() ) ;
        add( tabbedPane, BorderLayout.CENTER ) ;
    }
    
    public void refreshAnswerKeyPages() {
        List<PageImage> ansKeyPages = projectModel.getPageImages()
                .stream()
                .filter( f -> f.getState().isHasAnswerKeys() )
                .toList() ;
        
        if( ansKeyPages.isEmpty() ) {
            tabbedPane.removeAll() ;
        }
        else {
            addTabsWhichAreAnswerKeys( ansKeyPages ) ;
            removeTabsWhichAreNotAnswerKeys( ansKeyPages ) ;
        }
    }
    
    private void addTabsWhichAreAnswerKeys( List<PageImage> ansKeyPages ) {
        for( PageImage pageImg : ansKeyPages ) {
            boolean pageAttached = false ;
            String pageTitle = AppUtil.stripExtension( pageImg.getImgFile() ) ;
            
            for( int i=0; i<tabbedPane.getTabCount(); i++ ) {
                if( tabbedPane.getTitleAt( i ).equals( pageTitle ) ) {
                    pageAttached = true ;
                    break ;
                }
            }
            
            if( !pageAttached ) {
                ImgLabel imgLabel = new ImgLabel( this, parent, pageImg ) ;
                JScrollPane scrollPane = new JScrollPane( imgLabel ) ;
                scrollPane.getVerticalScrollBar().setUnitIncrement( 10 ) ;
                scrollPane.getHorizontalScrollBar().setUnitIncrement( 10 ) ;
                tabbedPane.addTab( pageTitle, scrollPane ) ;
            }
        }
    }
    
    private void removeTabsWhichAreNotAnswerKeys( List<PageImage> ansKeyPages ) {
        List<Integer> tabsToRemove = new ArrayList<>() ;
        for( int i=0; i<tabbedPane.getTabCount(); i++ ) {
            String tabTitle = tabbedPane.getTitleAt( i ) ;
            boolean tabNotAnsKey = true ;
            for( PageImage pageImg : ansKeyPages ) {
                String pageTitle = AppUtil.stripExtension( pageImg.getImgFile() ) ;
                if( tabTitle.equals( pageTitle ) ) {
                    tabNotAnsKey = false ;
                    break ;
                }
            }
            
            if( tabNotAnsKey ) {
                tabsToRemove.add( i ) ;
            }
        }
        for( int i=tabsToRemove.size()-1; i>=0; i-- ) {
            tabbedPane.removeTabAt( tabsToRemove.get( i ) ) ;
        }
    }
}
