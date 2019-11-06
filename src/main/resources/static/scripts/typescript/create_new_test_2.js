var active_selection_color = "rgba(0, 0, 255, 0.3)";
var active_selection_outline_color = "rgb(0, 0, 255)";
var inactive_selection_color = "rgba(0, 0, 0, 0.3)";
var inactive_selection_outline_color = "rgb(0, 0, 0)";
var page_render_dpi = 100;
var page_render_fps = 30;
var current_selection = null;
var questions = [];
jQuery(function ($) {
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    var canvas = $("canvas")[0];
    var next_page_button = $("#next_page_button")[0];
    var previous_page_button = $("#previous_page_button")[0];
    var test_viewer = new TestViewer(canvas, next_page_button, previous_page_button, test_id, page_render_dpi, page_render_fps);
    // Put the test ID into a hidden input to be sent back to the server.
    $("#test_id").val(test_id.toString());
    // Set up mousedown handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousedown", function (event) {
        current_selection = test_viewer.getRenderer().addRectangleToPage(test_viewer.getCurrentPage(), event.offsetX, event.offsetY, 0, 0, active_selection_color, active_selection_outline_color, "Q" + (questions.length + 1), 1);
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
        var test_question = new TestQuestion("1.0", test_viewer.getCurrentPage(), [current_selection], false);
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
