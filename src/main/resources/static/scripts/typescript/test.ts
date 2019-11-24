jQuery(function($): void {
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("canvas")[0];
    const next_page_button: HTMLButtonElement = <HTMLButtonElement> $("#next_page_button")[0];
    const previous_page_button: HTMLButtonElement = <HTMLButtonElement> $("#previous_page_button")[0];
    const test_viewer: TestViewer = new TestViewer(canvas, next_page_button, previous_page_button, test_id, page_render_dpi, page_render_fps);
    const canvas_renderer: CanvasRenderer = test_viewer.getRenderer();
    // Retrieve questions on this test from the database.
    retrieveQuestions(test_id).then(function(test_questions) {
        let max_page_number: number = 0;
        for (let test_question of test_questions) {
            if (test_question.getPage() > max_page_number) {
                max_page_number = test_question.getPage();
            }
        }
        canvas_renderer.createPages(max_page_number + 1);
        for (let test_question of test_questions) {
            for (let region of test_question.getRegions()) {
                canvas_renderer.addCanvasRectangleToPage(test_question.getPage(), region);
            }
        }
    });
});