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
    var canvas_renderer = test_viewer.getRenderer();
    // Retrieve questions on this test from the database.
    retrieveQuestions(test_id).then(function (test_questions) {
        var max_page_number = 0;
        for (var _i = 0, test_questions_1 = test_questions; _i < test_questions_1.length; _i++) {
            var test_question = test_questions_1[_i];
            if (test_question.getPage() > max_page_number) {
                max_page_number = test_question.getPage();
            }
        }
        canvas_renderer.createPages(max_page_number + 1);
        for (var _a = 0, test_questions_2 = test_questions; _a < test_questions_2.length; _a++) {
            var test_question = test_questions_2[_a];
            for (var _b = 0, _c = test_question.getRegions(); _b < _c.length; _b++) {
                var region = _c[_b];
                canvas_renderer.addCanvasRectangleToPage(test_question.getPage(), region);
            }
        }
    });
});
