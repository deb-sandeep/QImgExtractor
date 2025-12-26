package com.sandy.sconsole.qimgextractor.ui.project.qsync.logpanel;

import com.sandy.sconsole.qimgextractor.ui.core.SwingUtils;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient.QuestionImageVO;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.apiclient.QuestionVO;
import org.apache.commons.lang3.StringUtils;

import javax.swing.*;
import java.awt.*;

public class QSyncLogPanel extends JPanel {
    
    private final JTextArea logTextArea ;

    public QSyncLogPanel() {
        setPreferredSize( new java.awt.Dimension( 500, 200 ) ) ;
        this.logTextArea = new JTextArea() ;
        this.setUpUI() ;
    }
    
    private void setUpUI() {
        setUpTextArea() ;
        
        setLayout( new BorderLayout() ) ;
        JScrollPane sp = new JScrollPane( logTextArea ) ;
        add( sp, BorderLayout.CENTER ) ;
        add( getButtonPanel(), BorderLayout.NORTH ) ;
    }
    
    private JPanel getButtonPanel() {
        JPanel buttonPanel = new JPanel() ;
        buttonPanel.setLayout( new FlowLayout( FlowLayout.LEFT ) ) ;
        
        JButton btnClear = new JButton( "Clear" ) ;
        btnClear.setIcon( SwingUtils.getIcon( "bin" ) ) ;
        btnClear.setToolTipText( "Clear Log" ) ;
        btnClear.addActionListener( e -> logTextArea.setText( "" ) ) ;
        buttonPanel.add( btnClear ) ;
        
        return buttonPanel ;
    }
    
    private void setUpTextArea() {
        logTextArea.setEditable( false ) ;
        logTextArea.setForeground( Color.BLACK ) ;
        logTextArea.setFont( new Font( "Courier New", Font.PLAIN, 11 ) ) ;
    }
    
    private void append( String msg ) {
        logTextArea.append( msg + "\n" ) ;
    }
    
    public void logSeparator() {
        append( "\n-------------------------------------------------------------------" ) ;
    }
    
    public void log( String msg ) {
        append( msg ) ;
    }
    
    public void logQuestion( QuestionVO vo ) {
        logNVP( "ID", vo.getId() ) ;
        logNVP( "QID", vo.getQuestionId() ) ;
        logNVP( "Source", vo.getSourceId() ) ;
        logNVP( "Syllabus", vo.getSyllabusName() ) ;
        logNVP( "Topic", vo.getTopicId() ) ;
        logNVP( "Q Type", vo.getProblemType() ) ;
        logNVP( "LCT Seq", vo.getLctSequence() ) ;
        logNVP( "Q No", vo.getQuestionNumber() ) ;
        logNVP( "Answer", vo.getAnswer() ) ;
        
        for( QuestionImageVO imageVO : vo.getQuestionImages() ) {
            append( "................" ) ;
            logNVP( "  Img", imageVO.getFileName() ) ;
            logNVP( "  Sequence", imageVO.getSequence() ) ;
            logNVP( "  Page No", imageVO.getPageNumber() ) ;
            logNVP( "  Is LCT Ctx?", imageVO.getLctCtxImage() ) ;
            logNVP( "  Part No", imageVO.getPartNumber() ) ;
            logNVP( "  Img width", imageVO.getImgWidth() ) ;
            logNVP( "  Img height", imageVO.getImgHeight() ) ;
            logNVP( "  Img data", getCompressedImgData( imageVO ) ) ;
        }
    }
    
    private void logNVP( String name, Object value ) {
        append( StringUtils.rightPad( name, 15 ) + " = " + value ) ;
    }
    
    private String getCompressedImgData( QuestionImageVO imgVO ) {
        String imgData = imgVO.getImgData() ;
        return imgData.substring( 0, Math.min( 10, imgData.length() ) ) + "... " +
                (int)(( imgData.length() - 10 )/1024) + " KB" ;
    }
}
