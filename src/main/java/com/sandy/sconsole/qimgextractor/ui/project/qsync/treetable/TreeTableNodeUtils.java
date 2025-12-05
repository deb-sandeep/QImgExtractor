package com.sandy.sconsole.qimgextractor.ui.project.qsync.treetable;

import org.jdesktop.swingx.JXTreeTable;
import org.jdesktop.swingx.decorator.ComponentAdapter;

public final class TreeTableNodeUtils {
    private TreeTableNodeUtils() {}
    
    public static Object getNode( ComponentAdapter adapter ) {
        if (!(adapter.getComponent() instanceof JXTreeTable table)) return null;
        int viewRow = adapter.row;
        if (viewRow < 0) return null;
        var path = table.getPathForRow(viewRow);
        return path != null ? path.getLastPathComponent() : null;
    }
}
