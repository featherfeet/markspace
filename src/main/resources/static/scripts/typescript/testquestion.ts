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
