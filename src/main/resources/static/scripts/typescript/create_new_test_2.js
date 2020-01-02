var current_selection = null;
var questions = [];
var selection_active = false; // This flag indicates when the user is in the act of selecting a region in the canvas (i. e. the "drag" part of the "click-and-drag").
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
        // If the user is still dragging to select, DO NOT let them start a new selection.
        if (selection_active) {
            return;
        }
        selection_active = true;
        var x_inches = event.offsetX / page_render_dpi;
        var y_inches = event.offsetY / page_render_dpi;
        current_selection = test_viewer.getRenderer().addRectangleToPage(test_viewer.getCurrentPage(), x_inches, y_inches, 0, 0, active_selection_color, active_selection_outline_color, "Q" + (questions.length + 1), 1);
    });
    // Set up mousemove handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousemove", function (event) {
        if (current_selection != null) {
            current_selection.setWidth((event.offsetX / page_render_dpi) - current_selection.getX());
            current_selection.setHeight((event.offsetY / page_render_dpi) - current_selection.getY());
        }
    });
    // Set up mouseup handler on the canvas for the click-and-drag selection.
    $(canvas).on("mouseup", function () {
        selection_active = false;
        current_selection.setColor(inactive_selection_color);
        current_selection.setOutlineColor(inactive_selection_outline_color);
        var test_question = new TestQuestion(0, "1.0", test_viewer.getCurrentPage(), [current_selection], false);
        questions.push(test_question);
        $("#current_question_number").text("question " + (questions.length + 1));
        $("#no_questions_p").hide();
        $(".submit_button").show();
        $("#questions_table").append("\n        <tr>\n            <td>" + questions.length + "</td>\n            <td><input type=\"text\" value=\"1.0\" class=\"point_value_input\" id=\"point_value_input_" + questions.length + "\" /></td>\n            <td><input type=\"checkbox\" id=\"extra_credit_input_" + questions.length + "\" /></td>\n            <td><input type=\"radio\" name=\"student_identification\" id=\"student_identification_input_" + questions.length + "\" /></td>\n        </tr>");
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
        $("#student_identification_input_" + questions.length).on("click", null, test_question, function (event) {
            for (var i = 0; i < questions.length; i++) {
                for (var _i = 0, _a = questions[i].getRegions(); _i < _a.length; _i++) {
                    var region = _a[_i];
                    region.setColor(inactive_selection_color);
                    region.setOutlineColor(inactive_selection_outline_color);
                    region.setLabel("Q" + (i + 1));
                }
            }
            for (var _b = 0, _c = test_question.getRegions(); _b < _c.length; _b++) {
                var region = _c[_b];
                region.setColor("rgb(0, 255, 0, 0.2)");
                region.setOutlineColor("rgb(0, 150, 0)");
                region.setLabel("NAME");
            }
            test_question.setPoints("0.0");
            $("#point_value_input_" + questions.length).val("0.0");
        });
        current_selection = null;
    });
    // Set up form submit handler that adds the selected region's data (the questions that the user selected with the mouse) to a hidden input that will be sent to the server.
    $(".submit_button").on("click", function (event) {
        for (var _i = 0, questions_1 = questions; _i < questions_1.length; _i++) {
            var question = questions_1[_i];
            for (var _a = 0, _b = question.getRegions(); _a < _b.length; _a++) {
                var region = _b[_a];
                region.setX(region.getX());
                region.setY(region.getY());
                region.setWidth(region.getWidth());
                region.setHeight(region.getHeight());
            }
        }
        $("#test_questions_json").val(JSON.stringify(questions));
        $("#test_id").val(test_id.toString());
        $("#questions_form").trigger("submit");
    });
});
