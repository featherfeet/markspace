function retrieveQuestions(test_id) {
    return new Promise(function (resolve, reject) {
        $.getJSON("/get_questions?test_id=" + test_id, function (data) {
            var test_questions = new Array();
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
            resolve(test_questions);
        });
    });
}
