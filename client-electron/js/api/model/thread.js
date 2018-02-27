class Thread {
    constructor(awesomeSms, id, participants, messages) {
        this.awesomeSms = awesomeSms;
        this.id = id;
        // DOES NOT INCLUDE YOURSELF
        this.participants = participants;
        // Always sorted with 0 being oldest
        this.messages = messages;
    }

    /**
     * Insert into the thread, keeping the internal message storage sorted by date.
     * @param message The message to insert.
     */
    insertMessage(message) {
        pushSorted(this.messages, message, (a, b) => a.date - b.date);
    }

    removeMessage(message) {
        this.messages.splice(this.messages.indexOf(message), 1);
    }

    getMostRecentMessage() {
        return this.messages[this.messages.length - 1];
    }

    getParticipantContact(i) {
        return awesomeSms._contacts.get(this.participants[i]);
    }
}