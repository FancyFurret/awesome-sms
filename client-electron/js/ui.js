let templateThreadThumbnailText;
let templateMessage;

const colorSent = materialColors.grey["800"];

function init() {
    let templateThreadThumbnailTextSource = $("#template-thread-thumbnail-text").html();
    templateThreadThumbnailText = Handlebars.compile(templateThreadThumbnailTextSource);

    let templateMessageSource = $("#template-message").html();
    templateMessage = Handlebars.compile(templateMessageSource);
}

function newThread(thread) {
    if (thread.participants.length !== 1) {
        console.error("Does not support group messages yet!");
        return false;
    }

    let abbr = "";
    let names = thread.participants[0].name.split(' ');
    abbr += names[0][0].toUpperCase();
    if (names.length > 1)
        abbr += names[names.length - 1][0].toUpperCase();

    $("#threads").append(
        templateThreadThumbnailText(
            {
                id: thread.id,
                thumbnail: abbr,
                color: thread.participants[0].color,
                name: thread.participants[0].name,
                preview: thread.messages[0].body
            }
        )
    );
}

function newMessage(message) {
    if (message.thread.participants.length !== 1) {
        console.error("Does not support group messages yet!");
        return false;
    }

    $("#messages").append(
        templateMessage(
            {
                id: message.id,
                color: message.sender == null ? colorSent : message.sender.color,
                sent: message.sender == null,
                message: message.body
            }
        )
    );
}
