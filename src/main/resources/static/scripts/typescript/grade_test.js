jQuery(function ($) {
    // Get the test ID of the test being created.
    var raw_url = window.location.href;
    var url = new URL(raw_url);
    var test_id = parseInt(url.searchParams.get("test_id"));
    // Set up the object that handles viewing of the PDF of the test.
    var canvas = $("#question_canvas")[0];
    var renderer = new CanvasRenderer(canvas);
    // Retrieve questions on this test from the database.
    retrieveQuestions(test_id).then(function (test_questions) {
    });
});
