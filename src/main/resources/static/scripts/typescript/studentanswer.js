var StudentAnswer = /** @class */ (function () {
    function StudentAnswer(student_answer_id, student_answer_file_id, test_question, score, points_possible, page) {
        this.student_answer_id = student_answer_id;
        this.student_answer_file_id = student_answer_file_id;
        this.test_question = test_question;
        this.score = score;
        this.points_possible = points_possible;
        this.page = page;
    }
    StudentAnswer.prototype.getStudentAnswerId = function () {
        return this.student_answer_id;
    };
    StudentAnswer.prototype.setStudentAnswerId = function (student_answer_id) {
        this.student_answer_id = student_answer_id;
    };
    StudentAnswer.prototype.getStudentAnswerFileId = function () {
        return this.student_answer_file_id;
    };
    StudentAnswer.prototype.setStudentAnswerFileId = function (student_answer_file_id) {
        this.student_answer_file_id = student_answer_file_id;
    };
    StudentAnswer.prototype.getTestQuestion = function () {
        return this.test_question;
    };
    StudentAnswer.prototype.setTestQuestion = function (test_question) {
        this.test_question = test_question;
    };
    StudentAnswer.prototype.getScore = function () {
        return this.score;
    };
    StudentAnswer.prototype.setScore = function (score) {
        this.score = score;
    };
    StudentAnswer.prototype.getPointsPossible = function () {
        return this.points_possible;
    };
    StudentAnswer.prototype.setPointsPossible = function (points_possible) {
        this.points_possible = points_possible;
    };
    StudentAnswer.prototype.getPage = function () {
        return this.page;
    };
    StudentAnswer.prototype.setPage = function (page) {
        this.page = page;
    };
    StudentAnswer.fromRawObject = function (student_answer_raw) {
        // @ts-ignore
        var test_question = TestQuestion.fromRawObject(student_answer_raw.test_question);
        // @ts-ignore
        return new StudentAnswer(student_answer_raw.student_answer_id, student_answer_raw.student_answer_file_id, test_question, student_answer_raw.score, student_answer_raw.points_possible, student_answer_raw.page);
    };
    StudentAnswer.prototype.getImageURLs = function () {
        var image_urls = new Array();
        for (var _i = 0, _a = this.test_question.getRegions(); _i < _a.length; _i++) {
            var region = _a[_i];
            var image_url = "/render_student_answer?page=" + this.page + "&student_answer_file_id=" + this.student_answer_file_id + "&x=" + region.getX() + "&y=" + region.getY() + "&width=" + region.getWidth() + "&height=" + region.getHeight() + "&dpi=" + page_render_dpi;
            image_urls.push(image_url);
        }
        return image_urls;
    };
    StudentAnswer.prototype.renderCanvasImages = function (renderer, page, layer) {
        var canvas_images = new Array();
        var current_y = 0;
        for (var _i = 0, _a = this.test_question.getRegions(); _i < _a.length; _i++) {
            var region = _a[_i];
            var image_url = "/render_student_answer?page=" + this.page + "&student_answer_file_id=" + this.student_answer_file_id + "&x=" + region.getX() + "&y=" + region.getY() + "&width=" + region.getWidth() + "&height=" + region.getHeight() + "&dpi=" + page_render_dpi;
            canvas_images.push(renderer.addImageToPage(page, 0, current_y, image_url, layer));
            current_y += region.getY() * page_render_dpi;
        }
        return canvas_images;
    };
    return StudentAnswer;
}());
