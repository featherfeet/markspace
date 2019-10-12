function blockPasswordEntry(message) {
    $("#password_input,#duplicate_password_input").addClass("wrong");
    $("#message_p").text(message);
    $("#submit_button").prop("disabled", true).addClass("disabled");
}

function allowPasswordEntry() {
    $("#password_input,#duplicate_password_input").removeClass("wrong");
    $("#message_p").text("");
    $("#submit_button").prop("disabled", false).removeClass("disabled");
}

function checkPassword() {
    if ($("#password_input").val() !== $("#duplicate_password_input").val()) {
        blockPasswordEntry("Passwords must match.");
    }
    else if ($("#password_input").val() === $("#username_input").val() || $("#duplicate_password_input").val() === $("#username_input").val()) {
        blockPasswordEntry("Your password cannot be the same as your username.");
    }
    else {
        allowPasswordEntry();
    }
}

$(document).ready(function() {
    console.log("Signup page loaded.");
    $("#password_input,#duplicate_password_input").on("input", checkPassword);
});