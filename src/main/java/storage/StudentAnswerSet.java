package storage;

public class StudentAnswerSet {
    private int student_answer_set_id;
    private int user_id;
    private int test_id;
    private Integer[] student_answer_ids;

    public StudentAnswerSet(int student_answer_set_id, int user_id, int test_id, Integer[] student_answer_ids) {
        this.student_answer_set_id = student_answer_set_id;
        this.user_id = user_id;
        this.test_id = test_id;
        this.student_answer_ids = student_answer_ids;
    }

    public int getStudentAnswerSetId() {
        return student_answer_set_id;
    }

    public int getUserId() {
        return user_id;
    }

    public int getTestId() {
        return test_id;
    }

    public Integer[] getStudentAnswerIds() {
        return student_answer_ids;
    }
}
