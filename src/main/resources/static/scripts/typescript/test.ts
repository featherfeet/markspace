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
});