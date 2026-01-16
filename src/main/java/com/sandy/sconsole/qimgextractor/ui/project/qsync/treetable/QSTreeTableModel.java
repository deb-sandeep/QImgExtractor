package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import com.sandy.sconsole.qimgextractor.ui.project.model.ProjectModel;
import com.sandy.sconsole.qimgextractor.ui.project.model.Question;
import com.sandy.sconsole.qimgextractor.ui.project.model.QuestionImage;
import com.sandy.sconsole.qimgextractor.ui.project.qsync.QSyncUI;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.jdesktop.swingx.treetable.AbstractTreeTableModel;

import javax.swing.*;
import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable.QSyncTreeTable.*;

class TreeNode {
   
    @Getter final protected String name ;
    @Getter protected List<TreeNode> children = null ;
    
    protected TreeNode( String name ) {
        this.name = name ;
    }
    
    public String toString() {
        return name ;
    }
    
    public boolean hasChildren() {
        return children != null && !children.isEmpty() ;
    }
    
    public int getChildCount() {
        if( hasChildren() ) {
            return children.size() ;
        }
        return 0 ;
    }
    
    public int getIndexOfChild( TreeNode child ) {
        if( hasChildren() ) {
            for( int i=0; i<children.size(); i++ ) {
                if( child == children.get( i ) ) {
                    return i;
                }
            }
        }
        return -1 ;
    }
}

class QuestionImgNode extends TreeNode {
    
    final QuestionImage questionImage ;
    
    QuestionImgNode( QuestionImage qImg ) {
        super( qImg.getShortFileName() ) ;
        this.questionImage = qImg ;
    }
}

class QuestionNode extends TreeNode {
    
    final Question question ;
    
    QuestionNode( Question q ) {
        super( q.getQID().toString() ) ;
        super.children = new ArrayList<>() ;
        this.question = q ;
        for( QuestionImage qImg : q.getQImgList() ) {
            children.add( new QuestionImgNode( qImg ) ) ;
        }
    }
}

class SyllabusNode extends TreeNode {
    
    SyllabusNode( String name ) {
        super( name ) ;
        super.children = new ArrayList<>() ;
    }
    
    void addQuestionNode( QuestionNode qNode ) {
        children.add( qNode ) ;
    }
}

class RootNode extends TreeNode {
    
    final Map<String, SyllabusNode> syllabusMap = new HashMap<>() ;
    
    RootNode() {
        super( "Questions" ) ;
        super.children = new ArrayList<>() ;
    }
    
    void addQuestion( Question q ) {
        String syllabusName = "Unclassified" ;
        if( q.getTopic() != null ) {
            syllabusName = q.getTopic().getSyllabusName() ;
        }
        
        SyllabusNode syllabusNode = syllabusMap.computeIfAbsent( syllabusName, s -> {
            SyllabusNode sNode = new SyllabusNode( s ) ;
            children.add( sNode ) ;
            return sNode ;
        } ) ;
        syllabusNode.addQuestionNode( new QuestionNode( q ) ) ;
    }
    
    void clear() {
        syllabusMap.clear() ;
        children.clear() ;
    }
}

@Slf4j
public class QSTreeTableModel extends AbstractTreeTableModel {
    
    private static final String[] COLUMNS = {
            COL_NAME_LABEL,
            COL_TYPE_LABEL,
            COL_LAST_SYNC_DATE_LABEL,
            COL_LAST_UPDATE_DATE_LABEL,
            COL_ANSWER_LABEL,
            COL_SYNC_BTN_LABEL
    } ;

    private final QSyncUI qSyncUI ;
    private final ProjectModel projectModel ;
    private final RootNode rootNode = new RootNode() ;
    
    private boolean showUnsyncedOnly;
    
    public QSTreeTableModel( QSyncUI qSyncUI, ProjectModel projectModel, boolean showSyncPendingOnly ) {
        super() ;
        this.qSyncUI = qSyncUI ;
        this.projectModel = projectModel ;
        super.root = rootNode ;
        this.showUnsyncedOnly = showSyncPendingOnly ;
        refreshModel() ;
    }
    
    @Override
    public int getColumnCount() {
        return COLUMNS.length;
    }
    
    @Override
    public String getColumnName( int column ) {
        return COLUMNS[column];
    }
    
    @Override
    public boolean isCellEditable( Object node, int column ) {
        if( column == COL_SYNC_BTN ) {
            Question q = ((QuestionNode)node).question ;
            return q.isReadyForServerSync() ;
        }
        return false ;
    }
    
    @Override
    public Class<?> getColumnClass( int column ) {
        return switch (column) {
            case COL_NAME,
                 COL_TYPE -> String.class;
            case COL_ANSWER,
                 COL_SYNC_BTN -> Object.class;
            case COL_LAST_SYNC_DATE,
                 COL_LAST_UPDATE_DATE -> java.util.Date.class;
            
            default -> throw new IllegalStateException( "Unexpected value: " + column ) ;
        };
    }

    @Override
    public Object getChild( Object parent, int index ) {
        
        if( parent == null ) { return null ; }
        
        TreeNode node = ( TreeNode )parent ;
        if( node.hasChildren() ) {
            return node.children.get( index ) ;
        }
        return null ;
    }
    
    @Override
    public int getChildCount( Object parent ) {
        return ((TreeNode)parent).getChildCount() ;
    }
    
    @Override
    public int getIndexOfChild( Object parent, Object child ) {
        
        if( parent == null || child == null ) { return -1 ; }
        return (( TreeNode )parent).getIndexOfChild( (TreeNode)child ) ;
    }
    
    @Override
    public Object getValueAt( Object node, int column ) {
        if( node instanceof SyllabusNode ) {
            return getValueAt( (SyllabusNode)node, column ) ;
        }
        else if( node instanceof QuestionNode ) {
            return getValueAt( (QuestionNode)node, column ) ;
        }
        else if( node instanceof QuestionImgNode ) {
            return getValueAt( (QuestionImgNode)node, column ) ;
        }
        return null;
    }
    
    private Object getValueAt( SyllabusNode node, int column ) {
        if( column == COL_NAME ) {
            return node.getName() ;
        }
        return null ;
    }
    
    private Object getValueAt( QuestionNode qNode, int column ) {
        Question q = qNode.question ;
        return switch( column ) {
            case COL_NAME -> q.getQRef() ;
            case COL_TYPE -> q.getQID().getQuestionType() ;
            case COL_LAST_SYNC_DATE -> q.getSyncTime() ;
            case COL_LAST_UPDATE_DATE -> null ;
            case COL_ANSWER -> {
                if( q.getMmtAnswer() != null ) {
                    yield q.getMmtAnswer() ;
                }
                yield q.getAnswer() ;
            }
            case COL_SYNC_BTN -> "Sync Button" ; // This will be replaced by a button by the renderer
            default -> throw new IllegalStateException( "Unexpected value: " + column ) ;
        } ;
    }
    
    private Object getValueAt( QuestionImgNode qImgNode, int column ) {
        QuestionImage qImg = qImgNode.questionImage ;
        return switch( column ) {
            case COL_NAME -> qImg.getShortFileName() ;
            case COL_TYPE -> qImg.getShortFileName().substring( qImg.getShortFileName().lastIndexOf( '.' ) ) ;
            case COL_LAST_UPDATE_DATE -> qImg.getLastModified() ;
            case COL_LAST_SYNC_DATE,
                 COL_ANSWER,
                 COL_SYNC_BTN -> null ;
            default -> throw new IllegalStateException( "Unexpected value: " + column ) ;
        } ;
    }
    
    public void refreshModel() {
        rootNode.clear() ;
        for( Question q : projectModel.getQuestionRepo().getQuestionList() ) {
            if( showUnsyncedOnly ) {
                if( !q.isSynced() || q.isModifiedAfterSync() ) {
                    rootNode.addQuestion( q ) ;
                }
            }
            else {
                rootNode.addQuestion( q ) ;
            }
        }
        super.modelSupport.fireTreeStructureChanged( new TreePath( super.getRoot() ) ) ;
    }
    
    public void toggleShowOnlyUnsynced() {
        this.showUnsyncedOnly = !this.showUnsyncedOnly;
        refreshModel() ;
    }

    public void syncAllSyncPendingQuestions() {
        for( TreeNode syllabusNode : rootNode.getChildren() ) {
            for( TreeNode questionNode : syllabusNode.getChildren() ) {
                Question q = ((QuestionNode)questionNode).question ;
                boolean syncQuestion = false ;
                if( q.isReadyForServerSync() ) {
                    if( q.isSynced() ) {
                        if( q.isModifiedAfterSync() ) {
                            syncQuestion = true ;
                        }
                    }
                    else {
                        syncQuestion = true ;
                    }
                }
                if( syncQuestion ) {
                    qSyncUI.syncQuestion( q ) ;
                    SwingUtilities.invokeLater( () -> super.modelSupport.firePathChanged( new TreePath( questionNode ) ) ) ;
                }
            }
        }
    }
}
