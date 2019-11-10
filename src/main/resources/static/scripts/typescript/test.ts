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
    const test_questions: TestQuestion[] = new Array<TestQuestion>();
    $.getJSON(`/get_questions?test_id=${test_id}`, function(data) {
        for (let test_question_raw of data) {
            const regions: CanvasRectangle[] = new Array<CanvasRectangle>();
            for (let region_raw of test_question_raw.regions) {
                const region: CanvasRectangle = new CanvasRectangle(region_raw.x * page_render_dpi, region_raw.y * page_render_dpi, region_raw.width * page_render_dpi, region_raw.height * page_render_dpi, region_raw.color, region_raw.outline_color, region_raw.label, region_raw.layer);
                regions.push(region);
            }
            const test_question: TestQuestion = new TestQuestion(test_question_raw.points, test_question_raw.page, regions, test_question_raw.extra_credit);
            test_questions.push(test_question);
        }
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