package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.img;

import com.sandy.sconsole.qimgextractor.QImgExtractor;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.extract.AnswerImportPlan;
import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.extract.ExtractedAnswers;
import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.util.AppConfig;
import com.sandy.sconsole.qimgextractor.util.AppUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.configurationprocessor.json.JSONArray;
import org.springframework.boot.configurationprocessor.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

// Runs a prompt (typically the /extract-answers skill) in an ephemeral,
// non-interactive Claude Code session and streams its reply as it arrives.
// If a page name is given, the prompt is submitted as soon as the dialog
// opens. Ok imports the answers in the reply's <extracted-answers> block into the
// project, after showing a preview of the changes. If the reply has a valid
// answers block, the preview is shown as soon as the session finishes. The
// preview also offers to delete the questions the answer key has no answer for.
@Slf4j
public class AnswerExtractorDialog extends JDialog {

    // The extract-answers skill only locates the page image with find and
    // reads it. Print mode can't ask for permissions, so these are pre-approved.
    private static final String ALLOWED_TOOLS = "Read,Bash(find:*)" ;

    private final AppConfig appConfig ;
    private final AnswerMapperUI answerMapperUI ;
    private final ProjectModel projectModel ;

    private final JTextField promptTF = new JTextField() ;
    private final JButton submitBtn = new JButton( "Submit" ) ;
    private final JButton okBtn = new JButton( "Ok" ) ;
    private final JButton cancelBtn = new JButton( "Cancel" ) ;
    private final JTextArea outputTA = new JTextArea() ;
    private final JLabel busyLabel = new JLabel( "Claude is working ..." ) ;

    private SwingWorker<Void, String> worker = null ;
    private Process process = null ;

    // Set while the confirmed answers are being applied to the project. The
    // dialog can't be dismissed during this time.
    private boolean importing = false ;

    // The complete reply of the last finished session.
    private volatile String replyText = null ;

    public AnswerExtractorDialog( Window owner, AnswerMapperUI answerMapperUI, String pageName ) {
        super( owner, "Extract Answers", ModalityType.APPLICATION_MODAL ) ;
        this.appConfig = QImgExtractor.getBean( AppConfig.class ) ;
        this.answerMapperUI = answerMapperUI ;
        this.projectModel = answerMapperUI.getProjectModel() ;

        promptTF.setText( "/extract-answers " + ( pageName != null ? pageName : "" ) ) ;
        promptTF.addActionListener( e -> submit() ) ;
        submitBtn.addActionListener( e -> submit() ) ;

        outputTA.setEditable( false ) ;
        outputTA.setLineWrap( true ) ;
        outputTA.setWrapStyleWord( true ) ;
        outputTA.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 13 ) ) ;

        busyLabel.setIcon( new ImageIcon( getClass().getResource( "/icons/busy.gif" ) ) ) ;
        busyLabel.setVisible( false ) ;

        setLayout( new BorderLayout() ) ;
        add( createPromptPanel(), BorderLayout.NORTH ) ;
        add( new JScrollPane( outputTA ), BorderLayout.CENTER ) ;
        add( createButtonPanel(), BorderLayout.SOUTH ) ;

        getRootPane().setDefaultButton( submitBtn ) ;
        getRootPane().registerKeyboardAction( e -> dismiss(),
                KeyStroke.getKeyStroke( KeyEvent.VK_ESCAPE, 0 ),
                JComponent.WHEN_IN_FOCUSED_WINDOW ) ;

        setDefaultCloseOperation( DO_NOTHING_ON_CLOSE ) ;
        addWindowListener( new java.awt.event.WindowAdapter() {
            @Override
            public void windowOpened( java.awt.event.WindowEvent e ) {
                if( pageName != null ) {
                    submit() ;
                }
            }

            @Override
            public void windowClosing( java.awt.event.WindowEvent e ) {
                dismiss() ;
            }
        } ) ;

        setSize( 800, 500 ) ;
        setLocationRelativeTo( owner ) ;
    }

    private JPanel createPromptPanel() {
        JPanel panel = new JPanel( new BorderLayout( 5, 0 ) ) ;
        panel.setBorder( BorderFactory.createEmptyBorder( 5, 5, 5, 5 ) ) ;
        panel.add( promptTF, BorderLayout.CENTER ) ;
        panel.add( submitBtn, BorderLayout.EAST ) ;
        return panel ;
    }

    private JPanel createButtonPanel() {
        okBtn.addActionListener( e -> importAnswers( false ) ) ;
        cancelBtn.addActionListener( e -> dismiss() ) ;

        JPanel btnPanel = new JPanel( new FlowLayout( FlowLayout.RIGHT ) ) ;
        btnPanel.add( cancelBtn ) ;
        btnPanel.add( okBtn ) ;

        JPanel panel = new JPanel( new BorderLayout() ) ;
        panel.setBorder( BorderFactory.createEmptyBorder( 0, 8, 0, 0 ) ) ;
        panel.add( busyLabel, BorderLayout.WEST ) ;
        panel.add( btnPanel, BorderLayout.EAST ) ;
        return panel ;
    }

    // If auto is set, the import was triggered by the session finishing and a
    // reply without a valid answers block is left in the dialog for review.
    private void importAnswers( boolean auto ) {

        // The dialog may have been dismissed while the session was finishing.
        if( auto && ( replyText == null || !isDisplayable() ) ) {
            return ;
        }
        if( replyText == null ) {
            AppUtil.showErrorMsg( this, "There is no reply to import. Submit the prompt first." ) ;
            return ;
        }

        AnswerImportPlan plan ;
        try {
            ExtractedAnswers extracted = ExtractedAnswers.parse( replyText ) ;
            plan = AnswerImportPlan.build( extracted, projectModel.getQuestionRepo().getQuestionList() ) ;
        }
        catch( IllegalArgumentException e ) {
            log.error( "Could not parse the extracted answers.", e ) ;
            if( !auto ) {
                AppUtil.showErrorMsg( this, e.getMessage() ) ;
            }
            return ;
        }

        List<Question> unanswered = plan.getUnansweredQuestions(
                projectModel.getQuestionRepo().getQuestionList() ) ;

        if( !plan.hasChanges() && unanswered.isEmpty() ) {
            JOptionPane.showMessageDialog( this, createSummaryPane( "Nothing to import.\n\n" + plan.toSummary() ),
                    "Import Answers", JOptionPane.INFORMATION_MESSAGE ) ;
            return ;
        }

        String summary = plan.toSummary() ;
        JCheckBox deleteCB = null ;
        Object message = createSummaryPane( summary ) ;
        if( !unanswered.isEmpty() ) {
            summary += "\nNOT IN ANSWER KEY\n  " + toQIdList( unanswered ) + "\n" ;
            deleteCB = new JCheckBox( "Delete the " + unanswered.size() +
                                      " question(s) not in the answer key (images are deleted permanently)", true ) ;
            message = new Object[]{ createSummaryPane( summary ), deleteCB } ;
        }

        int choice = JOptionPane.showConfirmDialog( this, message,
                "Import Answers - Confirm Changes", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE ) ;
        if( choice != JOptionPane.OK_OPTION ) {
            return ;
        }

        List<String> toDelete = ( deleteCB != null && deleteCB.isSelected() ) ?
                unanswered.stream().map( q -> q.getQID().toString() ).toList() :
                List.of() ;
        applyImportPlan( plan, toDelete ) ;
    }

    private static String toQIdList( List<Question> questions ) {
        return questions.stream()
                        .map( q -> q.getQID().toString() )
                        .collect( Collectors.joining( ", " ) ) ;
    }

    // Deletes the questions with the given QIDs. The questions are looked up
    // afresh as applying the import plan rebuilds the question list. Returns
    // descriptions of the questions that could not be deleted.
    private List<String> deleteQuestions( List<String> qIds ) {
        List<String> failures = new ArrayList<>() ;
        for( String qId : qIds ) {
            Question q = projectModel.getQuestionRepo().getQuestionList().stream()
                    .filter( x -> x.getQID().toString().equals( qId ) )
                    .findFirst().orElse( null ) ;
            if( q == null || q.getAnswer() != null ) {
                continue ;
            }
            try {
                projectModel.deleteQuestion( q ) ;
            }
            catch( RuntimeException e ) {
                log.error( "Failed to delete question {}", qId, e ) ;
                failures.add( qId + " : not deleted. " + e.getMessage() ) ;
            }
        }
        return failures ;
    }

    // Applying the plan renames question images and saves the question repo,
    // so it is done in the background while the busy label is shown.
    private void applyImportPlan( AnswerImportPlan plan, List<String> qIdsToDelete ) {

        importing = true ;
        setControlsEnabled( false ) ;
        busyLabel.setText( "Importing answers ..." ) ;
        busyLabel.setVisible( true ) ;

        new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() {
                return plan.apply( projectModel ) ;
            }

            @Override
            protected void done() {
                importing = false ;
                busyLabel.setVisible( false ) ;

                List<String> failures ;
                try {
                    failures = new ArrayList<>( get() ) ;
                    // Deletion notifies the UI listeners, so it is done on the EDT.
                    failures.addAll( deleteQuestions( qIdsToDelete ) ) ;
                    answerMapperUI.answersImported() ;
                }
                catch( Exception e ) {
                    log.error( "Could not import the extracted answers.", e ) ;
                    answerMapperUI.answersImported() ;
                    AppUtil.showErrorMsg( AnswerExtractorDialog.this,
                            "Could not import the extracted answers.\n" + e.getMessage() ) ;
                    setControlsEnabled( true ) ;
                    return ;
                }

                if( !failures.isEmpty() ) {
                    JOptionPane.showMessageDialog( AnswerExtractorDialog.this,
                            createSummaryPane( "These changes could not be applied:\n\n" + String.join( "\n", failures ) ),
                            "Import Answers", JOptionPane.WARNING_MESSAGE ) ;
                }
                QImgExtractor.logStatusMsg( "Imported extracted answers." +
                        ( qIdsToDelete.isEmpty() ? "" : " Deleted " + qIdsToDelete.size() + " unanswered question(s)." ) ) ;
                dismiss() ;
                answerMapperUI.answerImportCompleted() ;
            }
        }.execute() ;
    }

    private void setControlsEnabled( boolean enabled ) {
        promptTF.setEnabled( enabled ) ;
        submitBtn.setEnabled( enabled ) ;
        okBtn.setEnabled( enabled ) ;
        cancelBtn.setEnabled( enabled ) ;
    }

    private JScrollPane createSummaryPane( String text ) {
        JTextArea ta = new JTextArea( text ) ;
        ta.setEditable( false ) ;
        ta.setFont( new Font( Font.MONOSPACED, Font.PLAIN, 12 ) ) ;
        ta.setCaretPosition( 0 ) ;
        JScrollPane scrollPane = new JScrollPane( ta ) ;
        scrollPane.setPreferredSize( new Dimension( 650, 400 ) ) ;
        return scrollPane ;
    }

    private void submit() {
        String prompt = promptTF.getText().trim() ;
        if( prompt.isEmpty() || worker != null ) {
            return ;
        }

        submitBtn.setEnabled( false ) ;
        promptTF.setEnabled( false ) ;
        okBtn.setEnabled( false ) ;
        busyLabel.setText( "Claude is working ..." ) ;
        busyLabel.setVisible( true ) ;
        replyText = null ;
        outputTA.setText( "" ) ;

        worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() throws Exception {
                runClaude( prompt, text -> publish( text ) ) ;
                return null ;
            }

            @Override
            protected void process( List<String> chunks ) {
                for( String chunk : chunks ) {
                    outputTA.append( chunk ) ;
                }
            }

            @Override
            protected void done() {
                try {
                    get() ;
                }
                catch( Exception e ) {
                    log.error( "Claude session failed.", e ) ;
                    outputTA.append( "\n\nError : " + e.getMessage() ) ;
                }
                busyLabel.setVisible( false ) ;
                outputTA.requestFocusInWindow() ;
                submitBtn.setEnabled( true ) ;
                promptTF.setEnabled( true ) ;
                okBtn.setEnabled( true ) ;
                worker = null ;
                importAnswers( true ) ;
            }
        } ;
        worker.execute() ;
    }

    // Reads the stream-json output line by line. Reply text is passed on as it
    // is generated and each tool call is shown as a one-line note, so that
    // long stretches of tool use don't look like a hang.
    private void runClaude( String prompt, Consumer<String> out ) throws Exception {

        ProcessBuilder pb = new ProcessBuilder(
                appConfig.getClaudeExecutable(),
                "-p", prompt,
                "--output-format", "stream-json",
                "--verbose",
                "--include-partial-messages",
                "--allowedTools", ALLOWED_TOOLS )
                .directory( appConfig.getAnswerExtractorWorkDir() )
                .redirectInput( ProcessBuilder.Redirect.from( new File( "/dev/null" ) ) ) ;

        log.debug( "Launching claude : {}", pb.command() ) ;
        process = pb.start() ;

        // Drain stderr in parallel so that a full stderr pipe can't block the
        // process while stdout is being read.
        CompletableFuture<String> stderr = CompletableFuture.supplyAsync( () -> {
            try {
                return new String( process.getErrorStream().readAllBytes(), StandardCharsets.UTF_8 ) ;
            }
            catch( Exception e ) {
                return "" ;
            }
        } ) ;

        boolean resultReceived = false ;
        boolean lastWasText = false ;
        try( BufferedReader reader = new BufferedReader(
                new InputStreamReader( process.getInputStream(), StandardCharsets.UTF_8 ) ) ) {

            String line ;
            while( ( line = reader.readLine() ) != null ) {
                JSONObject event ;
                try {
                    event = new JSONObject( line ) ;
                }
                catch( Exception e ) {
                    log.debug( "Ignoring non-JSON output : {}", line ) ;
                    continue ;
                }

                switch( event.optString( "type" ) ) {
                    case "stream_event" -> {
                        JSONObject delta = event.getJSONObject( "event" ).optJSONObject( "delta" ) ;
                        if( delta != null && "text_delta".equals( delta.optString( "type" ) ) ) {
                            out.accept( delta.getString( "text" ) ) ;
                            lastWasText = true ;
                        }
                    }
                    case "assistant" -> {
                        JSONArray content = event.getJSONObject( "message" ).getJSONArray( "content" ) ;
                        for( int i = 0; i < content.length(); i++ ) {
                            JSONObject block = content.getJSONObject( i ) ;
                            if( "tool_use".equals( block.optString( "type" ) ) ) {
                                out.accept( ( lastWasText ? "\n\n" : "" ) +
                                            "> " + describeToolUse( block ) + "\n\n" ) ;
                                lastWasText = false ;
                            }
                        }
                    }
                    case "result" -> {
                        resultReceived = true ;
                        replyText = event.optString( "result", null ) ;
                        if( event.optBoolean( "is_error" ) ) {
                            out.accept( "\n\nClaude reported an error : " +
                                        event.optString( "result", event.optString( "subtype" ) ) ) ;
                        }
                    }
                    default -> {}
                }
            }
        }

        int exitCode = process.waitFor() ;
        if( !resultReceived && exitCode != 0 ) {
            out.accept( "\n\nclaude exited with code " + exitCode + "\n" + stderr.get() ) ;
        }
    }

    private String describeToolUse( JSONObject toolUse ) {
        String name = toolUse.optString( "name" ) ;
        JSONObject input = toolUse.optJSONObject( "input" ) ;
        String detail = "" ;
        if( input != null ) {
            detail = input.optString( "command", input.optString( "file_path", "" ) ) ;
        }
        return detail.isEmpty() ? name : name + " : " + detail ;
    }

    private void dismiss() {
        if( importing ) {
            return ;
        }
        if( process != null && process.isAlive() ) {
            process.descendants().forEach( ProcessHandle::destroy ) ;
            process.destroy() ;
        }
        dispose() ;
    }
}
