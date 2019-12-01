jQuery(function ($) {
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    var canvas = $("#question_canvas")[0];
    var renderer = new CanvasRenderer(canvas, 1000, 400);
    var student_answer_image = null;
    // Retrieve questions on this test from the database.
    retrieveQuestions(test_id).then(function (test_questions) {
        // Add links to the test questions.
        var questions_div = $("#questions_div");
        var test_question_index = 0;
        // Index of the question currently being graded.
        var current_test_question = 0;
        for (var _i = 0, test_questions_1 = test_questions; _i < test_questions_1.length; _i++) {
            var test_question = test_questions_1[_i];
            // Create a link at the bottom of the screen to this test question.
            var question_link = $("<a class=\"buttonlike test_question_a\">" + test_question.getRegions()[0].getLabel() + "</a>");
            questions_div.append(question_link);
            // If the link is clicked, jump to that question.
            question_link.on("click", null, test_question_index, function (event) {
                current_test_question = test_question_index;
                // TODO:
                student_answer_image = renderer.addImageToPage(0, 0, 0, "/render_student_answer", 0);
                console.log("Switching to the question at index " + event.data + ".");
            });
            test_question_index++;
        }
    });
});
