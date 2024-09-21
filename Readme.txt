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
