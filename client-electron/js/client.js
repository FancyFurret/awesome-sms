let selectedThread;
let awesomeSms;

$(document).ready(function () {
    console.log("loaded");

    initUi();

    // TODO: Loading screen
    // TODO: Cache

    let currentAttachmentId = 0;
    let selectedAttachments = new Map();

    awesomeSms = new AwesomeSms();
    awesomeSms.connectToServer("localhost:11150", (res) => {
        if (res.error) {
            console.error(res.error);
            return;
        }
        console.log("Connected to server! Refreshing messages...");

        // Attachments
        $("#entry-bar-attachment").click(() => {
            $("#entry-bar-attachment-input").trigger('click');
        });
        $("#entry-bar-attachment-input").change(() => {
            let input = $("#entry-bar-attachment-input")[0];
            if (!input.files[0]) {
                console.log("No files selected");
                return;
            }

            console.log("Picked: " + input.files[0]);


            for (let i = 0; i < input.files.length; i++) {
                let reader = new FileReader();
                reader.onload = (e) => {
                    let uiEntryBarImage = appendEntryBarImage(currentAttachmentId, e.target.result);
                    selectedAttachments.set(currentAttachmentId, {
                        mime: e.target.result.split(";")[0].split(":")[1],
                        data: e.target.result.split(",")[1]
                    });
                    currentAttachmentId++;

                    uiEntryBarImage.find(".entry-bar-image-delete").click(function () {
                        let id = parseInt($(this).parent().attr("id").replace("entry-bar-image-", ""));
                        selectedAttachments.delete(id);

                        $(this).parent().remove()
                    });
                };
                reader.readAsDataURL(input.files[i]);
            }

            $("#entry-bar-attachment-input").val("");
        });

        // Text
        $("#entry-bar-input").keyup((event) => {
            if (event.keyCode === 13)
                $("#entry-bar-send").click();
        });

        // Submit
        $("#entry-bar-send").click(() => {
            if ($("entry-bar-input").val() !== "") { // TODO: Fix
                awesomeSms.sendMessage(
                    getThreadIdFromThread(selectedThread),
                    $("#entry-bar-input").val(),
                    Array.from(selectedAttachments.values())
                );
                $("#entry-bar-input").val("");
                $("#entry-bar-images").empty();
                currentAttachmentId = 0;
            }
        });

        awesomeSms.onContactReceived = () => {
            refreshThreads();
        };

        awesomeSms.onMessageReceived = () => {
            refreshThreads();
            awesomeSms.refreshContacts();
        };

        awesomeSms.refreshMessages();
    });
});

function refreshThreads() {
    $("#threads").empty();
    awesomeSms.getThreadsByDate().forEach((thread) => {
        let uiThread = prependThread(thread);
        if (uiThread)
            uiThread.click(function () {
                selectedThread = $(this);
                refreshThreadMessages();
            });
    });

    if (selectedThread === undefined) {
        selectedThread = $(".thread").first();
    }

    refreshThreadMessages();
}

function refreshThreadMessages() {
    if (selectedThread === undefined || selectedThread.length === 0)
        return;

    let threadId = getThreadIdFromThread(selectedThread);
    $("#messages").empty();
    for (let i = 0; i < awesomeSms.getThread(threadId).messages.length; i++)
        appendMessage(awesomeSms.getThread(threadId).messages[i]);
}

function getThreadIdFromThread(thread) {
    return parseInt(thread.attr("id").replace("thread-", ""));
}