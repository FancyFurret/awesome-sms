class Thread {
    constructor(id, participants, messages) {
        this.id = id;
        // DOES NOT INCLUDE YOURSELF
        this.participants = participants;
        // Always sorted with 0 being most recent
        this.messages = messages;
    }

    /**
     // * Insert into the thread, keeping the internal message storage sorted by date.
     // * @param message The message to insert.
     // */
    insertMessage(message) {
        this.messages.splice(
            binaryIndexOf(this.messages, message, (a, b) => b.date - a.date) + 1, 0, message);
    }

    getMostRecentMessage() {
        return this.messages[0];
    }
}