$.holdReady(true);

$.when(
    $.getScript("js/api/model/contact.js"),
    $.getScript("js/api/model/message.js"),
    $.getScript("js/api/model/thread.js"),
    $.getScript("js/api/util/util.js"),
    $.getScript("node_modules/libphonenumber-js/bundle/libphonenumber-js.min.js"),
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
        this.onContactReceived = undefined;
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
            this._connected = true;
            callback({})
        };

        this._socket.onmessage = (e) => {
            let data = JSON.parse(e.data);
            switch (data[0]) {
                case "new_messages":
                    this._handleNewMessages(data[1]);
                    break;
                case "new_contacts":
                    this._handleNewContacts(data[1]);
                    break;
            }
        };
    }

    refreshMessages() {
        this._socket.send(JSON.stringify([
            "get_new_messages",
            {
                "lastDateReceived": 1518813830,
                "amount": 100
            }
        ]));
        // this._socket.send(JSON.stringify([
        //     "get_threads",
        //     {
        //         "amount": 100
        //     }
        // ]));
    }

    refreshContacts() {
        this._socket.send(JSON.stringify([
            "get_contacts", {}
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
                if (!this._contacts.has(address["address"])) {
                    this._contacts.set(address["address"], new Contact(
                        this,
                        null,
                        null,
                        [new ContactPhone(this, address["address"], address["type"])],
                        null,
                        randomColor()
                    ))
                }

                if (address["type"] === _messageAddressTypeFrom)
                    sender = address["address"];

                participants.push(address["address"]);
            });

            // Add thread if it doesn't exist
            if (!this._threads.has(message["threadId"])) {
                this._threads.set(message["threadId"], new Thread(
                    this,
                    message["threadId"],
                    participants,
                    []
                ));

                newThreads.push(this._threads.get(message["threadId"]));
            }

            // Add messages
            this._messages.set(message["id"], new Message(
                this,
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

    _handleNewContacts(contacts) {
        contacts.forEach((contact) => {
            let phones = [];

            // Make contact
            let newContact = new Contact(
                this,
                contact["id"],
                contact["name"],
                phones,
                contact["thumbnail"],
                randomColor()
            );

            // Get contact phones
            contact["phones"].forEach((phone) => {
                phones.push(new ContactPhone(this, phone["number"], phone["type"]));

                // Insert contact
                this._contacts.set(phone["number"], newContact)
            });
        });

        if (this.onContactReceived !== undefined)
            this.onContactReceived()
    }


    sendMessage(threadId, body) {
        this._socket.send(JSON.stringify([
            "send_message",
            {
                "threadId": threadId,
                "addresses": this.getThread(threadId).participants,
                "body": body
            }
        ]));
    }
}