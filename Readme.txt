

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


TO BE ADDED



--------------------------------------------------------------------------------------------------------

Troubleshooting oracle database connections

1. database must be running
2. listener must be running
3. listener.ora must be configured to be the same as connection string
4. listener must be runnng after database has been started (reloading listener may be necessary)