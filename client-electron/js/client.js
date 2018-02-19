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
            awesomeSms.getThreadsByDate().forEach((thread) => {
                let uiThread = prependThread(thread);
                if (uiThread)
                    uiThread.click(function() {
                        let threadId = parseInt($(this).attr("id").replace("thread-", ""));
                        $("#messages").empty();
                        for (let i = 0; i < awesomeSms.getThread(threadId).messages.length; i++)
                            prependMessage(awesomeSms.getThread(threadId).messages[i]);
                    });
            });
        };

        awesomeSms.refreshMessages();
    });

    // TODO: Auto select first thread
});
