$(document).ready(function () {
    console.log("loaded");

    initUi();

    // TODO: Loading screen
    // TODO: Cache

    let awesomeSms = new AwesomeSms();
    awesomeSms.connectToServer("localhost:11150", (res) => {
        if (res.error) {
            console.error(res.error);
            return;
        }
        console.log("Connected to server! Refreshing messages...");

        awesomeSms.onMessageReceived = () => {
            awesomeSms.getThreads().forEach((thread) => {
                let uiThread = newThread(thread);
                if (uiThread)
                    uiThread.click(function () {
                        let threadId = parseInt($(this).attr("id").replace("thread-", ""));
                        $("#messages").empty();
                        for (let i = 0; i < awesomeSms.getThreads().get(threadId).messages.length; i++)
                            newMessage(awesomeSms.getThreads().get(threadId).messages[i]);
                    });
            });
        };

        awesomeSms.refreshMessages();
    });

    // TODO: Auto select first thread
    // TODO: Sort messages and threads by date!
});
