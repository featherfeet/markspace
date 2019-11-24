jQuery(function($): void {
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("#question_canvas")[0];
    const renderer: CanvasRenderer = new CanvasRenderer(canvas);
    // Retrieve questions on this test from the database.
    retrieveQuestions(test_id).then(function(test_questions) {

    });
});