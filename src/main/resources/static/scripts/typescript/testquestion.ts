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

    equals(other: TestQuestion): boolean {
        if (other.getRegions().length != this.regions.length) {
            return false;
        }
        for (let i = 0; i < other.getRegions().length; i++) {
            if (!other.getRegions()[i].equals(this.regions[i])) {
                return false;
            }
        }
        return other.page == this.page && other.points == this.points && other.extra_credit == this.extra_credit;
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

    getImageURLs(test_id: number, answers: boolean): string[] {
        const image_urls: string[] = new Array<string>();
        for (let region of this.regions) {
            const image_url: string = `/render_question?page=${this.page}&test_id=${test_id}&answers=${answers}&x=${region.getX()}&y=${region.getY()}&width=${region.getWidth()}&height=${region.getHeight()}&dpi=${page_render_dpi}`;
            image_urls.push(image_url);
        }
        return image_urls;
    }

    static fromRawObject(test_question_raw: object): TestQuestion {
        const regions: CanvasRectangle[] = new Array<CanvasRectangle>();
        // @ts-ignore
        for (let region_raw of test_question_raw.regions) {
            const region: CanvasRectangle = new CanvasRectangle(region_raw.x, region_raw.y, region_raw.width, region_raw.height, region_raw.color, region_raw.outline_color, region_raw.label, region_raw.layer);
            regions.push(region);
        }
        // @ts-ignore
        const test_question: TestQuestion = new TestQuestion(test_question_raw.points, test_question_raw.page, regions, test_question_raw.extra_credit);
        return test_question;
    }
}
