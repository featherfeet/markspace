jQuery(function($): void {
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("#question_canvas")[0];
    const renderer: CanvasRenderer = new CanvasRenderer(canvas, 1000, 1500);
    renderer.createPages(1);
    setInterval(function() {
        renderer.renderPage(0);
    }, (1.0 / page_render_fps * 1000.0));
    let current_student_answer_images: CanvasImage[] = new Array<CanvasImage>();
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
        for (let test_question of test_questions) {
            // Create a link at the bottom of the screen to this test question.
            const question_link: JQuery<HTMLAnchorElement> = $(`<a class="buttonlike test_question_a">${test_question.getRegions()[0].getLabel()}</a>`);
            questions_div.append(question_link);
            // If the link is clicked, jump to that question.
            question_link.on("click", null, test_question_index, function(event) {
                current_test_question = test_question_index;
                // TODO:
                for (let student_answer of student_answers) {
                    console.table(student_answer.getTestQuestion());
                    console.table(test_question);
                    if (student_answer.getTestQuestion().equals(test_question)) {
                        renderer.emptyPage(0);
                        current_student_answer_images = student_answer.renderCanvasImages(renderer, 0, 0);
                        break;
                    }
                }
                console.log(`Switching to the question at index ${event.data}.`);
            });
            test_question_index++;
        }
    });
});
