let selectedThread;
let awesomeSms;

$(document).ready(function () {
    console.log("loaded");

    initUi();

    // TODO: Loading screen
    // TODO: Cache

    awesomeSms = new AwesomeSms();
    awesomeSms.connectToServer("localhost:11150", (res) => {
        if (res.error) {
            console.error(res.error);
            return;
        }
        console.log("Connected to server! Refreshing messages...");

        $("#entry-bar-input").keyup((event) => {
            if (event.keyCode === 13)
                $("#entry-bar-send").click();
        });
        $("#entry-bar-send").click(() => {
            awesomeSms.sendMessage(getThreadIdFromThread(selectedThread), $("#entry-bar-input").val());
            $("#entry-bar-input").val("")
        });

        awesomeSms.onMessageReceived = () => {
            $("#threads").empty();
            awesomeSms.getThreadsByDate().forEach((thread) => {
                let uiThread = prependThread(thread);
                if (uiThread)
                    uiThread.click(function() {
                        selectedThread = $(this);
                        refreshThreadMessages();
                    });
            });

            if (selectedThread === undefined) {
                selectedThread = $(".thread").first();
            }

            refreshThreadMessages();
        };

        awesomeSms.refreshMessages();
    });
});

function refreshThreadMessages() {
    let threadId = getThreadIdFromThread(selectedThread);
    $("#messages").empty();
    for (let i = 0; i < awesomeSms.getThread(threadId).messages.length; i++)
        appendMessage(awesomeSms.getThread(threadId).messages[i]);
}

function getThreadIdFromThread(thread) {
    return parseInt(thread.attr("id").replace("thread-", ""));
}