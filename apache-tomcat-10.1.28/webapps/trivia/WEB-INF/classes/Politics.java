import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.Base64;
import java.util.UUID;
import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.http.Part;
import java.sql.*;
import java.io.*;
import java.time.LocalDate;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuilder;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.text.*;
import java.nio.*;


public class Politics extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
            return; // Ensure no further processing occurs after redirection
        }

        String categoryName = request.getParameter("category_name");
        
        //categoryName="Science";
        if (categoryName == null || categoryName.isEmpty()) {
            response.getWriter().write("Category not provided.");
            return;
        }

        String indexParam = request.getParameter("currentQuestionIndex");
        int currentQuestionIndex = (indexParam != null) ? Integer.parseInt(indexParam) : 0;

        String title = categoryName + " Quiz";
        response.setContentType("text/html");

        StringBuilder questionHtml = new StringBuilder();
        Connection con = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");

            PreparedStatement categoryStmt = con.prepareStatement("SELECT id FROM categories WHERE category_name = ?");
            categoryStmt.setString(1, categoryName);
            ResultSet categoryRs = categoryStmt.executeQuery();

            if (!categoryRs.next()) {
                response.getWriter().write("Invalid category name.");
                return;
            }

            byte[] categoryIdRaw = categoryRs.getBytes("id");

            UUID categoryId = asUuid(categoryIdRaw);

            PreparedStatement questionStmt = con.prepareStatement(
                "SELECT * FROM (SELECT q.*, ROWNUM rnum FROM questions q WHERE q.category_id = ?) WHERE rnum = ?");
            questionStmt.setBytes(1, categoryIdRaw);
            questionStmt.setInt(2, currentQuestionIndex + 1);
            ResultSet questionRs = questionStmt.executeQuery();

            if (questionRs.next()) {
                String questionId = questionRs.getString("id");
                byte[] questionIdRaw = questionRs.getBytes("id");
                //String questionId = Base64.getEncoder().encodeToString(questionIdRaw);
                String questionText = questionRs.getString("question_text");
                System.out.println("Question ID: " + questionId);  // Log raw question ID

                questionHtml.append("<div class='question'>")
                        .append("<h3>").append(questionText).append("</h3>")
                        .append("<div class='answers'>");

                PreparedStatement answersStmt = con.prepareStatement("SELECT * FROM answers WHERE question_id = ?");
                answersStmt.setBytes(1, questionIdRaw);
                ResultSet answersRs = answersStmt.executeQuery();

                while (answersRs.next()) {
                    String answerText = answersRs.getString("answer_text");
                    byte[] answerIdRaw = answersRs.getBytes("id");
                    String answerId = answersRs.getString("id");
                    System.out.println("Answer ID: " + answerId);  // Log raw answer ID

                    questionHtml.append("<button onclick='selectAnswer(\"").append(answerId).append("\", \"")
                                .append(questionId).append("\", ").append(currentQuestionIndex).append(")'>")
                                .append(answerText).append("</button>");
                }
                questionHtml.append("<br><br><button onclick=\"window.location.href='main'\">Back to Main Page</button>");
                questionHtml.append("</div></div>");
                answersRs.close();
                answersStmt.close();
            } else {
                questionHtml.append("<img src='https://images.slideplayer.com/20/5999287/slides/slide_30.jpg'>");
                questionHtml.append("<br><br><button onclick=\"window.location.href='main'\">Back to Main Page</button>");
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

        PrintWriter out = response.getWriter();
        out.println("<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + title + "</title>\n" +
                "    <link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <h1>" + title + "</h1>\n" +
                "        <p>Test your knowledge of the " + categoryName + " landscape!</p>\n" +
                "    </header>\n" +
                "    <main class=\"quiz-container\">\n" +
                "        <div id=\"quiz-content\">\n" +
                questionHtml.toString() +
                "        </div>\n" +
                "    </main>\n" +
                "    <script>\n" +
                "        function selectAnswer(answerId, questionId, currentIndex) {\n" +
                "            console.log('Fetching answer for questionId:', questionId, 'and answerId:', answerId);\n" +
                "            fetch('getCorrectAnswer?questionId=' + questionId + '&answerId=' + answerId)\n" +
                "                .then(response => response.text())\n" +
                "                .then(result => {\n" +
                "                 alert(result)\n" +
                "                    if (result === 'correct') {\n" +
                "                        alert('Moving to the next question.');\n" +
                "                        window.location.href = 'Quizpage?category_name=" + categoryName + "&currentQuestionIndex=' + (currentIndex + 1);\n" +
                "                    } else {\n" +
                "                        alert('Try Again.');\n" +
                "                    }\n" +
                "                })\n" +
                "                .catch(error => console.error('Error checking the answer:', error));\n" +
                "        }\n" +
                "    </script>\n" +
                "</body>\n" +
                "</html>");
    }

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}