// let phoneUtil = require('google-libphonenumber').phoneUtil
//     , PNF = require('google-libphonenumber').PhoneNumberFormat
//     , PNT = require('google-libphonenumber').PhoneNumberType;

let templateThread;
let templateMessage;
// let templateMessageImage;

const colorSent = materialColors.grey["800"];

function initUi() {
    let templateThumbnailSource = $("#template-thumbnail").html();
    let templateThumbnail = Handlebars.compile(templateThumbnailSource);
    Handlebars.registerPartial("template-thumbnail", templateThumbnail);

    let templateThreadSource = $("#template-thread").html();
    templateThread = Handlebars.compile(templateThreadSource);

    let templateMessageSource = $("#template-message").html();
    templateMessage = Handlebars.compile(templateMessageSource);
}

function prependThread(thread) {
    let abbr = thread.participants.length > 1 ? "#" : thread.getParticipantContact(0).getAbbreviation();
    let name = "";
    for (let i = 0; i < thread.participants.length; i++) {
        if (i > 0) // More than 1 participant
            abbr = "#";
        else
            abbr = thread.getParticipantContact(i).getAbbreviation();

        name += thread.getParticipantContact(i).getDisplayName() + ", ";
    }
    name = name.slice(0, -2);

    let uiThread = $(
        templateThread(
            {
                id: thread.id,
                thumbnail: abbr,
                color: thread.getParticipantContact(0).color,
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
    let color = message.getSenderContact() == null ? colorSent : message.getSenderContact().color;
    let sender;
    let thumbnail;
    if (message.thread.participants.length > 1 && message.sender != null) {
        sender = message.getSenderContact().getDisplayName();
        thumbnail = message.getSenderContact().getAbbreviation();
    }

    // Add message attachments
    if (message.attachments != null) {
        for (let i = 0; i < message.attachments.length; i++) {
            if (message.attachments[i].mime.startsWith("image")) {
                let uiImage = $(
                    templateMessage(
                        {
                            id: message.id + "-" + i,
                            color: color,
                            sent: message.sender == null,
                            mime: message.attachments[i].mime,
                            image: message.attachments[i].data,
                            sender: sender,
                            thumbnail: thumbnail,
                            pending: message.pending
                        }
                    )
                );
                $("#messages").append(uiImage);
            }
            else
                console.log("Currently unsupported mime: " + message.attachments[i].mime);
        }
    }

    // Add message
    if (message.body != null) {
        let uiMessage = $(
            templateMessage(
                {
                    id: message.id,
                    color: color,
                    sent: message.sender == null,
                    message: message.body,
                    sender: sender,
                    thumbnail: thumbnail,
                    pending: message.pending
                }
            )
        );
        $("#messages").append(uiMessage);
    }
    return true;
}
