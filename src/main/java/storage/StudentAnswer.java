/*
 * Copyright 2020 Oliver Trevor and Suchin Ravi.
 *
 * This file is part of MarkSpace.
 *
 * MarkSpace is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MarkSpace is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MarkSpace.  If not, see <https://www.gnu.org/licenses/>.
 */

package storage;

public class StudentAnswer {
    private int student_answer_id;
    private int student_answer_file_id;
    private String student_identification;
    private TestQuestion test_question;
    private String score;
    private String points_possible;
    private int page;

    public StudentAnswer(int student_answer_id, int student_answer_file_id, String student_identification, TestQuestion test_question, String score, String points_possible, int page) {
        this.student_answer_id = student_answer_id;
        this.student_answer_file_id = student_answer_file_id;
        this.student_identification = student_identification;
        this.test_question = test_question;
        this.score = score;
        this.points_possible = points_possible;
        this.page = page;
    }

    public int getStudentAnswerId() {
        return student_answer_id;
    }

    public void setStudentAnswerId(int student_answer_id) {
        this.student_answer_id = student_answer_id;
    }

    public String getStudentIdentification() {
        return student_identification;
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
