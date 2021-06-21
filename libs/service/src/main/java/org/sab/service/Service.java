package org.sab.service;

import org.sab.service.managers.ControlManager;

/**
 * Abstract class service which will be extended by the main class of each mini-app.
 * Uses the Command Pattern.
 * Contains the threading and command invoking functionality.
 */

public abstract class Service {

    private final ControlManager controlManager = new ControlManager(getAppUriName());

    public abstract String getAppUriName();

    public void start() {
        controlManager.start();
    }

    public ControlManager getControlManager() {
        return controlManager;
    }
}
