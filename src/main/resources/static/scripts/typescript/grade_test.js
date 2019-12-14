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
        console.log("Test questions:");
        console.table(test_questions);
        console.log("Student answers:");
        console.table(student_answers);
        // Add links to the test questions.
        var questions_div = $("#questions_div");
        var test_question_index = 0;
        // Index of the question currently being graded.
        var current_test_question = 0;
        // All student answers to the current question being graded.
        var current_student_answers = new Array();
        var current_student_answer = 0; // Index (in current_student_answers) of the student answer currently being graded/displayed.
        // For every test question on this test...
        var number_of_student_answers = 0;
        var number_of_graded_student_answers = 0;
        var question_title = "";
        for (var _i = 0, test_questions_1 = test_questions; _i < test_questions_1.length; _i++) {
            var test_question = test_questions_1[_i];
            // Create a link at the bottom of the screen to this test question.
            var question_link = $("<a class=\"buttonlike test_question_a\">" + test_question.getRegions()[0].getLabel() + "</a>");
            questions_div.append(question_link);
            // If the link is clicked, jump to that question.
            question_link.on("click", null, { test_question_index: test_question_index }, function (event) {
                // Set the current test question.
                current_test_question = event.data.test_question_index;
                // Display the possible points for this test question.
                var possible_points = "/" + test_questions[current_test_question].getPoints();
                if (test_questions[current_test_question].getExtraCredit()) {
                    possible_points += " (EXTRA CREDIT)";
                }
                $("#possible_points_span").text(possible_points);
                // Show the question title.
                question_title = test_questions[current_test_question].getRegions()[0].getLabel();
                $("#current_question_h4").html("Currently grading question <i>" + question_title + "</i>.");
                // Highlight this question's link.
                $("a.test_question_a").removeClass("highlighted");
                $(event.target).addClass("highlighted");
                // Find all student answers to the current question.
                current_student_answers = student_answers.filter(function (student_answer) { return test_questions[current_test_question].getTestQuestionId() == student_answer.getTestQuestion().getTestQuestionId(); });
                number_of_student_answers = current_student_answers.length;
                // Find all student answers to the current question that have not yet been graded.
                current_student_answers = current_student_answers.filter(function (student_answer) { return student_answer.getScore() == ""; });
                number_of_graded_student_answers = number_of_student_answers - current_student_answers.length;
                // Display how many answers need to be graded.
                $("#student_answer_p").html("Grading student answer " + (number_of_graded_student_answers + 1) + " of " + number_of_student_answers + " for question <i>" + question_title + "</i>.");
                // Choose the first ungraded student answer to grade.
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
                console.log("Switching to the question at index " + event.data.test_question_index + ".");
            });
            test_question_index++;
        }
        // Handler for when a score is entered.
        $("#score_input").on("keypress", function (event) {
            if (event.key == "Enter") {
                // Save the score.
                // Go to the next student answer.
                current_student_answer++;
                number_of_graded_student_answers++;
                // Add images of the student answer.
                $("#student_answer_td").empty();
                for (var _i = 0, _a = current_student_answers[current_student_answer].getImageURLs(); _i < _a.length; _i++) {
                    var image_url = _a[_i];
                    $("#student_answer_td").append("<img src=\"" + image_url + "\" alt=\"Student answer.\">");
                }
                // Display which student answer is being graded.
                $("#student_answer_p").html("Grading student answer " + (number_of_graded_student_answers + 1) + " of " + number_of_student_answers + " for question <i>" + question_title + "</i>.");
            }
        });
    });
});
