jQuery(function($): void {
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("#question_canvas")[0];
    const renderer: CanvasRenderer = new CanvasRenderer(canvas, 1000, 400);
    let student_answer_image: CanvasRenderer = null;
    // Retrieve questions on this test from the database.
    retrieveQuestions(test_id).then(function(test_questions: TestQuestion[]) {
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
                student_answer_image = renderer.addImageToPage(0, 0, 0, `/render_student_answer`, 0);
                console.log(`Switching to the question at index ${event.data}.`);
            });
            test_question_index++;
        }
    });
});
