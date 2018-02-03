package com.eightbitforest.awesomesms.observer;

import com.eightbitforest.awesomesms.model.Contact;

/**
 * Created by osum4est on 1/29/18.
 */

public interface IContactListener {
    void ContactUpdated(Contact contact);

    void ContactRemoved(int id);
}
