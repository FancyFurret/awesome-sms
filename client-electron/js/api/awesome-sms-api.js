$.holdReady(true);

$.when(
    $.getScript("js/api/model/contact.js"),
    $.getScript("js/api/model/message.js"),
    $.getScript("js/api/model/thread.js"),
    $.getScript("js/api/util/util.js")
).then(function() {
    console.log("AwesomeSMS api v0.1.0");
    $.holdReady(false);
});
