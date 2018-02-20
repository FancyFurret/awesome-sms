let templateThreadThumbnailText;
let templateMessage;
let templateMessageImage;

const colorSent = materialColors.grey["800"];

function initUi() {
    let templateThreadThumbnailTextSource = $("#template-thread-thumbnail-text").html();
    templateThreadThumbnailText = Handlebars.compile(templateThreadThumbnailTextSource);

    let templateMessageSource = $("#template-message").html();
    templateMessage = Handlebars.compile(templateMessageSource);

    let templateMessageImageSource = $("#template-message-image").html();
    templateMessageImage = Handlebars.compile(templateMessageImageSource);
}

function prependThread(thread) {
    if (thread.participants.length !== 1) {
        console.error("Does not support group messages yet!");
        return false;
    }

    let abbr = "";
    let name = "";
    if (thread.participants[0].name != null) {
        let names = thread.participants[0].name.split(' ');
        abbr += names[0][0].toUpperCase();
        if (names.length > 1)
            abbr += names[names.length - 1][0].toUpperCase();
        name = thread.participants[0].name;
    }
    else {
        abbr = "#";
        name = thread.participants[0].phones[0].number;
    }


    let uiThread = $(
        templateThreadThumbnailText(
            {
                id: thread.id,
                thumbnail: abbr,
                color: thread.participants[0].color,
                name: name,
                preview: thread.getMostRecentMessage().body
            }
        ));
    $("#threads").prepend(uiThread);
    uiThread.dotdotdot({
       truncate: "letter"
    });
    return uiThread;
}

function appendMessage(message) {
    if (message.thread.participants.length !== 1) {
        console.error("Does not support group messages yet!");
        return false;
    }

    if (message.attachments != null) {
        for (let i = 0; i < message.attachments.length; i++) {
            let uiImage = $(
                templateMessageImage(
                    {
                        id: message.id + "-" + i,
                        color: message.sender == null ? colorSent : message.sender.color,
                        sent: message.sender == null,
                        mime: message.attachments[i].mime,
                        image: message.attachments[i].data
                    }
                )
            );
            $("#messages").append(uiImage);
        }
    }
    if (message.body != null) {
        let uiMessage = $(
            templateMessage(
                {
                    id: message.id,
                    color: message.sender == null ? colorSent : message.sender.color,
                    sent: message.sender == null,
                    message: message.body
                }
            )
        );
        $("#messages").append(uiMessage);
    }
    return true;
}
