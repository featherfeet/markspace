function retrieveStudentAnswers(test_id, student_answer_file_id) {
    return new Promise(function (resolve, reject) {
        $.getJSON("/get_student_answers_for_test?test_id=" + test_id + "&student_answer_file_id=" + student_answer_file_id, function (data) {
            var student_answers = new Array();
            for (var _i = 0, data_1 = data; _i < data_1.length; _i++) {
                var student_answer_raw = data_1[_i];
                var student_answer = StudentAnswer.fromRawObject(student_answer_raw);
                student_answers.push(student_answer);
            }
            console.log("Found " + student_answers.length + " student answers.");
            resolve(student_answers);
        });
    });
}
