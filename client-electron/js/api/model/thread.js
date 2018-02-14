class Thread {
    constructor(id, participants, messages) {
        this.id = id;
        // DOES NOT INCLUDE YOURSELF
        this.participants = participants;
        // Always sorted with 0 being most recent
        this.messages = messages;
    }
}