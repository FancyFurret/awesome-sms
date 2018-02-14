$(document).ready(function () {
    console.log("loaded");

    init();

    let threads = [];

    // Temporary testing code
    for (let i = 0; i < 15; i++) {
        let participants = [
            new Contact(0, "John Doe", [
                new ContactPhone("1234567", 0)
            ], null, randomColor())
        ];

        let thread = new Thread(i, participants, []);
        threads[i] = thread;

        for (let j = 0; j < 100; j++) {
            let message = new Message(i*j, 1243214321, 0, thread,
                Math.random() > .5 ? null : participants[0], "This is a message", []);
            thread.messages.push(message);
        }

        newThread(thread);
    }

    for (let i = 0; i < threads[0].messages.length; i++)
        newMessage(threads[0].messages[i]);

    $(".thread").click(function() {
        let threadId = $(this).attr("id").replace("thread-", "");
        $("#messages").empty();
        for (let i = 0; i < threads[threadId].messages.length; i++)
            newMessage(threads[threadId].messages[i]);
    });
});
