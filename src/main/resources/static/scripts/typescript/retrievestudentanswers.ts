function retrieveStudentAnswers(test_id: number, student_answer_file_id: number): Promise<StudentAnswer[]> {
    return new Promise<StudentAnswer[]>((resolve, reject) => {
        $.getJSON(`/generate_student_answers_for_test?test_id=${test_id}&student_answer_file_id=${student_answer_file_id}`, function(data) {
            const student_answers: StudentAnswer[] = new Array<StudentAnswer>();
            for (let student_answer_raw of data) {
                const student_answer: StudentAnswer = StudentAnswer.fromRawObject(student_answer_raw);
                student_answers.push(student_answer);
            }
            resolve(student_answers);
        });
    });
}