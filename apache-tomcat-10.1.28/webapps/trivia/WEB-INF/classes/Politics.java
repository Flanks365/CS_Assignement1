import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;

public class Politics extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
            return; // Ensure no further processing occurs after redirection
        }

        // Get the current question index from the request (if available)
        String indexParam = request.getParameter("currentQuestionIndex");
        int currentQuestionIndex = (indexParam != null) ? Integer.parseInt(indexParam) : 0;

        String title = "50’s Politics Quiz";
        response.setContentType("text/html");

        // Retrieve questions and answers from the database
        StringBuilder questionHtml = new StringBuilder();
        Connection con = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");

            // Retrieve the current question
            PreparedStatement questionStmt = con.prepareStatement(
                "SELECT * FROM (SELECT q.*, ROWNUM rnum FROM questions q WHERE q.category_id = 2) WHERE rnum = ?"
            );
            questionStmt.setInt(1, currentQuestionIndex + 1); // Adjust for the 1-based index
            ResultSet questionRs = questionStmt.executeQuery();

            if (questionRs.next()) {
                int questionId = questionRs.getInt("id");
                String questionText = questionRs.getString("question_text");

                questionHtml.append("<div class='question'>")
                        .append("<h3>").append(questionText).append("</h3>")
                        .append("<div class='answers'>");

                // Retrieve answers for the current question
                PreparedStatement answersStmt = con.prepareStatement("SELECT * FROM answers WHERE question_id = ?");
                answersStmt.setInt(1, questionId);
                ResultSet answersRs = answersStmt.executeQuery();

                while (answersRs.next()) {
                    String answerText = answersRs.getString("answer_text");
                    int answerId = answersRs.getInt("id");
                    questionHtml.append("<button onclick='selectAnswer(").append(answerId).append(", ").append(questionId).append(", ").append(currentQuestionIndex).append(")'>")
                                .append(answerText).append("</button>");
                }

                questionHtml.append("</div></div>"); // Close the question and answers divs

                // Close the answers ResultSet and statement
                answersRs.close();
                answersStmt.close();
            } else {
                questionHtml.append("<img src='https://images.slideplayer.com/20/5999287/slides/slide_30.jpg'>");
            }

            questionRs.close();
            questionStmt.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }

        // Final HTML output
        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>\n" +
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
                questionHtml.toString() +
                "        </div>\n" +
                "    </main>\n" +
                "    <script>\n" +
                "        function selectAnswer(answerId, questionId, currentIndex) {\n" +
                "            fetch('getCorrectAnswer?questionId=' + questionId)\n" +
                "                .then(response => response.json())\n" +
                "                .then(data => {\n" +
                "                    const correctAnswerId = data.correctAnswerId;\n" +
                "                    if (answerId === correctAnswerId) {\n" +
                "                        alert('Correct! Moving to the next question.');\n" +
                "                        window.location.href = 'politics?currentQuestionIndex=' + (currentIndex + 1);\n" +
                "                    } else {\n" +
                "                        alert('Incorrect, try again.');\n" +
                "                    }\n" +
                "                });\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>");
    }

    private int getCorrectAnswerId(int questionId, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT id FROM answers WHERE question_id = ? AND is_correct = 'T'");
        stmt.setInt(1, questionId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1; // No correct answer found
    }
}
