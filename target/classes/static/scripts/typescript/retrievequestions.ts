function retrieveQuestions(test_id: number): Promise<TestQuestion[]> {
    return new Promise<TestQuestion[]>((resolve, reject) => {
        $.getJSON(`/get_questions?test_id=${test_id}`, function(data) {
            const test_questions: TestQuestion[] = new Array<TestQuestion>();
            for (let test_question_raw of data) {
                test_questions.push(TestQuestion.fromRawObject(test_question_raw));
            }
            resolve(test_questions);
        });
    });
}
