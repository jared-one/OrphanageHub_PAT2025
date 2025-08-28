/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */
package com.orphanagehub.util;

/* Copyright (C) 2025 Jared Wisdom - All Rights Reserved */

import javax.swing.JComponent;
import javax.swing.RepaintManager;
import javax.swing.SwingUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class EdtGuard {
    private static final Logger logger = LoggerFactory.getLogger(EdtGuard.class);

    public static void install() {
        String env = System.getProperty("app.env", "");
        if (!"dev".equalsIgnoreCase(env)) return;

        RepaintManager.setCurrentManager(
                new RepaintManager() {
                    @Override
                    public void addDirtyRegion(JComponent c, int x, int y, int w, int h) {
                        if (!SwingUtilities.isEventDispatchThread()) {
                            logger.warn(
                                    "EDT VIOLATION: UI update outside Event Dispatch Thread!",
                                    new Exception("EDT violation stack trace"));
                        }
                        super.addDirtyRegion(c, x, y, w, h);
                    }
                });
        logger.info("EDT Guard has been installed for this development session.");
    }
}
