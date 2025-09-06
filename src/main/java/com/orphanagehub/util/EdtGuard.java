
package com.orphanagehub.util;

import javax.swing.SwingUtilities;

public class EdtGuard {
    
    public static void runOnEdt(Runnable runnable) {
        if (SwingUtilities.isEventDispatchThread()) {
            runnable.run();
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }
}
