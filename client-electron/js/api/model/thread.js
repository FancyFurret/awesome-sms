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

    getAbbreviation() {
        if (this.participants.length > 1)
            return this.participants.length;

        return this.getParticipantContact(0).getAbbreviation();
    }

    getThumbnail() {
        if (this.participants.length > 1)
            return undefined;

        return this.getParticipantContact(0).thumbnail;
    }

    getNames() {
        let name = "";
        for (let i = 0; i < this.participants.length; i++) {
            name += this.getParticipantContact(i).getDisplayName() + ", ";
        }
        name = name.slice(0, -2);
        return name;
    }
}