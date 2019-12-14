let current_selection: CanvasRectangle = null;
let questions: TestQuestion[] = [];

jQuery(function($): void {
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("canvas")[0];
    const next_page_button: HTMLButtonElement = <HTMLButtonElement> $("#next_page_button")[0];
    const previous_page_button: HTMLButtonElement = <HTMLButtonElement> $("#previous_page_button")[0];
    const test_viewer: TestViewer = new TestViewer(canvas, next_page_button, previous_page_button, test_id, page_render_dpi, page_render_fps);
    // Put the test ID into a hidden input to be sent back to the server.
    $("#test_id").val(test_id.toString());
    // Set up mousedown handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousedown", function(event): void {
        const x_inches = event.offsetX / page_render_dpi;
        const y_inches = event.offsetY / page_render_dpi;
        console.log(x_inches);
        console.log(y_inches);
        current_selection = test_viewer.getRenderer().addRectangleToPage(test_viewer.getCurrentPage(), x_inches, y_inches, 0, 0, active_selection_color, active_selection_outline_color, `Q${questions.length + 1}`,1);
        console.log(current_selection);
    });
    // Set up mousemove handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousemove", function(event): void {
        if (current_selection != null) {
            current_selection.setWidth((event.offsetX / page_render_dpi) - current_selection.getX());
            current_selection.setHeight((event.offsetY / page_render_dpi) - current_selection.getY());
        }
    });
    // Set up mouseup handler on the canvas for the click-and-drag selection.
    $(canvas).on("mouseup", function(): void {
        current_selection.setColor(inactive_selection_color);
        current_selection.setOutlineColor(inactive_selection_outline_color);
        const test_question: TestQuestion = new TestQuestion(0, "1.0", test_viewer.getCurrentPage(), [current_selection], false);
        questions.push(test_question);
        $("#current_question_number").text(`question ${questions.length + 1}`);
        $("#no_questions_p").hide();
        $(".submit_button").show();
        $("#questions_table").append(`
        <tr>
            <td>${questions.length}</td>
            <td><input type="text" value="1.0" class="point_value_input" id="point_value_input_${questions.length}" /></td>
            <td><input type="checkbox" id="extra_credit_input_${questions.length}" /></td>
            <td><input type="radio" name="student_identification" id="student_identification_input_${questions.length}" /></td>
        </tr>`);
        $(`#point_value_input_${questions.length}`).on("focus", function(event) {
            const target: HTMLInputElement = <HTMLInputElement> event.target;
            if (target.value == target.defaultValue) {
                target.value = "";
            }
        });
        $(`#point_value_input_${questions.length}`).on("input", null, test_question, function(event) {
            let pointValue = <string> $(event.target).val();
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
        $(`#extra_credit_input_${questions.length}`).on("input", null, test_question, function(event) {
            const checked: boolean = $(event.target).is(":checked");
            test_question.setExtraCredit(checked);
        });
        $(`#student_identification_input_${questions.length}`).on("click", null, test_question, function(event) {
            for (let i: number = 0; i < questions.length; i++) {
                for (let region of questions[i].getRegions()) {
                    region.setColor(inactive_selection_color);
                    region.setOutlineColor(inactive_selection_outline_color);
                    region.setLabel(`Q${i + 1}`);
                }
            }
            for (let region of test_question.getRegions()) {
                region.setColor("rgb(0, 255, 0, 0.2)");
                region.setOutlineColor("rgb(0, 150, 0)");
                region.setLabel("NAME");
            }
        });
        current_selection = null;
    });
    // Set up form submit handler that adds the selected region's data (the questions that the user selected with the mouse) to a hidden input that will be sent to the server.
    $(".submit_button").on("click", function(event) {
        for (let question of questions) {
            for (let region of question.getRegions()) {
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
