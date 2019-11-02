var __extends = (this && this.__extends) || (function () {
    var extendStatics = function (d, b) {
        extendStatics = Object.setPrototypeOf ||
            ({ __proto__: [] } instanceof Array && function (d, b) { d.__proto__ = b; }) ||
            function (d, b) { for (var p in b) if (b.hasOwnProperty(p)) d[p] = b[p]; };
        return extendStatics(d, b);
    };
    return function (d, b) {
        extendStatics(d, b);
        function __() { this.constructor = d; }
        d.prototype = b === null ? Object.create(b) : (__.prototype = b.prototype, new __());
    };
})();
var CanvasRenderer = /** @class */ (function () {
    function CanvasRenderer(canvas) {
        this.canvas = canvas;
        this.initializeCanvasContextWithDPI(1000, 1500);
        this.pages = [];
    }
    CanvasRenderer.prototype.initializeCanvasContextWithDPI = function (width, height) {
        var devicePixelRatio = window.devicePixelRatio || 1;
        this.canvas.width = width * devicePixelRatio;
        this.canvas.height = height * devicePixelRatio;
        this.canvas.style.width = width + "px";
        this.canvas.style.height = height + "px";
        this.ctx = this.canvas.getContext("2d");
        this.ctx.scale(devicePixelRatio, devicePixelRatio);
    };
    CanvasRenderer.prototype.createPages = function (pages) {
        for (var i = 0; i < pages; i++) {
            this.pages.push([]);
        }
    };
    CanvasRenderer.prototype.addImageToPage = function (page, x, y, url, layer) {
        var image = new CanvasImage(x, y, url, layer);
        this.pages[page].push(image);
        return image;
    };
    CanvasRenderer.prototype.addRectangleToPage = function (page, x, y, width, height, color, outline_color, label, layer) {
        var rectangle = new CanvasRectangle(x, y, width, height, color, outline_color, label, layer);
        this.pages[page].push(rectangle);
        return rectangle;
    };
    CanvasRenderer.prototype.renderPage = function (page) {
        // Clear the canvas.
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        // Get the contents of the page to be rendered.
        var pageContents = this.pages[page];
        if (pageContents == undefined) {
            return;
        }
        // Sort the contents by layer.
        pageContents.sort(function (a, b) {
            if (a.getLayer() < b.getLayer()) {
                return -1;
            }
            else if (a.getLayer() > b.getLayer()) {
                return 1;
            }
            return 0;
        });
        // Draw the contents in their layer-sorted order.
        for (var _i = 0, pageContents_1 = pageContents; _i < pageContents_1.length; _i++) {
            var content = pageContents_1[_i];
            content.render(this.ctx);
        }
    };
    return CanvasRenderer;
}());
var CanvasDrawable = /** @class */ (function () {
    function CanvasDrawable() {
    }
    return CanvasDrawable;
}());
var CanvasRectangle = /** @class */ (function (_super) {
    __extends(CanvasRectangle, _super);
    function CanvasRectangle(x, y, width, height, color, outline_color, label, layer) {
        var _this = _super.call(this) || this;
        _this.x = x;
        _this.y = y;
        _this.width = width;
        _this.height = height;
        _this.color = color;
        _this.outline_color = outline_color;
        _this.label = label;
        _this.layer = layer;
        return _this;
    }
    CanvasRectangle.prototype.getX = function () {
        return this.x;
    };
    CanvasRectangle.prototype.getY = function () {
        return this.y;
    };
    CanvasRectangle.prototype.getWidth = function () {
        return this.width;
    };
    CanvasRectangle.prototype.getHeight = function () {
        return this.height;
    };
    CanvasRectangle.prototype.setWidth = function (width) {
        this.width = width;
    };
    CanvasRectangle.prototype.setHeight = function (height) {
        this.height = height;
    };
    CanvasRectangle.prototype.setColor = function (color) {
        this.color = color;
    };
    CanvasRectangle.prototype.setOutlineColor = function (outline_color) {
        this.outline_color = outline_color;
    };
    CanvasRectangle.prototype.getLayer = function () {
        return this.layer;
    };
    CanvasRectangle.prototype.setX = function (x) {
        this.x = x;
    };
    CanvasRectangle.prototype.setY = function (y) {
        this.y = y;
    };
    CanvasRectangle.prototype.render = function (ctx) {
        // Draw the inside of the rectangle.
        ctx.fillStyle = this.color;
        ctx.fillRect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        // Draw the border of the rectangle.
        ctx.beginPath();
        ctx.strokeStyle = this.outline_color;
        ctx.lineWidth = 3;
        ctx.setLineDash([10, 10]);
        ctx.rect(this.getX(), this.getY(), this.getWidth(), this.getHeight());
        ctx.stroke();
        // Draw the text of the rectangle.
        ctx.font = "25px Verdana";
        ctx.fillStyle = this.outline_color;
        ctx.fillText(this.label, this.getX() + this.getWidth() / 2, this.getY() + this.getHeight() / 2);
    };
    return CanvasRectangle;
}(CanvasDrawable));
var CanvasImage = /** @class */ (function (_super) {
    __extends(CanvasImage, _super);
    function CanvasImage(x, y, url, layer) {
        var _this = _super.call(this) || this;
        _this.x = x;
        _this.y = y;
        _this.layer = layer;
        _this.image = new Image();
        _this.image.src = url;
        _this.loaded = false;
        $(_this.image).on("load", _this.setLoadedTrue);
        return _this;
    }
    CanvasImage.prototype.setLoadedTrue = function () {
        this.loaded = true;
    };
    CanvasImage.prototype.getImage = function () {
        return this.image;
    };
    CanvasImage.prototype.getX = function () {
        return this.x;
    };
    CanvasImage.prototype.getY = function () {
        return this.y;
    };
    CanvasImage.prototype.getWidth = function () {
        return this.image.naturalWidth;
    };
    CanvasImage.prototype.getHeight = function () {
        return this.image.naturalHeight;
    };
    CanvasImage.prototype.getLayer = function () {
        return this.layer;
    };
    CanvasImage.prototype.render = function (ctx) {
        ctx.drawImage(this.getImage(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
    };
    return CanvasImage;
}(CanvasDrawable));
var TestQuestion = /** @class */ (function () {
    function TestQuestion(points, page, regions) {
        this.points = points;
        this.page = page;
        this.regions = regions;
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
    return TestQuestion;
}());
var active_selection_color = "rgba(0, 0, 255, 0.3)";
var active_selection_outline_color = "rgb(0, 0, 255)";
var inactive_selection_color = "rgba(0, 0, 0, 0.3)";
var inactive_selection_outline_color = "rgb(0, 0, 0)";
var page_render_dpi = 100;
var number_of_pages = 0;
var current_page = 0;
var current_selection = null;
var questions = [];
function disableOrEnableButtons() {
    var previous_page_button = $("#previous_page_button");
    var next_page_button = $("#next_page_button");
    if (current_page == 0) {
        previous_page_button.prop("disabled", true);
        previous_page_button.addClass("disabled");
    }
    else {
        previous_page_button.prop("disabled", false);
        previous_page_button.removeClass("disabled");
    }
    if (current_page == number_of_pages - 1) {
        next_page_button.prop("disabled", true);
        next_page_button.addClass("disabled");
    }
    else {
        next_page_button.prop("disabled", false);
        next_page_button.removeClass("disabled");
    }
}
function nextPage() {
    if (current_page + 1 <= number_of_pages - 1) {
        current_page++;
        disableOrEnableButtons();
    }
}
function previousPage() {
    if (current_page - 1 >= 0) {
        current_page--;
        disableOrEnableButtons();
    }
}
jQuery(function ($) {
    // Get canvas and set up renderer.
    var canvas = $("canvas")[0];
    var renderer = new CanvasRenderer(canvas);
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Put the test ID into a hidden input to be sent back to the server.
    $("#test_id").val(test_id.toString());
    // Set the renderer to render the test (with answers).
    // Get the number of pages in the PDF of the test with answers.
    // Once the number of pages has been retrieved, then start downloading rendered pages.
    $.get("/render_test?test_id=" + test_id + "&answers=true&get_number_of_pages=true", function (response) {
        number_of_pages = parseInt(response);
        renderer.createPages(number_of_pages);
        for (var i = 0; i < number_of_pages; i++) {
            renderer.addImageToPage(i, 0, 0, "/render_test?test_id=" + test_id + "&answers=true&page=" + i + "&dpi=" + page_render_dpi, 0);
        }
    });
    // Continuously re-render the canvas at 30 fps.
    setInterval(function () {
        renderer.renderPage(current_page);
    }, (1.0 / 30.0) * 1000.0);
    // Set up page change buttons.
    $("#next_page_button").on("click", nextPage);
    $("#previous_page_button").on("click", previousPage);
    // Set up mousedown handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousedown", function (event) {
        current_selection = renderer.addRectangleToPage(current_page, event.offsetX, event.offsetY, 0, 0, active_selection_color, active_selection_outline_color, "Q" + (questions.length + 1), 1);
    });
    // Set up mousemove handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousemove", function (event) {
        if (current_selection != null) {
            current_selection.setWidth(event.offsetX - current_selection.getX());
            current_selection.setHeight(event.offsetY - current_selection.getY());
        }
    });
    // Set up mouseup handler on the canvas for the click-and-drag selection.
    $(canvas).on("mouseup", function () {
        current_selection.setColor(inactive_selection_color);
        current_selection.setOutlineColor(inactive_selection_outline_color);
        var test_question = new TestQuestion("1.0", current_page, [current_selection]);
        questions.push(test_question);
        $("#current_question_number").text("question " + (questions.length + 1));
        $("#no_questions_p").hide();
        $(".submit_button").show();
        $("#questions_table").append("\n        <tr>\n            <td>" + questions.length + "</td>\n            <td><input type=\"text\" value=\"1.0\" class=\"point_value_input\" id=\"point_value_input_" + questions.length + "\" /></td>\n            <td><input type=\"checkbox\" id=\"extra_credit_input_" + questions.length + "\" /></td>\n        </tr>");
        $("#point_value_input_" + questions.length).on("focus", function (event) {
            var target = event.target;
            if (target.value == target.defaultValue) {
                target.value = "";
            }
        });
        $("#point_value_input_" + questions.length).on("input", null, test_question, function (event) {
            var pointValue = $(event.target).val();
            if (!$.isNumeric(pointValue)) {
                $(event.target).addClass("wrong");
                $(".submit_button").addClass("disabled");
                $(".submit_button").attr("disabled", "disabled");
                pointValue = "1.0";
            }
            else {
                $(event.target).removeClass("wrong");
                $(".submit_button").removeClass("disabled");
                $(".submit_button").removeAttr("disabled");
            }
            event.data.setPoints(pointValue);
        });
        current_selection = null;
    });
    // Set up form submit handler that adds the selected region's data (the questions that the user selected with the mouse) to a hidden input that will be sent to the server.
    $(".submit_button").on("click", function (event) {
        for (var _i = 0, questions_1 = questions; _i < questions_1.length; _i++) {
            var question = questions_1[_i];
            for (var _a = 0, _b = question.getRegions(); _a < _b.length; _a++) {
                var region = _b[_a];
                region.setX(region.getX() / page_render_dpi);
                region.setY(region.getY() / page_render_dpi);
                region.setWidth(region.getWidth() / page_render_dpi);
                region.setHeight(region.getHeight() / page_render_dpi);
            }
        }
        $("#test_questions_json").val(JSON.stringify(questions));
        $("#test_id").val(test_id.toString());
        $("#questions_form").trigger("submit");
    });
});
