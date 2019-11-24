function retrieveQuestions(test_id: number): Promise<TestQuestion[]> {
    return new Promise<TestQuestion[]>((resolve, reject) => {
        $.getJSON(`/get_questions?test_id=${test_id}`, function(data) {
            const test_questions: TestQuestion[] = new Array<TestQuestion>();
            for (let test_question_raw of data) {
                const regions: CanvasRectangle[] = new Array<CanvasRectangle>();
                for (let region_raw of test_question_raw.regions) {
                    const region: CanvasRectangle = new CanvasRectangle(region_raw.x * page_render_dpi, region_raw.y * page_render_dpi, region_raw.width * page_render_dpi, region_raw.height * page_render_dpi, region_raw.color, region_raw.outline_color, region_raw.label, region_raw.layer);
                    regions.push(region);
                }
                const test_question: TestQuestion = new TestQuestion(test_question_raw.points, test_question_raw.page, regions, test_question_raw.extra_credit);
                test_questions.push(test_question);
            }
            resolve(test_questions);
        });
    });
}
