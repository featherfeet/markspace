class CanvasRenderer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private pages: CanvasDrawable[][];

    private initializeCanvasContextWithDPI(width: number, height: number): void {
        const devicePixelRatio: number = window.devicePixelRatio || 1;
        this.canvas.width = width * devicePixelRatio;
        this.canvas.height = height * devicePixelRatio;
        this.canvas.style.width = `${width}px`;
        this.canvas.style.height = `${height}px`;
        this.ctx = this.canvas.getContext("2d");
        this.ctx.scale(devicePixelRatio, devicePixelRatio);
    }

    constructor(canvas: HTMLCanvasElement) {
        this.canvas = canvas;
        this.initializeCanvasContextWithDPI(1000, 1500);
        this.pages = [];
    }

    createPages(pages: number): void {
        for (let i: number = 0; i < pages; i++) {
            this.pages.push([]);
        }
    }

    addImageToPage(page: number, x: number, y: number, url: string, layer: number): CanvasImage {
        const image: CanvasImage = new CanvasImage(x, y, url, layer);
        this.pages[page].push(image);
        return image;
    }

    addRectangleToPage(page: number, x: number, y: number, width: number, height: number, color: string, outline_color: string, label: string, layer: number): CanvasRectangle {
        const rectangle: CanvasRectangle = new CanvasRectangle(x, y, width, height, color, outline_color, label, layer);
        this.pages[page].push(rectangle);
        return rectangle;
    }

    renderPage(page: number): void {
        // Clear the canvas.
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Get the contents of the page to be rendered.
        const pageContents: CanvasDrawable[] = this.pages[page];
        if (pageContents == undefined) {
            return;
        }

        // Sort the contents by layer.
        pageContents.sort(function(a: CanvasDrawable, b: CanvasDrawable): number {
            if (a.getLayer() < b.getLayer()) {
                return -1;
            }
            else if (a.getLayer() > b.getLayer()) {
                return 1;
            }
            return 0;
        });

        // Draw the contents in their layer-sorted order.
        for (let content of pageContents) {
            content.render(this.ctx);
        }
    }
}

abstract class CanvasDrawable {
    abstract getX(): number;
    abstract getY(): number;
    abstract getWidth(): number;
    abstract getHeight(): number;
    abstract getLayer(): number;
    abstract render(ctx: CanvasRenderingContext2D): void;
}

class CanvasRectangle extends CanvasDrawable {
    private x: number;
    private y: number;
    private readonly layer: number;
    private width: number;
    private height: number;
    private color: string;
    private outline_color: string;
    private label: string;

    constructor(x: number, y: number, width: number, height: number, color: string, outline_color: string, label: string, layer: number) {
        super();
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.outline_color = outline_color;
        this.label = label;
        this.layer = layer;
    }

    getX(): number {
        return this.x;
    }

    getY(): number {
        return this.y;
    }

    getWidth(): number {
        return this.width;
    }

    getHeight(): number {
        return this.height;
    }

    setWidth(width: number): void {
        this.width = width;
    }

    setHeight(height: number): void {
        this.height = height;
    }

    setColor(color: string): void {
        this.color = color;
    }

    setOutlineColor(outline_color: string): void {
        this.outline_color = outline_color;
    }

    getLayer(): number {
        return this.layer;
    }

    setX(x: number) {
        this.x = x;
    }

    setY(y: number) {
        this.y = y;
    }

    render(ctx: CanvasRenderingContext2D): void {
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
    }
}

class CanvasImage extends CanvasDrawable {
    private readonly image: HTMLImageElement;
    private readonly x: number;
    private readonly y: number;
    private readonly layer: number;
    private loaded: boolean;

    private setLoadedTrue(): void {
        this.loaded = true;
    }

    constructor(x: number, y: number, url: string, layer: number) {
        super();
        this.x = x;
        this.y = y;
        this.layer = layer;
        this.image = new Image();
        this.image.src = url;
        this.loaded = false;
        $(this.image).on("load", this.setLoadedTrue);
    }

    getImage(): HTMLImageElement {
        return this.image;
    }

    getX(): number {
        return this.x;
    }

    getY(): number {
        return this.y;
    }

    getWidth(): number {
        return this.image.naturalWidth;
    }

    getHeight(): number {
        return this.image.naturalHeight;
    }

    getLayer(): number {
        return this.layer;
    }

    render(ctx: CanvasRenderingContext2D): void {
        ctx.drawImage(this.getImage(), this.getX(), this.getY(), this.getWidth(), this.getHeight());
    }
}

class TestQuestion {
    private page: number;
    private points: string;
    private regions: CanvasRectangle[];

    constructor(points: string, page: number, regions: CanvasRectangle[]) {
        this.points = points;
        this.page = page;
        this.regions = regions;
    }

    getPoints(): string {
        return this.points;
    }

    getPage(): number {
        return this.page;
    }

    setPoints(points: string): void {
        this.points = points;
    }

    getRegions(): CanvasRectangle[] {
        return this.regions;
    }

    setRegions(regions: CanvasRectangle[]): void {
        this.regions = regions;
    }
}

const active_selection_color: string = "rgba(0, 0, 255, 0.3)";
const active_selection_outline_color: string = "rgb(0, 0, 255)";
const inactive_selection_color: string = "rgba(0, 0, 0, 0.3)";
const inactive_selection_outline_color: string = "rgb(0, 0, 0)";
const page_render_dpi: number = 100;
let number_of_pages: number = 0;
let current_page: number = 0;
let current_selection: CanvasRectangle = null;
let questions: TestQuestion[] = [];

function disableOrEnableButtons(): void {
    const previous_page_button: JQuery = $("#previous_page_button");
    const next_page_button: JQuery = $("#next_page_button");
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

function nextPage(): void {
    if (current_page + 1 <= number_of_pages - 1) {
        current_page++;
        disableOrEnableButtons();
    }
}

function previousPage(): void {
    if (current_page - 1 >= 0) {
        current_page--;
        disableOrEnableButtons();
    }
}

jQuery(function($): void {
    // Get canvas and set up renderer.
    const canvas: HTMLCanvasElement = <HTMLCanvasElement> $("canvas")[0];
    const renderer: CanvasRenderer = new CanvasRenderer(canvas);
    // Get the test ID of the test being created.
    const raw_url = window.location.href;
    const url = new URL(raw_url);
    const test_id: number = parseInt(url.searchParams.get("test_id"));
    // Put the test ID into a hidden input to be sent back to the server.
    $("#test_id").val(test_id.toString());
    // Set the renderer to render the test (with answers).
    // Get the number of pages in the PDF of the test with answers.
    // Once the number of pages has been retrieved, then start downloading rendered pages.
    $.get(`/render_test?test_id=${test_id}&answers=true&get_number_of_pages=true`, function(response) {
        number_of_pages = parseInt(response);
        renderer.createPages(number_of_pages);
        for (let i: number = 0; i < number_of_pages; i++) {
            renderer.addImageToPage(i, 0, 0, `/render_test?test_id=${test_id}&answers=true&page=${i}&dpi=${page_render_dpi}`, 0);
        }
    });
    // Continuously re-render the canvas at 30 fps.
    setInterval(function() {
        renderer.renderPage(current_page);
    }, (1.0 / 30.0) * 1000.0);
    // Set up page change buttons.
    $("#next_page_button").on("click", nextPage);
    $("#previous_page_button").on("click", previousPage);
    // Set up mousedown handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousedown", function(event): void {
        current_selection = renderer.addRectangleToPage(current_page, event.offsetX, event.offsetY, 0, 0, active_selection_color, active_selection_outline_color, `Q${questions.length + 1}`,1);
    });
    // Set up mousemove handler on the canvas for the click-and-drag selection.
    $(canvas).on("mousemove", function(event): void {
        if (current_selection != null) {
            current_selection.setWidth(event.offsetX - current_selection.getX());
            current_selection.setHeight(event.offsetY - current_selection.getY());
        }
    });
    // Set up mouseup handler on the canvas for the click-and-drag selection.
    $(canvas).on("mouseup", function(): void {
        current_selection.setColor(inactive_selection_color);
        current_selection.setOutlineColor(inactive_selection_outline_color);
        const test_question: TestQuestion = new TestQuestion("1.0", current_page, [current_selection]);
        questions.push(test_question);
        $("#current_question_number").text(`question ${questions.length + 1}`);
        $("#no_questions_p").hide();
        $(".submit_button").show();
        $("#questions_table").append(`
        <tr>
            <td>${questions.length}</td>
            <td><input type="text" value="1.0" class="point_value_input" name="point_value_input_${questions.length}" /></td>
            <td><input type="checkbox" name="extra_credit_input_${questions.length}" /></td>
        </tr>`);
        $(`input[name=point_value_input_${questions.length}]`).on("focus", function(event) {
            const target: HTMLInputElement = <HTMLInputElement> event.target;
            if (target.value == target.defaultValue) {
                target.value = "";
            }
        });
        $(`input[name=point_value_input_${questions.length}]`).on("input", null, test_question, function(event) {
            let pointValue = <string> $(event.target).val();
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
    $(".submit_button").on("click", function(event) {
        for (let question of questions) {
            for (let region of question.getRegions()) {
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