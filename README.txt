IF YOU ARE USING MYSQL AS THE DATABASE:
Add this to your my.cnf or my.ini (MySQL configuration file):
[mysqld]
innodb_log_file_size=256M
max_allowed_packet=500M
