class StudentAnswer {
    private student_answer_file_id: number;
    private test_question: TestQuestion;
    private score: string;
    private points_possible: string;
    private page: number;

    constructor(student_answer_file_id: number, test_question: TestQuestion, score: string, points_possible: string, page: number) {
        this.student_answer_file_id = student_answer_file_id;
        this.test_question = test_question;
        this.score = score;
        this.points_possible = points_possible;
        this.page = page;
    }

    getStudentAnswerFileId(): number {
        return this.student_answer_file_id;
    }

    setStudentAnswerFileId(student_answer_file_id: number): void {
        this.student_answer_file_id = student_answer_file_id;
    }

    getTestQuestion(): TestQuestion {
        return this.test_question;
    }

    setTestQuestion(test_question: TestQuestion): void {
        this.test_question = test_question;
    }

    getScore(): string {
        return this.score;
    }

    setScore(score: string): void {
        this.score = score;
    }

    getPointsPossible(): string {
        return this.points_possible;
    }

    setPointsPossible(points_possible: string): void {
        this.points_possible = points_possible;
    }

    getPage(): number {
        return this.page;
    }

    setPage(page: number): void {
        this.page = page;
    }

    static fromRawObject(student_answer_raw: object): StudentAnswer {
        // @ts-ignore
        const test_question: TestQuestion = TestQuestion.fromRawObject(student_answer_raw.test_question);
        // @ts-ignore
        return new StudentAnswer(student_answer_raw.student_answer_file_id, test_question, student_answer_raw.score, student_answer_raw.points_possible, student_answer_raw.page);
    }

    getImageURLs(): string[] {
        const image_urls: string[] = new Array<string>();

        for (let region of this.test_question.getRegions()) {
            const image_url: string = `/render_student_answer?page=${this.page}&student_answer_file_id=${this.student_answer_file_id}&x=${region.getX()}&y=${region.getY()}&width=${region.getWidth()}&height=${region.getHeight()}&dpi=${page_render_dpi}`;
            image_urls.push(image_url);
        }

        return image_urls;
    }

    renderCanvasImages(renderer: CanvasRenderer, page: number, layer: number): CanvasDrawable[] {
        const canvas_images: CanvasImage[] = new Array<CanvasImage>();
        let current_y: number = 0;
        for (let region of this.test_question.getRegions()) {
            const image_url: string = `/render_student_answer?page=${this.page}&student_answer_file_id=${this.student_answer_file_id}&x=${region.getX()}&y=${region.getY()}&width=${region.getWidth()}&height=${region.getHeight()}&dpi=${page_render_dpi}`;
            canvas_images.push(renderer.addImageToPage(page, 0, current_y, image_url, layer));
            current_y += region.getY() * page_render_dpi;
        }
        return canvas_images;
    }
}