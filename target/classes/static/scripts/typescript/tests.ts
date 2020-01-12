jQuery(function(): void {
    $.each($(".delete_test_button"), function(index, delete_test_button) {
        $(delete_test_button).on("click", function(event) {
            const test_id: number = parseInt($(delete_test_button).attr("data-test-id"));
            $(delete_test_button).css("background-color", "red");
            if (confirm("Delete this test (warning: this cannot be undone)?")) {
                $.get(`/delete_test?test_id=${test_id}`);
                $(`#test_row_${test_id}`).remove();
            }
        });
    });
});