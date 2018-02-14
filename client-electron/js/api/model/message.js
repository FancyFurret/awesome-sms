class Message {
    constructor(id, date, protocol, thread, sender, body, attachments) {
        this.id = id;
        this.date = date;
        this.protocol = protocol;
        this.thread = thread;
        this.sender = sender; // NULL IF YOU
        this.body = body;
        this.attachents = attachments;
    }
}
