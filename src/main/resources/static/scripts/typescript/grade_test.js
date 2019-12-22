function scoreStudentAnswer(student_answer_id, score) {
    return new Promise(function (resolve, reject) {
        $.post("/score_student_answer", { student_answer_id: student_answer_id, score: score })
            .done(function () {
            resolve(null);
        })
            .fail(function () {
            reject(new Error("POST request to /score_student_answer failed."));
        });
    });
}
var app = new Vue({
    el: "#vue_div",
    data: {
        test_id: -1,
        test_questions: [],
        current_test_question_label: "",
        all_student_answers: [],
        current_student_answer_index: 0,
        current_student_answer_score: "",
        possible_points_for_current_student_answer: 0,
        current_student_answers: [],
        student_answer_image_urls: [],
        test_question_image_urls: []
    },
    methods: {
        jumpToTestQuestion: function (test_question, self) {
            // JS hackery.
            if (self == undefined) {
                self = this;
            }
            // Un-highlight all test questions' links.
            for (var _i = 0, _a = self.test_questions; _i < _a.length; _i++) {
                var other_test_question = _a[_i];
                Vue.set(other_test_question, "highlighted", false);
            }
            // Highlight the selected test question's link.
            for (var _b = 0, _c = self.test_questions; _b < _c.length; _b++) {
                var other_test_question = _c[_b];
                if (other_test_question.getTestQuestionId() == test_question.getTestQuestionId()) {
                    Vue.set(other_test_question, "highlighted", true);
                }
            }
            // Load images of self test question.
            self.test_question_image_urls = test_question.getImageURLs(self.test_id, true);
            // Find student answers that are for self test question.
            self.current_student_answers = self.all_student_answers.filter(function (student_answer) { return student_answer.getTestQuestion().getTestQuestionId() == test_question.getTestQuestionId(); });
            // Load a student answer for grading.
            self.current_student_answer_index = 0;
            self.student_answer_image_urls = self.current_student_answers[self.current_student_answer_index].getImageURLs();
            // Check if the question is a special "NAME" question.
            if (test_question.getRegions()[0].getLabel() == "NAME") {
                self.possible_points_for_current_student_answer = "NAME";
            }
            else {
                self.possible_points_for_current_student_answer = self.current_student_answers[self.current_student_answer_index].getPointsPossible();
            }
            // Display the name of this question.
            self.current_test_question_label = test_question.getRegions()[0].getLabel();
            // Debug info.
            console.log("Jumping to question " + test_question.getRegions()[0].getLabel() + ".");
            // Focus the score input (put the user's cursor in it so that they can type).
            $("#score_input")[0].focus();
        },
        submitStudentAnswerScore: function () {
            var self = this;
            console.log("Submitting student answer score...");
            // Submit the entered score for the current student answer.
            var current_student_answer = this.current_student_answers[this.current_student_answer_index];
            // If the score is empty, auto-score 100%.
            if (this.current_student_answer_score == "") {
                // Send the score to the server.
                scoreStudentAnswer(current_student_answer.getStudentAnswerId(), current_student_answer.getPointsPossible());
                // Save the score to the client-side object.
                current_student_answer.setScore(current_student_answer.getPointsPossible());
            }
            // Otherwise, enter whatever was entered as a score. This allows non-numeric scores for things like NAME questions.
            else {
                // Send the score to the server.
                scoreStudentAnswer(current_student_answer.getStudentAnswerId(), this.current_student_answer_score);
                // Save the score to the client-side object.
                current_student_answer.setScore(this.current_student_answer_score);
            }
            // Clear the score input.
            this.current_student_answer_score = "";
            // If there are no more ungraded student answers for this question, then find a new question.
            if (this.current_student_answer_index >= this.current_student_answers.length - 1) {
                var ungraded_student_answers = this.all_student_answers.filter(function (student_answer) { return student_answer.getScore() == ""; });
                if (ungraded_student_answers.length == 0) {
                    alert("Congratulations! You have finished grading this test! You may now press the back button to return to the tests page, or remain here to gaze upon your handiwork.");
                }
                else {
                    self.jumpToTestQuestion(ungraded_student_answers[0].getTestQuestion(), self);
                }
            }
            // If there are more ungraded student answers for this question, advance to the next one.
            else {
                this.current_student_answer_index++;
                self.student_answer_image_urls = self.current_student_answers[self.current_student_answer_index].getImageURLs();
            }
        }
    },
    mounted: function () {
        // Get the test ID of the test being created.
        var raw_url = window.location.href;
        var url = new URL(raw_url);
        this.test_id = parseInt(url.searchParams.get("test_id"));
        // Fetch the test questions and student answers using Promises.
        var self = this;
        var questions_promise = retrieveQuestions(this.test_id);
        var student_answers_promise = retrieveStudentAnswers(this.test_id, -1);
        // When both test questions AND student answers have been retrieved, do this...
        Promise.all([questions_promise, student_answers_promise]).then(function (values) {
            // Get the retrieved student answers and questions.
            var test_questions = values[0];
            var student_answers = values[1];
            // Store the retrieved test questions for the data binding UI.
            self.test_questions = test_questions;
            // Store the retrieved student answers for the data binding UI.
            self.all_student_answers = student_answers;
            // Find the "NAME" test question and jump the UI to it.
            var name_question = test_questions.filter(function (test_question) { return test_question.getRegions()[0].getLabel() == "NAME"; })[0];
            self.jumpToTestQuestion(name_question, self);
        });
    }
});
