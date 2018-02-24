class Thread {
    constructor(id, addresses, participants, messages) {
        this.id = id;
        // DOES NOT INCLUDE YOURSELF
        this.addresses = addresses;
        this.participants = participants;
        // Always sorted with 0 being oldest
        this.messages = messages;
    }

    /**
     * Insert into the thread, keeping the internal message storage sorted by date.
     * @param message The message to insert.
     */
    insertMessage(message) {
        pushSorted(this.messages, message, (a, b) => a.id - b.id);
    }

    getMostRecentMessage() {
        return this.messages[this.messages.length - 1];
    }
}