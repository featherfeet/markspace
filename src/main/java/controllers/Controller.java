/*
 * Copyright 2020 Oliver Trevor and Suchin Ravi.
 *
 * This file is part of MarkSpace.
 *
 * MarkSpace is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MarkSpace is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MarkSpace.  If not, see <https://www.gnu.org/licenses/>.
 */

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
