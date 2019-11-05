class TestQuestion {
    private page: number;
    private points: string;
    private regions: CanvasRectangle[];
    private extra_credit: boolean;

    constructor(points: string, page: number, regions: CanvasRectangle[], extra_credit: boolean) {
        this.points = points;
        this.page = page;
        this.regions = regions;
        this.extra_credit = extra_credit;
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

    getExtraCredit(): boolean {
        return this.extra_credit;
    }

    setExtraCredit(extra_credit: boolean): void {
        this.extra_credit = extra_credit;
    }
}
