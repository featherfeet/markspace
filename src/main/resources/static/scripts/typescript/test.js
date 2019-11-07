jQuery(function ($) {
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    var canvas = $("canvas")[0];
    var next_page_button = $("#next_page_button")[0];
    var previous_page_button = $("#previous_page_button")[0];
    var test_viewer = new TestViewer(canvas, next_page_button, previous_page_button, test_id, page_render_dpi, page_render_fps);
});
