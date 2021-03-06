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

CREATE TABLE IF NOT EXISTS schema_version_data                -- The schema_version_data table specifies which revision this database schema is.
(
    schema_version INT
);

CREATE UNIQUE INDEX schema_unique_index ON schema_version_data (schema_version); -- There can only ever be one row in the schema_version_data table.

INSERT OR IGNORE INTO schema_version_data VALUES (1);         -- Schema revision 1.

CREATE TABLE IF NOT EXISTS users                              -- The users table holds a record of all users of the application.
(
    user_id INTEGER PRIMARY KEY,                                  -- Each user has a unique integer identification number.
    username VARCHAR(255),                                        -- The username used to log in as this user.
    password_hash BINARY(64),                                     -- The hash (PBKDF2WithHmacSHA512) of the password used to log in as this user.
    password_salt BINARY(64),                                     -- The salt (64 random bytes) used for the password hash.
    email VARCHAR(255),                                           -- The email used to send notifications/password change links to this user.
    permissions VARCHAR(255)                                      -- A string containing a semicolon-delimited list of permissions from UserPermission.java. Example: "CREATE_TEST;GRADE_TEST".
);

CREATE TABLE IF NOT EXISTS tests                              -- The tests table holds all tests (that can be administered to students) in the application.
(
    test_id          INTEGER PRIMARY KEY,                         -- Each test has a unique identification number.
    user_id          INT,                                         -- Id of the user who created the test.
    test_name        VARCHAR(1024),                               -- The title of the test that is shown to the user.
    test_description LONGTEXT,                                    -- A user-writable description of the test.
    blank_test_file   INT,                                        -- The identification number (see the test_files table) of the file containing the test WITHOUT correct answers filled in.
    answers_test_file INT                                         -- The identification number (see the test_files table) of the file containing the test WITH correct answers filled in.
);

CREATE TABLE IF NOT EXISTS test_files                         -- All Word documents, PDFs, and other document types that are used to store tests are kept here. Note that scans of student tests are NOT kept here.
(
    test_file_id INTEGER PRIMARY KEY,                             -- Each file has a unique id number.
    user_id INT,                                                  -- Id of the user who created the test file.
    test_file LONGBLOB,                                           -- The actual contents of the file as raw binary data.
    test_file_name TEXT,                                          -- The file's name (as it was uploaded).
    test_file_type VARCHAR(255),                                  -- The file's type as an all-lowercase string without the period. Examples: "pdf", "doc", "docx", "jpeg"
    number_of_pages INT                                           -- The number of pages in the file.
);

CREATE TABLE IF NOT EXISTS questions                          -- All test questions are stored here.
(
    question_id INTEGER PRIMARY KEY,                              -- Each test question has a unique id.
    test_file_id INT,                                             -- The id of the test file that this corresponds to.
    user_id INT,                                                  -- The id of the user who created this question.
    information TEXT                                              -- JSON describing where in the test file the question is located and other info.
);

CREATE TABLE IF NOT EXISTS student_answer_files               -- All scans of student responses to questions are stored here.
(
    student_answer_file_id INTEGER PRIMARY KEY,                   -- Each scanned set of student responses has a unique id.
    user_id INT,                                                  -- User id of the user who uploaded the scan.
    test_id INT,                                                  -- Id of the test that these answers correspond to.
    student_answer_file LONGBLOB,                                 -- The actual contents of the uploaded file as binary data.
    student_answer_file_name TEXT,                                -- The file's name (as it was uploaded).
    student_answer_file_type VARCHAR(255),                        -- The file's type as a string like 'pdf' or 'docx' or 'jpeg'.
    number_of_pages INT                                           -- Number of pages in the file.
);

CREATE TABLE IF NOT EXISTS student_answers                    -- All student answers to test questions are stored here.
(
    student_answer_id INTEGER PRIMARY KEY,                         -- Each student answer has a unique id.
    user_id INT,                                                   -- The ID of the user who uploaded this student answer.
    question_id INT,                                               -- The ID of the question that this answer answers.
    student_identification TEXT,                                   -- The name/ID number/other identification of the student who made this answer.
    student_answer_file_id INT,                                    -- The ID of the student answer file that this answer is from.
    score VARCHAR(255),                                            -- The score the student received for their answer, expressed as a string.
    points_possible VARCHAR(255),                                  -- Maximum possible points for this question.
    page INT                                                       -- The page number (in the student_answer_file for this student's answer) of this answer.
);

CREATE TABLE IF NOT EXISTS student_answer_sets                -- All "sets" (groups) of student answers. Each set is all of the answers from ONE student for a specific test.
(
    student_answer_set_id INTEGER PRIMARY KEY,                     -- The ID of the student answer set.
    user_id INT,                                                   -- The ID of the user who uploaded the answers.
    test_id INT,                                                   -- THe ID of the test that this student answer set corresponds to.
    student_answer_ids TEXT                                        -- Comma-separated list of the IDs of student answers in this set. The list must have a leading AND trailing comma.
);