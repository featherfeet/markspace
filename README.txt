This is the source code of the MarkSpace grading software. The project is licensed under the GNU GPL v3. See individual files for license notices. All external libraries are licensed according to their own licenses.

IF YOU ARE USING MYSQL AS THE DATABASE:
Add this to your my.cnf or my.ini (MySQL configuration file):
[mysqld]
innodb_log_file_size=256M
max_allowed_packet=500M
