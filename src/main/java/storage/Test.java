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

public class Test {
    private int id;
    private String name;
    private String description;
    private int blank_test_file_id;
    private int answers_test_file_id;

    public Test(int id, String name, String description, int blank_test_file_id, int answers_test_file_id) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.blank_test_file_id = blank_test_file_id;
        this.answers_test_file_id = answers_test_file_id;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getBlankTestFile() {
        return blank_test_file_id;
    }

    public int getAnswersTestFile() {
        return answers_test_file_id;
    }
}
