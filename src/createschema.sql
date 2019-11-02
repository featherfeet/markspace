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
    information TEXT                                              -- JSON describing where in the test file the question is located and other info.
);