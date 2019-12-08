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

    constructor(canvas: HTMLCanvasElement, width: number, height: number) {
        this.canvas = canvas;
        this.initializeCanvasContextWithDPI(width, height);
        this.pages = [];
    }

    emptyPage(page: number): void {
        this.pages[page] = new Array<CanvasDrawable>();
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

    addCanvasRectangleToPage(page: number, canvas_rectangle: CanvasRectangle): void {
        this.pages[page].push(canvas_rectangle);
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

    equals(other: CanvasRectangle): boolean {
        return other.x == this.x && other.y == this.y && other.layer == this.layer && other.width == this.width && other.height == this.height && other.color == this.color && other.outline_color == this.outline_color && other.label == this.label;
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

    getLabel(): string {
        return this.label;
    }

    setLabel(label: string): void {
        this.label = label;
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
