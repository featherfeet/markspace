var TestQuestion = /** @class */ (function () {
    function TestQuestion(points, page, regions, extra_credit) {
        this.points = points;
        this.page = page;
        this.regions = regions;
        this.extra_credit = extra_credit;
    }
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
    return TestQuestion;
}());
