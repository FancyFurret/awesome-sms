package com.eightbitforest.awesomesms.observer;

import com.eightbitforest.awesomesms.model.TextMessage;

/**
 * Interface for any class who wishes to get updates on newly sent or received text messages.
 *
 * @author Forrest Jones
 */
public interface ITextListener {
    void TextSent(TextMessage text);

    void TextReceived(TextMessage text);
}
