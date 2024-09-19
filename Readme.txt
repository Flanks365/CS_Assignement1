
For the questions and categories, let's use it this way:

SQL> CREATE TABLE categories (
        id INT PRIMARY KEY,
        category_name VARCHAR(100)
    );

Table created.

SQL> CREATE TABLE questions (
        id INT PRIMARY KEY,
        question_text VARCHAR(255),
        media_type VARCHAR(50),  -- "image" or "audio"
        media_src VARCHAR(255),    -- file path or URL for media
        category_id INT,
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
    );




SQL> CREATE TABLE answers (
        id INT PRIMARY KEY,
        question_id INT,
        answer_text VARCHAR(255),
        is_correct CHAR(1), -- Use 'Y' for true, 'N' for false
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
    );




--------------------------------------------------------------------------------------------------------

Oracle database tables are:

users
uid    username    password    admin/user
pk(uid)

question
qid    category    question    option1        option2        option3        option4        answer
pk(uid)

multimedia
mid    qid   type    content 
pk(mid, qid)

--------------------------------------------------------------------------------------------------------

Table creation script:

CREATE TABLE users(
"uid" RAW(16) NOT NULL, 
"username" VARCHAR2(36), 
"password" VARCHAR2(100),
"admin/user" VARCHAR2(36), 
PRIMARY KEY ("uid")
);

CREATE TABLE quizzes (
id raw(16) primary key,
name varchar(50),
image_type varchar(50),
image blob
);

create table questions (
id raw(16) primary key,
quiz_id raw(16) not null,
question varchar(400),
answer varchar(200),
decoy1 varchar(200),
decoy2 varchar(200),
decoy3 varchar(200),
content_type varchar(50),
content blob,
foreign key(quiz_id) references quizzes(id)
);


TO BE ADDED



--------------------------------------------------------------------------------------------------------

Troubleshooting oracle database connections

1. database must be running
2. listener must be running
3. listener.ora must be configured to be the same as connection string
4. listener must be runnng after database has been started (reloading listener may be necessary)
