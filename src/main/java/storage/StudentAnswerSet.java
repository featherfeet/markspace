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
