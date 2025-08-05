package com.sandy.sconsole.qimgextractor.ui.project.tree;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

public class TreeUtil {

    public static Object getUserObject( TreePath path ) {
        DefaultMutableTreeNode node = ( DefaultMutableTreeNode )path.getLastPathComponent() ;
        return node.getUserObject() ;
    }
}
