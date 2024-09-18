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
        
        String title = "Logged in as: " + session.getAttribute("USER_ID");
        response.setContentType("text/html");
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";

        String html = docType + "<html>\n" +
                      "<head><title>" + title + "</title></head>\n" +
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
                "<h2 id=\"question-text\">Who was the President of the United States during the start of the 1950s?</h2>\n" +
                "<img id=\"question-media\" src=\"images/harry-truman.jpg\" alt=\"Harry Truman\" style=\"width:300px;height:auto;\"/>\n" +
                "<div class=\"answers\">\n" +
                "<button onclick=\"checkAnswer('correct')\">Harry S. Truman</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">Dwight D. Eisenhower</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">John F. Kennedy</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">Lyndon B. Johnson</button>\n" +
                "</div>\n" +
                "</div>\n" +

                "<div id=\"question-container\">\n" +
                "<h2 id=\"question-text\">Which U.S. senator led the infamous anti-communist \"Red Scare\" hearings?</h2>\n" +
                "<img id=\"question-media\" src=\"images/joseph-mccarthy.jpg\" alt=\"Joseph McCarthy\" style=\"width:300px;height:auto;\"/>\n" +
                "<div class=\"answers\">\n" +
                "<button onclick=\"checkAnswer('wrong')\">Robert Taft</button>\n" +
                "<button onclick=\"checkAnswer('correct')\">Joseph McCarthy</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">Richard Nixon</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">Hubert Humphrey</button>\n" +
                "</div>\n" +
                "</div>\n" +

                "<div id=\"question-container\">\n" +
                "<h2 id=\"question-text\">In which year did Dwight D. Eisenhower become President?</h2>\n" +
                "<audio id=\"question-audio\" controls autoplay>\n" +
                "<source src=\"audio/eisenhower-speech.mp3\" type=\"audio/mp3\">\n" +
                "Your browser does not support the audio element.\n" +
                "</audio>\n" +
                "<div class=\"answers\">\n" +
                "<button onclick=\"checkAnswer('wrong')\">1948</button>\n" +
                "<button onclick=\"checkAnswer('correct')\">1953</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">1957</button>\n" +
                "<button onclick=\"checkAnswer('wrong')\">1961</button>\n" +
                "</div>\n" +
                "</div>\n" +

                "</main>\n" +
                
                "<script>\n" +
                "// Function to check the answer and give feedback\n" +
                "function checkAnswer(answer) {\n" +
                "    if (answer === 'correct') {\n" +
                "        alert('Correct! Moving to the next question.');\n" +
                "        // Logic to load the next question\n" +
                "    } else {\n" +
                "        alert('Incorrect, please try again.');\n" +
                "    }\n" +
                "}\n" +
                "</script>\n" +
                
                "</body></html>";

        PrintWriter out = response.getWriter();
        out.println(html);
    }
}
