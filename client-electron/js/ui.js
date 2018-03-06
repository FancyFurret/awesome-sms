// let phoneUtil = require('google-libphonenumber').phoneUtil
//     , PNF = require('google-libphonenumber').PhoneNumberFormat
//     , PNT = require('google-libphonenumber').PhoneNumberType;

let templateThread;
let templateMessage;
let templateEntryBarImage;
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

    let templateEntryBarImageSource = $("#template-entry-bar-image").html();
    templateEntryBarImage = Handlebars.compile(templateEntryBarImageSource);
}

function prependThread(thread) {
    let thumbnailText = thread.participants.length > 1 ? "#" : thread.getParticipantContact(0).getAbbreviation();
    let thumbnailImage;
    let name = "";
    for (let i = 0; i < thread.participants.length; i++) {
        if (i > 0) // More than 1 participant
            thumbnailText = i + 1;
        else if (thread.getParticipantContact(i).thumbnail !== undefined)
            thumbnailImage = thread.getParticipantContact(i).thumbnail;
        else
            thumbnailText = thread.getParticipantContact(i).getAbbreviation();

        name += thread.getParticipantContact(i).getDisplayName() + ", ";
    }
    name = name.slice(0, -2);

    let uiThread = $(
        templateThread(
            {
                id: thread.id,
                thumbnailImage: thumbnailImage,
                thumbnailText: thumbnailText,
                color: thread.getParticipantContact(0).color,
                name: name,
                preview: thread.getMostRecentMessage().body
            }
        ));
    $("#thread-list").prepend(uiThread);
    return uiThread;
}

function appendMessage(message) {
    let color = message.getSenderContact() == null ? colorSent : message.getSenderContact().color;
    let sender;
    let thumbnailImage;
    let thumbnailText;
    if (message.thread.participants.length > 1 && message.sender != null) {
        sender = message.getSenderContact().getDisplayName();
        if (message.getSenderContact().thumbnail !== undefined)
            thumbnailImage = message.getSenderContact().thumbnail;
        else
            thumbnailText = message.getSenderContact().getAbbreviation();
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
                            thumbnailImage: thumbnailImage,
                            thumbnailText: thumbnailText,
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
    if (message.body) {
        let uiMessage = $(
            templateMessage(
                {
                    id: message.id,
                    color: color,
                    sent: message.sender == null,
                    message: message.body,
                    sender: sender,
                    thumbnailImage: thumbnailImage,
                    thumbnailText: thumbnailText,
                    pending: message.pending
                }
            )
        );
        $("#messages").append(uiMessage);
    }
    return true;
}

function appendEntryBarImage(id, image) {
    let uiEntryBarImage = $(
        templateEntryBarImage(
            {
                id: id,
                image: image
            }
        )
    );
    $("#entry-bar-images").append(uiEntryBarImage);

    return uiEntryBarImage;
}
