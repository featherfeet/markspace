jQuery(function ($) {
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
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
        // All student answers to the current question being graded.
        var current_student_answers = new Array();
        var current_student_answer = 0; // Index (in current_student_answers) of the student answer currently being graded/displayed.
        // For every test question on this test...
        for (var _i = 0, test_questions_1 = test_questions; _i < test_questions_1.length; _i++) {
            var test_question = test_questions_1[_i];
            // Create a link at the bottom of the screen to this test question.
            var question_link = $("<a class=\"buttonlike test_question_a\">" + test_question.getRegions()[0].getLabel() + "</a>");
            questions_div.append(question_link);
            // If the link is clicked, jump to that question.
            question_link.on("click", null, { test_question_index: test_question_index }, function (event) {
                // Set the current test question.
                current_test_question = event.data.test_question_index;
                // Highlight this question's link.
                $("a.test_question_a").removeClass("highlighted");
                $(event.target).addClass("highlighted");
                // Find all student answers to the current question.
                current_student_answers = student_answers.filter(function (student_answer) { return student_answer.getTestQuestion().equals(test_questions[current_test_question]); });
                current_student_answer = 0;
                // Add images of the student answer.
                $("#student_answer_td").empty();
                for (var _i = 0, _a = current_student_answers[current_student_answer].getImageURLs(); _i < _a.length; _i++) {
                    var image_url = _a[_i];
                    $("#student_answer_td").append("<img src=\"" + image_url + "\" alt=\"Student answer.\">");
                }
                // Add images of the question.
                $("#correct_answer_td").empty();
                for (var _b = 0, _c = test_questions[current_test_question].getImageURLs(test_id, true); _b < _c.length; _b++) {
                    var image_url = _c[_b];
                    $("#correct_answer_td").append("<img src=\"" + image_url + "\" alt=\"Correct answer.\">");
                }
                console.log("Switching to the question at index " + event.data + ".");
            });
            test_question_index++;
        }
    });
});
