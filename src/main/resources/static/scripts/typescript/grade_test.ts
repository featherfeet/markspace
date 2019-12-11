jQuery(function($): void {
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Retrieve questions on this test from the database.
    const test_questions_promise: Promise<TestQuestion[]> = retrieveQuestions(test_id);
    const student_answers_promise: Promise<StudentAnswer[]> = retrieveStudentAnswers(test_id, -1);
    Promise.all([test_questions_promise, student_answers_promise]).then(function(values) {
        const test_questions: TestQuestion[] = values[0];
        const student_answers: StudentAnswer[] = values[1];
        // Add links to the test questions.
        const questions_div: JQuery<HTMLDivElement> = $("#questions_div");
        let test_question_index: number = 0;
        // Index of the question currently being graded.
        let current_test_question: number = 0;
        // All student answers to the current question being graded.
        let current_student_answers: StudentAnswer[] = new Array<StudentAnswer>();
        let current_student_answer: number = 0; // Index (in current_student_answers) of the student answer currently being graded/displayed.
        // For every test question on this test...
        for (let test_question of test_questions) {
            // Create a link at the bottom of the screen to this test question.
            const question_link: JQuery<HTMLAnchorElement> = $(`<a class="buttonlike test_question_a">${test_question.getRegions()[0].getLabel()}</a>`);
            questions_div.append(question_link);
            // If the link is clicked, jump to that question.
            question_link.on("click", null, {test_question_index: test_question_index}, function(event) {
                // Set the current test question.
                current_test_question = event.data.test_question_index;
                // Highlight this question's link.
                $("a.test_question_a").removeClass("highlighted");
                $(event.target).addClass("highlighted");
                // Find all student answers to the current question.
                current_student_answers = student_answers.filter(student_answer => student_answer.getTestQuestion().equals(test_questions[current_test_question]));
                current_student_answer = 0;
                // Add images of the student answer.
                $("#student_answer_td").empty();
                for (let image_url of current_student_answers[current_student_answer].getImageURLs()) {
                    $("#student_answer_td").append(`<img src="${image_url}" alt="Student answer.">`);
                }
                // Add images of the question.
                $("#correct_answer_td").empty();
                for (let image_url of test_questions[current_test_question].getImageURLs(test_id, true)) {
                    $("#correct_answer_td").append(`<img src="${image_url}" alt="Correct answer.">`);
                }
                console.log(`Switching to the question at index ${event.data}.`);
            });
            test_question_index++;
        }
    });
});
