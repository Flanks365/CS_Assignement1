Setting up and launching webapp instructions:
1. Ensure you have set up the required tables for the database (see the database instructions below)
2. Clone the repo from https://github.com/Flanks365/CS_Assignement1 onto your machine
3. Create an environment variable called "CATALINA_HOME" and map it to this projects "apache-tomcat-10.1.28" folder
4. Open the command prompt and navigate to this projects "bin" folder. 
5. Type "startup.bat" in the command line to launch the webapp
6. Type "http://localhost:8081/trivia/login" in your browser to access the website
7. Enjoy!

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


Docker container instructions to deploy trivia webapp with Oracle database:
1. Find the IPV4 of your oracle database (if on local machine, can use ipconfig)
2. Update String databaseIP in Repository.java found at ROOT/trivia/WEB-INF/classes
3. Compile Repository.java in existing directory
4. Download and install Docker
5. Open a terminal and navigate to ROOT
6. type the following command
        $ docker build -t trivia .
7. Followed by:
        $ docker run -d -p 8081:8081 trivia
8. Open browser and go to localhost:8081