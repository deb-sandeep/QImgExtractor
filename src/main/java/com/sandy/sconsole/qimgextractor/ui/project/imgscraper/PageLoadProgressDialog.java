package com.sandy.sconsole.qimgextractor.ui.project.imgscraper;

import javax.swing.*;
import java.awt.*;

/**
 * Modal dialog shown while the page images of a project are being loaded.
 * It blocks interaction with the main window until loading completes and
 * can't be closed by the user. All methods must be called on the EDT.
 */
class PageLoadProgressDialog extends JDialog {

    private final JProgressBar progressBar ;
    private final JLabel fileNameLabel ;
    private final int numPages ;

    private int numLoaded = 0 ;
    private boolean finished = false ;

    PageLoadProgressDialog( Window owner, String projectName, int numPages ) {
        super( owner, "Opening " + projectName, ModalityType.APPLICATION_MODAL ) ;
        this.numPages = numPages ;

        progressBar = new JProgressBar( 0, Math.max( numPages, 1 ) ) ;
        progressBar.setStringPainted( true ) ;
        progressBar.setFont( progressBar.getFont().deriveFont( Font.BOLD, 14f ) ) ;
        progressBar.setPreferredSize( new Dimension( 400, 26 ) ) ;

        fileNameLabel = new JLabel( " " ) ;
        fileNameLabel.setForeground( Color.DARK_GRAY ) ;

        JPanel content = new JPanel( new BorderLayout( 0, 8 ) ) ;
        content.setBorder( BorderFactory.createEmptyBorder( 12, 16, 12, 16 ) ) ;
        content.add( new JLabel( "Loading page images..." ), BorderLayout.NORTH ) ;
        content.add( progressBar, BorderLayout.CENTER ) ;
        content.add( fileNameLabel, BorderLayout.SOUTH ) ;

        setContentPane( content ) ;
        setDefaultCloseOperation( WindowConstants.DO_NOTHING_ON_CLOSE ) ;
        setResizable( false ) ;
        updateProgress() ;
        pack() ;
        setLocationRelativeTo( owner ) ;
    }

    // Shows the dialog unless loading has already finished. Being modal,
    // this call returns only once the dialog is closed.
    void showDialog() {
        if( !finished ) {
            setVisible( true ) ;
        }
    }

    void pageLoaded( String fileName ) {
        numLoaded++ ;
        fileNameLabel.setText( fileName ) ;
        updateProgress() ;
    }

    void finish() {
        finished = true ;
        dispose() ;
    }

    private void updateProgress() {
        progressBar.setValue( numLoaded ) ;
        progressBar.setString( numLoaded + " / " + numPages ) ;
    }
}
