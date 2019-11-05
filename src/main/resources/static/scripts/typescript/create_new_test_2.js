var active_selection_color = "rgba(0, 0, 255, 0.3)";
var active_selection_outline_color = "rgb(0, 0, 255)";
var inactive_selection_color = "rgba(0, 0, 0, 0.3)";
var inactive_selection_outline_color = "rgb(0, 0, 0)";
var page_render_dpi = 100;
var number_of_pages = 0;
var current_page = 0;
var current_selection = null;
var questions = [];
function disableOrEnableButtons() {
    var previous_page_button = $("#previous_page_button");
    var next_page_button = $("#next_page_button");
    if (current_page == 0) {
        previous_page_button.prop("disabled", true);
        previous_page_button.addClass("disabled");
    }
    else {
        previous_page_button.prop("disabled", false);
        previous_page_button.removeClass("disabled");
    }
    if (current_page == number_of_pages - 1) {
        next_page_button.prop("disabled", true);
        next_page_button.addClass("disabled");
    }
    else {
        next_page_button.prop("disabled", false);
        next_page_button.removeClass("disabled");
    }
}
function nextPage() {
    if (current_page + 1 <= number_of_pages - 1) {
        current_page++;
        disableOrEnableButtons();
    }
}
function previousPage() {
    if (current_page - 1 >= 0) {
        current_page--;
        disableOrEnableButtons();
    }
}
jQuery(function ($) {
    // Get canvas and set up renderer.
    var canvas = $("canvas")[0];
    var renderer = new CanvasRenderer(canvas);
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Put the test ID into a hidden input to be sent back to the server.
    $("#test_id").val(test_id.toString());
    // Set the renderer to render the test (with answers).
    // Get the number of pages in the PDF of the test with answers.
    // Once the number of pages has been retrieved, then start downloading rendered pages.
    $.get("/render_test?test_id=" + test_id + "&answers=true&get_number_of_pages=true", function (response) {
        number_of_pages = parseInt(response);
        renderer.createPages(number_of_pages);
        for (var i = 0; i < number_of_pages; i++) {
            renderer.addImageToPage(i, 0, 0, "/render_test?test_id=" + test_id + "&answers=true&page=" + i + "&dpi=" + page_render_dpi, 0);
        }
    });
    // Continuously re-render the canvas at 30 fps.
    setInterval(function () {
        renderer.renderPage(current_page);
    }, (1.0 / 30.0) * 1000.0);
    // Set up page change buttons.
    $("#next_page_button").on("click", nextPage);
    $("#previous_page_button").on("click", previousPage);
    // Set up mousedown handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousedown", function (event) {
        current_selection = renderer.addRectangleToPage(current_page, event.offsetX, event.offsetY, 0, 0, active_selection_color, active_selection_outline_color, "Q" + (questions.length + 1), 1);
    });
    // Set up mousemove handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousemove", function (event) {
        if (current_selection != null) {
            current_selection.setWidth(event.offsetX - current_selection.getX());
            current_selection.setHeight(event.offsetY - current_selection.getY());
        }
    });
    // Set up mouseup handler on the canvas for the click-and-drag selection.
    $(canvas).on("mouseup", function () {
        current_selection.setColor(inactive_selection_color);
        current_selection.setOutlineColor(inactive_selection_outline_color);
        var test_question = new TestQuestion("1.0", current_page, [current_selection], false);
        questions.push(test_question);
        $("#current_question_number").text("question " + (questions.length + 1));
        $("#no_questions_p").hide();
        $(".submit_button").show();
        $("#questions_table").append("\n        <tr>\n            <td>" + questions.length + "</td>\n            <td><input type=\"text\" value=\"1.0\" class=\"point_value_input\" id=\"point_value_input_" + questions.length + "\" /></td>\n            <td><input type=\"checkbox\" id=\"extra_credit_input_" + questions.length + "\" /></td>\n        </tr>");
        $("#point_value_input_" + questions.length).on("focus", function (event) {
            var target = event.target;
            if (target.value == target.defaultValue) {
                target.value = "";
            }
        });
        $("#point_value_input_" + questions.length).on("input", null, test_question, function (event) {
            var pointValue = $(event.target).val();
            if (!$.isNumeric(pointValue)) {
                $(event.target).addClass("wrong");
                $(".submit_button").addClass("disabled");
                $(".submit_button").attr("disabled", "disabled");
                pointValue = "1.0";
            }
            else {
                $(event.target).removeClass("wrong");
                $(".submit_button").removeClass("disabled");
                $(".submit_button").removeAttr("disabled");
            }
            event.data.setPoints(pointValue);
        });
        $("#extra_credit_input_" + questions.length).on("input", null, test_question, function (event) {
            var checked = $(event.target).is(":checked");
            test_question.setExtraCredit(checked);
        });
        current_selection = null;
    });
    // Set up form submit handler that adds the selected region's data (the questions that the user selected with the mouse) to a hidden input that will be sent to the server.
    $(".submit_button").on("click", function (event) {
        for (var _i = 0, questions_1 = questions; _i < questions_1.length; _i++) {
            var question = questions_1[_i];
            for (var _a = 0, _b = question.getRegions(); _a < _b.length; _a++) {
                var region = _b[_a];
                region.setX(region.getX() / page_render_dpi);
                region.setY(region.getY() / page_render_dpi);
                region.setWidth(region.getWidth() / page_render_dpi);
                region.setHeight(region.getHeight() / page_render_dpi);
            }
        }
        $("#test_questions_json").val(JSON.stringify(questions));
        $("#test_id").val(test_id.toString());
        $("#questions_form").trigger("submit");
    });
});
