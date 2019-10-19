export class CanvasRenderer {
    private canvas: HTMLCanvasElement;
    private ctx: CanvasRenderingContext2D;
    private pages: CanvasDrawable[][];

    constructor(canvas: HTMLCanvasElement) {
        this.canvas = canvas;
        this.ctx = canvas.getContext("2d");
    }

    addImageToPage(page: number, x: number, y: number, url: string, layer: number): void {
        this.pages[page].push(new CanvasImage(x, y, url, layer));
    }

    renderPage(page: number): void {
        // Clear the canvas.
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);

        // Get the contents of the page to be rendered.
        const pageContents: CanvasDrawable[] = this.pages[page];

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

export abstract class CanvasDrawable {
    abstract getX(): number;
    abstract getY(): number;
    abstract getWidth(): number;
    abstract getHeight(): number;
    abstract getLayer(): number;
    abstract render(ctx: CanvasRenderingContext2D): void;
}

export class CanvasImage extends CanvasDrawable {
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
        this.image.addEventListener("load", this.setLoadedTrue, false);
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