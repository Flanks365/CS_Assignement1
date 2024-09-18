import jakarta.servlet.http.*; 
import jakarta.servlet.*;
import java.io.*;
public class Politics extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
            return; // Ensure no further processing occurs after redirection
        }
        
        String title = "50’s Politics Quiz";
        response.setContentType("text/html");
        
        String html = "<!DOCTYPE html>\n" +
                      "<html lang=\"en\">\n" +
                      "<head>\n" +
                      "    <meta charset=\"UTF-8\">\n" +
                      "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                      "    <title>" + title + "</title>\n" +
                      "    <link rel=\"stylesheet\" href=\"styles.css\">\n" +
                      "</head>\n" +
                      "<body>\n" +
                      "    <header>\n" +
                      "        <h1>" + title + "</h1>\n" +
                      "        <p>Test your knowledge of the political landscape of the 1950s!</p>\n" +
                      "    </header>\n" +
                      "    <main class=\"quiz-container\">\n" +
                      "        <div id=\"quiz-content\">\n" +
                      "            <h2 id=\"question-text\">Loading question...</h2>\n" +
                      "            <img id=\"question-media\" src=\"\" alt=\"\" style=\"display:none; width:300px;height:auto;\"/>\n" +
                      "            <audio id=\"question-audio\" controls style=\"display:none;\"></audio>\n" +
                      "            <div class=\"answers\">\n" +
                      "                <button onclick=\"selectAnswer(0)\">Answer 1</button>\n" +
                      "                <button onclick=\"selectAnswer(1)\">Answer 2</button>\n" +
                      "                <button onclick=\"selectAnswer(2)\">Answer 3</button>\n" +
                      "                <button onclick=\"selectAnswer(3)\">Answer 4</button>\n" +
                      "            </div>\n" +
                      "        </div>\n" +
                      "    </main>\n" +
                      "    <script>\n" +
                      "        // Question Data\n" +
                      "        const quizQuestions = [\n" +
                      "            {\n" +
                      "                question: \"Who was the President of the United States during the start of the 1950s?\",\n" +
                      "                answers: [\"Harry S. Truman\", \"Dwight D. Eisenhower\", \"John F. Kennedy\", \"Lyndon B. Johnson\"],\n" +
                      "                correctAnswer: 0,\n" +
                      "                mediaType: \"image\",\n" +
                      "                mediaSrc: \"images/harry-truman.jpg\"\n" +
                      "            },\n" +
                      "            {\n" +
                      "                question: \"Which U.S. senator led the infamous anti-communist 'Red Scare' hearings?\",\n" +
                      "                answers: [\"Robert Taft\", \"Joseph McCarthy\", \"Richard Nixon\", \"Hubert Humphrey\"],\n" +
                      "                correctAnswer: 1,\n" +
                      "                mediaType: \"image\",\n" +
                      "                mediaSrc: \"images/joseph-mccarthy.jpg\"\n" +
                      "            },\n" +
                      "            {\n" +
                      "                question: \"In which year did Dwight D. Eisenhower become President?\",\n" +
                      "                answers: [\"1948\", \"1953\", \"1957\", \"1961\"],\n" +
                      "                correctAnswer: 1,\n" +
                      "                mediaType: \"audio\",\n" +
                      "                mediaSrc: \"audio/eisenhower-speech.mp3\"\n" +
                      "            }\n" +
                      "        ];\n" +
                      "\n" +
                      "        let currentQuestionIndex = 0;\n" +
                      "\n" +
                      "        // Function to load a question\n" +
                      "        function loadQuestion() {\n" +
                      "            const questionObj = quizQuestions[currentQuestionIndex];\n" +
                      "            document.getElementById('question-text').textContent = questionObj.question;\n" +
                      "\n" +
                      "            // Load media\n" +
                      "            const imageElement = document.getElementById('question-media');\n" +
                      "            const audioElement = document.getElementById('question-audio');\n" +
                      "            if (questionObj.mediaType === 'image') {\n" +
                      "                imageElement.src = questionObj.mediaSrc;\n" +
                      "                imageElement.style.display = 'block';\n" +
                      "                audioElement.style.display = 'none';\n" +
                      "            } else if (questionObj.mediaType === 'audio') {\n" +
                      "                audioElement.src = questionObj.mediaSrc;\n" +
                      "                audioElement.style.display = 'block';\n" +
                      "                audioElement.play();\n" +
                      "                imageElement.style.display = 'none';\n" +
                      "            }\n" +
                      "\n" +
                      "            // Load answers\n" +
                      "            const buttons = document.querySelectorAll('.answers button');\n" +
                      "            buttons.forEach((button, index) => {\n" +
                      "                button.textContent = questionObj.answers[index];\n" +
                      "                button.disabled = false; // Enable the buttons\n" +
                      "            });\n" +
                      "        }\n" +
                      "\n" +
                      "        // Function to handle answer selection\n" +
                      "        function selectAnswer(selectedIndex) {\n" +
                      "            const questionObj = quizQuestions[currentQuestionIndex];\n" +
                      "\n" +
                      "            if (selectedIndex === questionObj.correctAnswer) {\n" +
                      "                alert('Correct! Moving to the next question.');\n" +
                      "                currentQuestionIndex++;\n" +
                      "                \n" +
                      "                if (currentQuestionIndex < quizQuestions.length) {\n" +
                      "                    loadQuestion(); // Load next question\n" +
                      "                } else {\n" +
                      "                    alert('Quiz complete! Well done!');\n" +
                      "                    // You can reset or redirect to a results page here\n" +
                      "                }\n" +
                      "            } else {\n" +
                      "                alert('Incorrect, try again.');\n" +
                      "            }\n" +
                      "        }\n" +
                      "\n" +
                      "        // Load the first question when the page loads\n" +
                      "        window.onload = loadQuestion;\n" +
                      "    </script>\n" +
                      "</body>\n" +
                      "</html>";

        PrintWriter out = response.getWriter();
        out.println(html);
    }
}
