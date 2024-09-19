import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
public class Quiz extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
            return; // Ensure no further processing occurs after redirection
        }
        
        String title = "Logged in as: user";
        title += session.getAttribute("USER_ID");
        response.setContentType("text/html");
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";

        String html = docType + "<html>\n" +
                      "<head><title>" + title + "</title>" +
                      "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" + "</head>\n" +
                      "<body bgcolor=\"#f0f0f0\">\n" +
                      "<h1 align=\"center\">" + title + "</h1>\n";
        
        if ("admin".equals(session.getAttribute("ROLE"))) {
            html += "<div style=\"text-align: center;\">\n" +
                    "<form action=\"upload\" method=\"GET\">\n" +
                    "<input type=\"submit\" value=\"UPLOAD\" />\n" +
                    "</form>\n" +
                    "</div>\n";
        }
        
        html += "<div style=\"text-align: center;\">\n" +
                "<form action=\"play\" method=\"GET\">\n" +
                "<input type=\"submit\" value=\"Quizzes\" />\n" +
                "</form>\n" +
                "</div>\n" +
                "<div style=\"text-align: center;\">\n" +
                "<form action=\"logout\" method=\"GET\">\n" +
                "<input type=\"submit\" value=\"LOGOUT\" />\n" +
                "</form>\n" +
                "</div>\n" +
                
                "<main class=\"quiz-container\">\n" +
                "<div id=\"question-container\">\n" +
                "<h2 id=\"question-text\">Question 1: Who directed the movie Casablanca?</h2>\n" +
                "<img id=\"question-media\" src=\"images/casablanca.jpg\" alt=\"Casablanca Poster\">\n" +
                "<!-- Example of different media types:\n" +
                "     Use <audio src=\"audio/clip.mp3\" controls></audio> for audio\n" +
                "     Use <video src=\"video/clip.mp4\" controls autoplay></video> for video\n" +
                "-->\n" +
                "<div class=\"answers\">\n" +
                "<button onclick=\"checkAnswer('wrong')\">Orson Welles</button>\n" +
                "<button onclick=\"checkAnswer('correct')\">Michael Curtiz</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">John Ford</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">Alfred Hitchcock</button>\n" +
                "</div>\n" +
                "</div>\n" +
                "</main>\n" +
                
                "<script>\n" +
                "// Function to check the answer and move to the next question\n" +
                "function checkAnswer(answer) {\n" +
                "    if (answer === 'correct') {\n" +
                "        alert('Correct! Moving to the next question.');\n" +
                "        // Logic to load the next question\n" +
                "    } else {\n" +
                "        alert('Incorrect, try again.');\n" +
                "    }\n" +
                "}\n" +
                "</script>\n" +
                
                "</body></html>";

        PrintWriter out = response.getWriter();
        out.println(html);
    }
}
