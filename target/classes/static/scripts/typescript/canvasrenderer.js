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
    function CanvasRenderer(canvas, width, height) {
        this.canvas = canvas;
        this.initializeCanvasContextWithDPI(width, height);
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
    CanvasRenderer.prototype.emptyPage = function (page) {
        this.pages[page] = new Array();
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
    CanvasRenderer.prototype.addCanvasRectangleToPage = function (page, canvas_rectangle) {
        this.pages[page].push(canvas_rectangle);
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
    CanvasRectangle.prototype.equals = function (other) {
        return other.x == this.x && other.y == this.y && other.layer == this.layer && other.width == this.width && other.height == this.height && other.color == this.color && other.outline_color == this.outline_color && other.label == this.label;
    };
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
    CanvasRectangle.prototype.getLabel = function () {
        return this.label;
    };
    CanvasRectangle.prototype.setLabel = function (label) {
        this.label = label;
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
        ctx.fillRect(this.getX() * page_render_dpi, this.getY() * page_render_dpi, this.getWidth() * page_render_dpi, this.getHeight() * page_render_dpi);
        // Draw the border of the rectangle.
        ctx.beginPath();
        ctx.strokeStyle = this.outline_color;
        ctx.lineWidth = 3;
        ctx.setLineDash([10, 10]);
        ctx.rect(this.getX() * page_render_dpi, this.getY() * page_render_dpi, this.getWidth() * page_render_dpi, this.getHeight() * page_render_dpi);
        ctx.stroke();
        // Draw the text of the rectangle.
        ctx.font = "25px Verdana";
        ctx.fillStyle = this.outline_color;
        ctx.textAlign = "center";
        ctx.textBaseline = "middle";
        ctx.fillText(this.label, (this.getX() + this.getWidth() / 2) * page_render_dpi, (this.getY() + this.getHeight() / 2) * page_render_dpi);
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
