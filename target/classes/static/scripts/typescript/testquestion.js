var TestQuestion = /** @class */ (function () {
    function TestQuestion(test_question_id, points, page, regions, extra_credit) {
        this.test_question_id = test_question_id;
        this.points = points;
        this.page = page;
        this.regions = regions;
        this.extra_credit = extra_credit;
    }
    TestQuestion.prototype.getTestQuestionId = function () {
        return this.test_question_id;
    };
    TestQuestion.prototype.setTestQuestionId = function (test_question_id) {
        this.test_question_id = test_question_id;
    };
    TestQuestion.prototype.getPoints = function () {
        return this.points;
    };
    TestQuestion.prototype.getPage = function () {
        return this.page;
    };
    TestQuestion.prototype.setPoints = function (points) {
        this.points = points;
    };
    TestQuestion.prototype.getRegions = function () {
        return this.regions;
    };
    TestQuestion.prototype.setRegions = function (regions) {
        this.regions = regions;
    };
    TestQuestion.prototype.getExtraCredit = function () {
        return this.extra_credit;
    };
    TestQuestion.prototype.setExtraCredit = function (extra_credit) {
        this.extra_credit = extra_credit;
    };
    TestQuestion.prototype.getImageURLs = function (test_id, answers) {
        var image_urls = new Array();
        for (var _i = 0, _a = this.regions; _i < _a.length; _i++) {
            var region = _a[_i];
            var image_url = "/render_question?page=" + this.page + "&test_id=" + test_id + "&answers=" + answers + "&x=" + region.getX() + "&y=" + region.getY() + "&width=" + region.getWidth() + "&height=" + region.getHeight() + "&dpi=" + page_render_dpi;
            image_urls.push(image_url);
        }
        return image_urls;
    };
    TestQuestion.fromRawObject = function (test_question_raw) {
        var regions = new Array();
        // @ts-ignore
        for (var _i = 0, _a = test_question_raw.regions; _i < _a.length; _i++) {
            var region_raw = _a[_i];
            var region = new CanvasRectangle(region_raw.x, region_raw.y, region_raw.width, region_raw.height, region_raw.color, region_raw.outline_color, region_raw.label, region_raw.layer);
            regions.push(region);
        }
        // @ts-ignore
        var test_question = new TestQuestion(test_question_raw.test_question_id, test_question_raw.points, test_question_raw.page, regions, test_question_raw.extra_credit);
        return test_question;
    };
    return TestQuestion;
}());
