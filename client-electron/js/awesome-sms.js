$(document).ready(function () {
    console.log("loaded");

    init();

    // Temporary testing code
    for (let i = 0; i < 15; i++) {
        let participants = [
            new Contact(0, "John Doe", [
                new ContactPhone("1234567", 0)
            ], null, randomColor())
        ];

        let thread = new Thread(i, participants, []);

        for (let j = 0; j < 100; j++) {
            let message = new Message(i*j, 1243214321, 0, thread,
                Math.random() > .5 ? null : participants[0], "This is a message", []);
            newMessage(message);

            thread.messages.push(message);
        }

        newThread(thread);
    }
});
