class Message {
    constructor(awesomeSms, id, date, protocol, thread, sender, body, attachments, pending = false) {
        this.awesomeSms = awesomeSms;
        this.id = id;
        this.date = date;
        this.protocol = protocol;
        this.thread = thread;
        this.sender = sender; // NULL IF YOU
        this.body = body;
        this.attachments = attachments;
        this.pending = pending;
    }

    getSenderContact() {
        return awesomeSms._contacts.get(this.sender);
    }
}
