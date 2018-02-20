$.holdReady(true);

$.when(
    $.getScript("js/api/model/contact.js"),
    $.getScript("js/api/model/message.js"),
    $.getScript("js/api/model/thread.js"),
    $.getScript("js/api/util/util.js")
).then(function () {
    console.log("AwesomeSMS api v0.1.0");
    $.holdReady(false);
});

const _messageAddressTypeTo = 0;
const _messageAddressTypeFrom = 1;
const _messageAddressTypeCC = 2;

// TODO: Move to private helper classes?
class AwesomeSms {

    constructor() {

        this._connected = false;
        this._messages = new Map();
        this._threads = new Map();
        this._contacts = new Map();

        this.onMessageReceived = undefined;
    }

    connectToServer(ip, callback) {
        this._socket = new WebSocket("ws://" + ip + "/ws");
        this._socket.onerror = () => {
            callback({error: "Could not connect to AwesomeSMS server at " + ip + "."});
        };

        this._socket.onclose = (e) => {
            console.error("Connection closed!")
        };

        this._socket.onopen = () => {
            this._connnected = true;
            callback({})
        };

        this._socket.onmessage = (e) => {
            let data = JSON.parse(e.data);
            switch (data[0]) {
                case "new_messages":
                    this._handleNewMessages(data[1]);
            }
        };
    }

    refreshMessages() {
        this._socket.send(JSON.stringify([
            "get_threads",
            {
                "amount": 100
            }
        ]));
    }

    getMessage(id) {
        return this._messages.get(id)
    }

    getThreadsByDate() {
        return [...this._threads.values()].sort((a, b) =>
            a.getMostRecentMessage().date - b.getMostRecentMessage().date);
    }

    getThread(id) {
        return this._threads.get(id)
    }

    getContact(id) {
        return this._contacts.get(id)
    }

    _handleNewMessages(messages) {
        let newMessages = [];
        let newThreads = [];

        // TODO: Cache parts of json message/address/etc. so we don't have to keep getting them
        messages.forEach((message) => {
            // Go through address, find sender, and add others to contacts if they don't exist
            let sender = undefined;
            let participants = [];
            message["addresses"].forEach((address) => {
                // TODO: Dont make a new phone, point to existing one
                // TODO: Other stuff that i need to think about
                if (!this._contacts.has(address["address"])) {
                    this._contacts.set(address["address"], new Contact(
                        null,
                        null,
                        [new ContactPhone(address["address"], address["type"])],
                        null,
                        randomColor()
                    ))
                }

                if (address["type"] === _messageAddressTypeFrom)
                    sender = this._contacts.get(address["address"]);

                participants.push(this._contacts.get(address["address"]));
            });

            // Add thread if it doesn't exist
            if (!this._threads.has(message["threadId"])) {
                this._threads.set(message["threadId"], new Thread(
                    message["threadId"],
                    participants,
                    []
                ));

                newThreads.push(this._threads.get(message["threadId"]));
            }

            // Add messages
            this._messages.set(message["id"], new Message(
                message["id"],
                message["date"],
                message["protocol"],
                this._threads.get(message["threadId"]),
                sender,
                message["body"],
                message["attachments"]
            ));

            this._threads.get(message["threadId"]).insertMessage(
                this._messages.get(message["id"])
            );

            newMessages.push(this._messages.get(message["id"]))
        });

        if (this.onMessageReceived !== undefined)
            this.onMessageReceived(newMessages, newThreads)
    }

}