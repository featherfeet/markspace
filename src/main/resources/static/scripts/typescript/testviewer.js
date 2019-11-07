var TestViewer = /** @class */ (function () {
    function TestViewer(canvas, next_page_button, previous_page_button, test_id, page_render_dpi, fps) {
        this.canvas = canvas;
        this.next_page_button = next_page_button;
        this.previous_page_button = previous_page_button;
        this.renderer = new CanvasRenderer(this.canvas);
        this.page_render_dpi = page_render_dpi;
        this.fps = fps;
        this.current_page = 0;
        this.test_id = test_id;
        var this_test_viewer = this;
        $.get("/render_test?test_id=" + test_id + "&answers=true&get_number_of_pages=true", function (response) {
            this_test_viewer.number_of_pages = parseInt(response);
            this_test_viewer.renderer.createPages(this_test_viewer.number_of_pages);
            for (var i = 0; i < this_test_viewer.number_of_pages; i++) {
                this_test_viewer.getRenderer().addImageToPage(i, 0, 0, "/render_test?test_id=" + this_test_viewer.test_id + "&answers=true&page=" + i + "&dpi=" + this_test_viewer.page_render_dpi, 0);
            }
        });
        setInterval(function () {
            this_test_viewer.renderer.renderPage(this_test_viewer.getCurrentPage());
        }, (1.0 / this.fps * 1000.0));
        $(next_page_button).on("click", null, this, this.nextPage);
        $(previous_page_button).on("click", null, this, this.previousPage);
        this.disableOrEnableButtons();
    }
    TestViewer.prototype.disableOrEnableButtons = function () {
        var previous_page_button = $(this.previous_page_button);
        var next_page_button = $(this.next_page_button);
        if (this.current_page == 0) {
            previous_page_button.prop("disabled", true);
            previous_page_button.addClass("disabled");
        }
        else {
            previous_page_button.prop("disabled", false);
            previous_page_button.removeClass("disabled");
        }
        if (this.current_page == this.number_of_pages - 1) {
            next_page_button.prop("disabled", true);
            next_page_button.addClass("disabled");
        }
        else {
            next_page_button.prop("disabled", false);
            next_page_button.removeClass("disabled");
        }
    };
    TestViewer.prototype.nextPage = function (event) {
        var this_test_viewer = event.data;
        if (this_test_viewer.current_page + 1 <= this_test_viewer.number_of_pages - 1) {
            this_test_viewer.current_page++;
            this_test_viewer.disableOrEnableButtons();
        }
    };
    TestViewer.prototype.previousPage = function (event) {
        var this_test_viewer = event.data;
        if (this_test_viewer.current_page - 1 >= 0) {
            this_test_viewer.current_page--;
            this_test_viewer.disableOrEnableButtons();
        }
    };
    TestViewer.prototype.getCurrentPage = function () {
        return this.current_page;
    };
    TestViewer.prototype.getRenderer = function () {
        return this.renderer;
    };
    return TestViewer;
}());
