class Message {
    constructor(awesomeSms, id, date, protocol, thread, sender, body, attachments) {
        this.awesomeSms = awesomeSms;
        this.id = id;
        this.date = date;
        this.protocol = protocol;
        this.thread = thread;
        this.sender = sender; // NULL IF YOU
        this.body = body;
        this.attachments = attachments;
    }

    getSenderContact() {
        return awesomeSms._contacts.get(this.sender);
    }
}
