const active_selection_color: string = "rgba(0, 0, 255, 0.3)";
const active_selection_outline_color: string = "rgb(0, 0, 255)";
const inactive_selection_color: string = "rgba(0, 0, 0, 0.3)";
const inactive_selection_outline_color: string = "rgb(0, 0, 0)";
const page_render_dpi: number = 100;
let number_of_pages: number = 0;
let current_page: number = 0;
let current_selection: CanvasRectangle = null;
let questions: TestQuestion[] = [];

function disableOrEnableButtons(): void {
    const previous_page_button: JQuery = $("#previous_page_button");
    const next_page_button: JQuery = $("#next_page_button");
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

function nextPage(): void {
    if (current_page + 1 <= number_of_pages - 1) {
        current_page++;
        disableOrEnableButtons();
    }
}

function previousPage(): void {
    if (current_page - 1 >= 0) {
        current_page--;
        disableOrEnableButtons();
    }
}

jQuery(function($): void {
    // Get canvas and set up renderer.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("canvas")[0];
    const renderer: CanvasRenderer = new CanvasRenderer(canvas);
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Put the test ID into a hidden input to be sent back to the server.
    $("#test_id").val(test_id.toString());
    // Set the renderer to render the test (with answers).
    // Get the number of pages in the PDF of the test with answers.
    // Once the number of pages has been retrieved, then start downloading rendered pages.
    $.get(`/render_test?test_id=${test_id}&answers=true&get_number_of_pages=true`, function(response) {
        number_of_pages = parseInt(response);
        renderer.createPages(number_of_pages);
        for (let i: number = 0; i < number_of_pages; i++) {
            renderer.addImageToPage(i, 0, 0, `/render_test?test_id=${test_id}&answers=true&page=${i}&dpi=${page_render_dpi}`, 0);
        }
    });
    // Continuously re-render the canvas at 30 fps.
    setInterval(function() {
        renderer.renderPage(current_page);
    }, (1.0 / 30.0) * 1000.0);
    // Set up page change buttons.
    $("#next_page_button").on("click", nextPage);
    $("#previous_page_button").on("click", previousPage);
    // Set up mousedown handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousedown", function(event): void {
        current_selection = renderer.addRectangleToPage(current_page, event.offsetX, event.offsetY, 0, 0, active_selection_color, active_selection_outline_color, `Q${questions.length + 1}`,1);
    });
    // Set up mousemove handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousemove", function(event): void {
        if (current_selection != null) {
            current_selection.setWidth(event.offsetX - current_selection.getX());
            current_selection.setHeight(event.offsetY - current_selection.getY());
        }
    });
    // Set up mouseup handler on the canvas for the click-and-drag selection.
    $(canvas).on("mouseup", function(): void {
        current_selection.setColor(inactive_selection_color);
        current_selection.setOutlineColor(inactive_selection_outline_color);
        const test_question: TestQuestion = new TestQuestion("1.0", current_page, [current_selection]);
        questions.push(test_question);
        $("#current_question_number").text(`question ${questions.length + 1}`);
        $("#no_questions_p").hide();
        $(".submit_button").show();
        $("#questions_table").append(`
        <tr>
            <td>${questions.length}</td>
            <td><input type="text" value="1.0" class="point_value_input" id="point_value_input_${questions.length}" /></td>
            <td><input type="checkbox" id="extra_credit_input_${questions.length}" /></td>
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
        current_selection = null;
    });
    // Set up form submit handler that adds the selected region's data (the questions that the user selected with the mouse) to a hidden input that will be sent to the server.
    $(".submit_button").on("click", function(event) {
        for (let question of questions) {
            for (let region of question.getRegions()) {
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