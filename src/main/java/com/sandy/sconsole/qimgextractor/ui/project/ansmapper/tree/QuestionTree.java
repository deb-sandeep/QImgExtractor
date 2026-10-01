package com.sandy.sconsole.qimgextractor.ui.project.ansmapper.tree;

import com.sandy.sconsole.qimgextractor.ui.project.ansmapper.AnswerMapperUI;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import lombok.Getter;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static javax.swing.tree.TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION;

public class QuestionTree extends JTree {

    public static final Font TREE_FONT = new Font( "Helvetica", Font.PLAIN, 12 ) ;

    @Getter
    private final AnswerMapperUI parentPanel;

    private final QuestionTreeModel treeModel ;
    private final JPopupMenu popupMenu ;

    public QuestionTree( AnswerMapperUI parentPanel ) {
        this.parentPanel = parentPanel;
        this.treeModel = new QuestionTreeModel( parentPanel.getProjectModel(), this ) ;

        super.setRootVisible( true ) ;
        super.setFont( TREE_FONT ) ;
        super.getSelectionModel().setSelectionMode( DISCONTIGUOUS_TREE_SELECTION ) ;
        super.setRowHeight( 25 ) ;
        super.setEditable( true ) ;
        super.setCellRenderer( new QuestionTreeCellRenderer() ) ;

        super.setModel( treeModel ) ;

        this.popupMenu = createPopupMenu() ;
        addTreeListeners() ;

        SwingUtilities.invokeLater( () -> setExpanded( false ) ) ;
    }

    private JPopupMenu createPopupMenu() {
        JPopupMenu popup = new JPopupMenu() ;
        JMenuItem deleteQuestions = new JMenuItem( "Delete Question(s)" ) ;
        deleteQuestions.addActionListener( e -> parentPanel.deleteQuestions( getSelectedQuestions() ) ) ;
        popup.add( deleteQuestions ) ;
        return popup ;
    }

    private void addTreeListeners() {
        super.addMouseListener( new MouseAdapter() {
            public void mousePressed( MouseEvent e )  { handlePopupTrigger( e ) ; }
            public void mouseReleased( MouseEvent e ) { handlePopupTrigger( e ) ; }
        } ) ;
    }

    private void handlePopupTrigger( MouseEvent e ) {
        if( !e.isPopupTrigger() ) {
            return ;
        }

        TreePath path = getPathForLocation( e.getX(), e.getY() ) ;
        if( path == null ) {
            return ;
        }

        // Right-click on an unselected node replaces the selection, while
        // right-click on a selected node retains the multi-selection.
        if( !isPathSelected( path ) ) {
            setSelectionPath( path ) ;
        }

        if( !getSelectedQuestions().isEmpty() ) {
            popupMenu.show( e.getComponent(), e.getX(), e.getY() ) ;
        }
    }

    // Question image nodes resolve to their parent question.
    private List<Question> getSelectedQuestions() {
        Set<Question> questions = new LinkedHashSet<>() ;
        TreePath[] paths = getSelectionPaths() ;
        if( paths != null ) {
            for( TreePath path : paths ) {
                DefaultMutableTreeNode node = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
                if( node.getUserObject() instanceof Question q ) {
                    questions.add( q ) ;
                }
                else if( node.getParent() instanceof DefaultMutableTreeNode parent &&
                         parent.getUserObject() instanceof Question q ) {
                    questions.add( q ) ;
                }
            }
        }
        return new ArrayList<>( questions ) ;
    }

    @Override
    public boolean isPathEditable( TreePath path ) {
        return false ;
    }

    public void setExpanded( boolean expanded ) {
        DefaultMutableTreeNode root = ( DefaultMutableTreeNode )getModel().getRoot() ;
        expandPath( new TreePath( root.getPath() ) ) ;

        Enumeration<TreeNode> pageNodes = root.children() ;
        while( pageNodes.hasMoreElements() ) {
            DefaultMutableTreeNode pageNode = (DefaultMutableTreeNode)pageNodes.nextElement() ;
            if( pageNode.getUserObject() instanceof Question ) {
                if( expanded ) {
                    super.expandPath( new TreePath( pageNode.getPath() ) ) ;
                }
                else {
                    super.collapsePath( new TreePath( pageNode.getPath() ) ) ;
                }
            }
        }
    }

    public void refreshTree() {
        this.treeModel.buildTree() ;
    }
}
