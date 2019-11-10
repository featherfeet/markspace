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
    var test_questions = new Array();
    $.getJSON("/get_questions?test_id=" + test_id, function (data) {
        for (var _i = 0, data_1 = data; _i < data_1.length; _i++) {
            var test_question_raw = data_1[_i];
            var regions = new Array();
            for (var _a = 0, _b = test_question_raw.regions; _a < _b.length; _a++) {
                var region_raw = _b[_a];
                var region = new CanvasRectangle(region_raw.x * page_render_dpi, region_raw.y * page_render_dpi, region_raw.width * page_render_dpi, region_raw.height * page_render_dpi, region_raw.color, region_raw.outline_color, region_raw.label, region_raw.layer);
                regions.push(region);
            }
            var test_question = new TestQuestion(test_question_raw.points, test_question_raw.page, regions, test_question_raw.extra_credit);
            test_questions.push(test_question);
        }
        var max_page_number = 0;
        for (var _c = 0, test_questions_1 = test_questions; _c < test_questions_1.length; _c++) {
            var test_question = test_questions_1[_c];
            if (test_question.getPage() > max_page_number) {
                max_page_number = test_question.getPage();
            }
        }
        canvas_renderer.createPages(max_page_number + 1);
        for (var _d = 0, test_questions_2 = test_questions; _d < test_questions_2.length; _d++) {
            var test_question = test_questions_2[_d];
            for (var _e = 0, _f = test_question.getRegions(); _e < _f.length; _e++) {
                var region = _f[_e];
                canvas_renderer.addCanvasRectangleToPage(test_question.getPage(), region);
            }
        }
    });
});
