package storage;

public class StudentAnswer {
    private int student_answer_file_id;
    private TestQuestion test_question;
    private String score;
    private String points_possible;
    private int page;

    public StudentAnswer(int student_answer_file_id, TestQuestion test_question, String score, String points_possible, int page) {
        this.student_answer_file_id = student_answer_file_id;
        this.test_question = test_question;
        this.score = score;
        this.points_possible = points_possible;
        this.page = page;
    }

    public int getStudentAnswerFileId() {
        return student_answer_file_id;
    }

    public void setStudentAnswerFileId(int student_answer_file_id) {
        this.student_answer_file_id = student_answer_file_id;
    }

    public TestQuestion getTestQuestion() {
        return test_question;
    }

    public void setTestQuestion(TestQuestion test_question) {
        this.test_question = test_question;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public String getPointsPossible() {
        return points_possible;
    }

    public void setPointsPossible(String points_possible) {
        this.points_possible = points_possible;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }
}
