package com.eightbitforest.awesomesms.observer;

import com.eightbitforest.awesomesms.model.Contact;

/**
 * Interface for any class who wishes to get updates on updated or deleted contacts.
 *
 * @author Forrest Jones
 */
public interface IContactListener {
    void ContactUpdated(Contact contact);

    void ContactRemoved(int id);
}
