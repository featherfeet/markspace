function retrieveQuestions(test_id) {
    return new Promise(function (resolve, reject) {
        $.getJSON("/get_questions?test_id=" + test_id, function (data) {
            var test_questions = new Array();
            for (var _i = 0, data_1 = data; _i < data_1.length; _i++) {
                var test_question_raw = data_1[_i];
                test_questions.push(TestQuestion.fromRawObject(test_question_raw));
            }
            resolve(test_questions);
        });
    });
}
