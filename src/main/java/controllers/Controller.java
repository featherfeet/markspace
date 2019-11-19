package controllers;

import storage.PersistentStorage;

/** @file Controller.java
 * Superclass for all controllers (classes that hold the business logic for webpages).
 * @see controllers.Controller
 */

/**
 * Superclass for all controllers. Has the shared PersistentStorage object for all other controllers to inherit.
 */
public class Controller {
    /**
     * This PersistentStorage object is shared with all other controllers and used to permanently store data.
     */
    protected static PersistentStorage persistentStorage;

    /**
     * Create a new generic Controller object. For any controller, this MUST be called before using the controller in order to pass in the shared PersistentStorage object.
     * @param persistentStorage The shared PersistentStorage object used for storing permanent data.
     */
    public Controller(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }
}
