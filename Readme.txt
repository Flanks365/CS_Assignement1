Setting up and launching webapp instructions:
1. Clone the repo onto your machine
2. Set up the CATALINA_HOME environment variable to map to this projects apache-tomcat-10.1.28 folder
3. Open the command prompt and navigate to this projects bin folder. 
4. Type "startup.bat" in the command line to launch the webapp
5. Type "http://localhost:8081/trivia/login" in your browser to access the website
6. Enjoy!

Setting up database instructions:
1. Download the oracle database if you have not already 
2. Open and connect to your oracle database
3. Ensure that your oracle username is "system" and your oracle password is "oracle1"
4. Execute the commands below to create the tables
5. Execute the command "commit;" to save your changes

Table creation script:

 CREATE TABLE categories (
        id RAW(16) PRIMARY KEY,
        category_name VARCHAR(100),
	image_type VARCHAR(50),
	image BLOB
    );

 CREATE TABLE questions (
        id RAW(16) PRIMARY KEY,
        question_text VARCHAR(255),
        media_type VARCHAR(50),  -- "image" or "audio"
        media_content BLOB,    -- media in base64
	media_preview VARCHAR(200),
        category_id RAW(16) NOT NULL,
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE CASCADE
    );

CREATE TABLE answers (
        id RAW(16) PRIMARY KEY,
        question_id RAW(16) NOT NULL,
        answer_text VARCHAR(255),
        is_correct CHAR(1), -- Use 'Y' for true, 'N' for false
	answer_index INT,
        FOREIGN KEY (question_id) REFERENCES questions(id) ON DELETE CASCADE
    );
CREATE TABLE users (
	id RAW(16) PRIMARY KEY, 
	username VARCHAR2(36), 
	password VARCHAR2(100),
	role VARCHAR2(36) 
);
