jQuery(function ($) {
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    var canvas = $("#question_canvas")[0];
    var renderer = new CanvasRenderer(canvas, 1000, 1500);
    renderer.createPages(1);
    setInterval(function () {
        renderer.renderPage(0);
    }, (1.0 / page_render_fps * 1000.0));
    var current_student_answer_images = new Array();
    // Retrieve questions on this test from the database.
    var test_questions_promise = retrieveQuestions(test_id);
    var student_answers_promise = retrieveStudentAnswers(test_id, -1);
    Promise.all([test_questions_promise, student_answers_promise]).then(function (values) {
        var test_questions = values[0];
        var student_answers = values[1];
        // Add links to the test questions.
        var questions_div = $("#questions_div");
        var test_question_index = 0;
        // Index of the question currently being graded.
        var current_test_question = 0;
        var _loop_1 = function (test_question) {
            // Create a link at the bottom of the screen to this test question.
            var question_link = $("<a class=\"buttonlike test_question_a\">" + test_question.getRegions()[0].getLabel() + "</a>");
            questions_div.append(question_link);
            // If the link is clicked, jump to that question.
            question_link.on("click", null, test_question_index, function (event) {
                current_test_question = test_question_index;
                // TODO:
                for (var _i = 0, student_answers_1 = student_answers; _i < student_answers_1.length; _i++) {
                    var student_answer = student_answers_1[_i];
                    console.table(student_answer.getTestQuestion());
                    console.table(test_question);
                    if (student_answer.getTestQuestion().equals(test_question)) {
                        renderer.emptyPage(0);
                        current_student_answer_images = student_answer.renderCanvasImages(renderer, 0, 0);
                        break;
                    }
                }
                console.log("Switching to the question at index " + event.data + ".");
            });
            test_question_index++;
        };
        for (var _i = 0, test_questions_1 = test_questions; _i < test_questions_1.length; _i++) {
            var test_question = test_questions_1[_i];
            _loop_1(test_question);
        }
    });
});
