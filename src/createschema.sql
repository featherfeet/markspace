USE markspace;                                                -- The markspace database holds all data pertaining to the application.
CREATE TABLE users                                            -- The users table holds a record of all users of the application.
(
    user_id INT AUTO_INCREMENT PRIMARY KEY,                       -- Each user has a unique integer identification number.
    username VARCHAR(255),                                        -- The username used to log in as this user.
    password_hash BINARY(64),                                     -- The hash (PBKDF2WithHmacSHA512) of the password used to log in as this user.
    password_salt BINARY(64),                                     -- The salt (64 random bytes) used for the password hash.
    email VARCHAR(255),                                           -- The email used to send notifications/password change links to this user.
    permissions VARCHAR(255)                                      -- A string containing a semicolon-delimited list of permissions from UserPermission.java. Example: "CREATE_TEST;GRADE_TEST".
);
CREATE TABLE tests                                            -- The tests table holds all tests (that can be administered to students) in the application.
(
    test_id          INT AUTO_INCREMENT PRIMARY KEY,              -- Each test has a unique identification number.
    user_id          INT,                                         -- Id of the user who created the test.
    test_name        VARCHAR(1024),                               -- The title of the test that is shown to the user.
    test_description LONGTEXT,                                    -- A user-writable description of the test.
    blank_test_file   INT,                                        -- The identification number (see the test_files table) of the file containing the test WITHOUT correct answers filled in.
    answers_test_file INT                                         -- The identification number (see the test_files table) of the file containing the test WITH correct answers filled in.
);
CREATE TABLE test_files                                       -- All Word documents, PDFs, and other document types that are used to store tests are kept here. Note that scans of student tests are NOT kept here.
(
    test_file_id INT AUTO_INCREMENT PRIMARY KEY,                  -- Each file has a unique id number.
    user_id INT,                                                  -- Id of the user who created the test file.
    test_file LONGBLOB,                                           -- The actual contents of the file as raw binary data.
    test_file_name TEXT,                                          -- The file's name (as it was uploaded).
    test_file_type VARCHAR(255),                                  -- The file's type as an all-lowercase string without the period. Examples: "pdf", "doc", "docx", "jpeg"
    number_of_pages INT                                           -- The number of pages in the file.
);
CREATE TABLE questions                                        -- All test questions are stored here.
(
    question_id INT AUTO_INCREMENT PRIMARY KEY,                   -- Each test question has a unique id.
    test_file_id INT,                                             -- The id of the test file that this corresponds to.
    user_id INT,                                                  -- The id of the user who created this question.
    information TEXT                                              -- JSON describing where in the test file the question is located and other info.
);
CREATE TABLE student_answer_files                             -- All scans of student responses to questions are stored here.
(
    student_answer_file_id INT AUTO_INCREMENT PRIMARY KEY,        -- Each scanned set of student responses has a unique id.
    user_id INT,                                                  -- User id of the user who uploaded the scan.
    test_id INT,                                                  -- Id of the test that these answers correspond to.
    student_answer_file LONGBLOB,                                 -- The actual contents of the uploaded file as binary data.
    student_answer_file_name TEXT,                                -- The file's name (as it was uploaded).
    student_answer_file_type VARCHAR(255),                        -- The file's type as a string like 'pdf' or 'docx' or 'jpeg'.
    number_of_pages INT                                           -- Number of pages in the file.
);
CREATE TABLE student_answers                                  -- All student answers to test questions are stored here.
(
    student_answer_id INT AUTO_INCREMENT PRIMARY KEY,              -- Each student answer has a unique id.
    question_id INT,                                               -- The ID of the question that this answer answers.
    student_identification TEXT,                                   -- The name/ID number/other identification of the student who made this answer.
    student_answer_file_id INT,                                    -- The ID of the student answer file that this answer is from.
    score VARCHAR(255),                                            -- The score the student received for their answer, expressed as a string.
    points_possible VARCHAR(255),                                  -- Maximum possible points for this question.
    page INT                                                       -- The page number (in the student_answer_file for this student's answer)_of this answer.
);