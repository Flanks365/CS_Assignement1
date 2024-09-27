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

public class Quizpage extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
            return; // Ensure no further processing occurs after redirection
        }

        String categoryName = "";
        // Boolean autoplay = Boolean.parseBoolean(request.getParameter("autoplay"));
        String autoplay = request.getParameter("autoplay");

        if (!autoplay.equals("all")) {
            // categoryName="Science";
            categoryName = request.getParameter("category_name").trim();
            if (categoryName == null || categoryName.isEmpty()) {
                response.getWriter().write("Category not provided.");
                return;
            }
        }

        String indexParam = request.getParameter("currentQuestionIndex");
        int currentQuestionIndex = (indexParam != null) ? Integer.parseInt(indexParam) : 0;
        String title = "";
        if (!autoplay.equals("all")) {
            title = categoryName + " Quiz";
        } else {
            title = "Autoplaying";
        }

        response.setContentType("text/html");

        StringBuilder questionHtml = new StringBuilder();
        Connection con = null;
        String mediaContentBase64 = "";
        String correctAnswerID = "";
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");

            PreparedStatement questionStmt;
            ResultSet questionRs;

            System.out.println("autoplay: " + autoplay);
            if (!autoplay.equals("all")) {
                System.out.println("in not autoplay all");
                PreparedStatement categoryStmt = con
                        .prepareStatement("SELECT id FROM categories WHERE UPPER(category_name) = UPPER(?)");
                categoryStmt.setString(1, categoryName);
                ResultSet categoryRs = categoryStmt.executeQuery();

                if (!categoryRs.next()) {
                    response.getWriter().write("Invalid category name.");
                    return;
                }

                byte[] categoryIdRaw = categoryRs.getBytes("id");

                UUID categoryId = asUuid(categoryIdRaw);

                questionStmt = con.prepareStatement(
                        "SELECT * FROM (SELECT q.*, ROWNUM rnum FROM questions q WHERE q.category_id = ?) WHERE rnum = ?");
                questionStmt.setBytes(1, categoryIdRaw);
                questionStmt.setInt(2, currentQuestionIndex + 1);
                questionRs = questionStmt.executeQuery();
            } else {
                questionStmt = con.prepareStatement(
                        "SELECT * FROM (SELECT q.*, ROWNUM rnum FROM questions q) WHERE rnum = ?");
                System.out.println("Current Question Index: " + currentQuestionIndex);
                questionStmt.setInt(1, currentQuestionIndex + 1);
                questionRs = questionStmt.executeQuery();
            }

            if (questionRs.next()) {
                String questionId = questionRs.getString("id");
                byte[] questionIdRaw = questionRs.getBytes("id");
                // String questionId = Base64.getEncoder().encodeToString(questionIdRaw);
                String questionText = questionRs.getString("question_text");
                System.out.println("Question ID: " + questionId); // Log raw question ID

                String mediaType = questionRs.getString("media_type");
                Blob mediaBlob = questionRs.getBlob("media_content");
                System.out.println("Media Type: " + mediaType);
                if (mediaType.equals("quote")) {
                    System.out.println("Quote: " + mediaBlob);
                    String mediaContent = new String(mediaBlob.getBytes(1, (int) mediaBlob.length()));

                    mediaContentBase64 = "<div class='quote'> \"" + mediaContent + "\"</div>";
                } else if (mediaType.contains("image")) {
                    System.out.println("Image: " + mediaBlob);
                    byte[] mediaBytes = mediaBlob.getBytes(1, (int) mediaBlob.length());
                    String mediaContent = Base64.getEncoder().encodeToString(mediaBytes);
                    mediaContentBase64 = "<img src=\"data:" + mediaType + ";base64," + mediaContent + "\" />";
                } else {
                    System.out.println("Video: " + mediaBlob);
                    byte[] mediaBytes = mediaBlob.getBytes(1, (int) mediaBlob.length());
                    String mediaContent = Base64.getEncoder().encodeToString(mediaBytes);
                    mediaContentBase64 = "<video controls autoplay><source src=\"data:" + mediaType + ";base64,"
                            + mediaContent + "\" type=\"" + mediaType + "\"></video>";
                }

                questionHtml.append("<div class='question'>")
                        .append("<h3>").append(questionText).append("</h3>")
                        .append("<div id=\"quoteOrBlob\">" + mediaContentBase64 + "</div>\n")
                        .append("<div class='answers'>");

                PreparedStatement answersStmt = con.prepareStatement("SELECT * FROM answers WHERE question_id = ?");
                answersStmt.setBytes(1, questionIdRaw);
                ResultSet answersRs = answersStmt.executeQuery();

                while (answersRs.next()) {
                    String answerText = answersRs.getString("answer_text");
                    byte[] answerIdRaw = answersRs.getBytes("id");
                    String answerId = answersRs.getString("id");
                    System.out.println("Answer ID: " + answerId); // Log raw answer ID

                    questionHtml.append("<button onclick='selectAnswer(this, \"").append(categoryName).append("\", \"")
                            .append(answerId).append("\", \"").append(questionId).append("\", ")
                            .append(autoplay).append(", ").append(currentQuestionIndex).append(")'");

                    if (!autoplay.equals("false")) {
                        questionHtml.append("id=\"" + answerId + "\" disabled>");
                    } else {
                        questionHtml.append(">");
                    }
                    questionHtml.append(answerText).append("</button>");
                }
                if (!autoplay.equals("false")) {
                    PreparedStatement correctAnswerStmt = con
                            .prepareStatement("SELECT id FROM answers WHERE question_id = ? AND is_correct = 'Y'");
                    correctAnswerStmt.setBytes(1, questionIdRaw);
                    ResultSet correctAnswerRs = correctAnswerStmt.executeQuery();
                    if (correctAnswerRs.next()) {
                        correctAnswerID = correctAnswerRs.getString("id");
                    }
                    correctAnswerRs.close();
                    correctAnswerStmt.close();
                }

                questionHtml.append("</div>");
                questionHtml.append("<p id=\"questionInfo\"></p>");
                questionHtml
                        .append("<br><br><button onclick=\"window.location.href='main'\">Back to Main Page</button>");
                // questionHtml.append(
                //         "<br><br><button onclick=\"window.location.href='categories'\">Back to Play Quizzes</button>");
                questionHtml.append("</div></div>");
                answersRs.close();
                answersStmt.close();
            } else {
                questionHtml.append("<img src='https://images.slideplayer.com/20/5999287/slides/slide_30.jpg'>");
                questionHtml
                        .append("<br><br><button onclick=\"window.location.href='main'\">Back to Main Page</button>");
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
        String script = "";
        // if (autoplay) {
        //     script = "let clock = document.getElementById('" + correctAnswerID + "');" +
        //             "let secondsRemaining = 4;" +
        //             "const myInterval = setInterval(()=>{" +
        //             "secondsRemaining--;" +
        //             "if(!secondsRemaining)" +
        //             "startAnimation();" +
        //             "},1000);" +
        //             "function startAnimation(){" +
        //             "clock.classList.add('animation');" +
        //             "clearInterval(myInterval);" +
        //             "setTimeout(()=>{" +
        //             "clock.classList.remove('animation');" +
        //             "clock.disabled = false;" +
        //             "clock.click();" +
        //             "}, 5000);" +
        //             "clock.classList.add('done');" +
        //             "};";
        // }
        PrintWriter out = response.getWriter();
        String finalHtml = "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "    <meta charset=\"UTF-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "    <title>" + title + "</title>\n" +
                "    <script src=\"resources/js/quiz.js\" async ></script>" +
                "    <link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" +
                "</head>\n" +
                "<body>\n" +
                "    <header>\n" +
                "        <h1>" + title + "</h1>\n" +
                "        <p>Test your knowledge of the " + categoryName + " landscape!</p>\n" +
                "    </header>\n" +
                "    <input id=\"autoplay\" type=\"hidden\" value=\"" + autoplay + "\" />" +
                "    <input id=\"autoplayCorrectAnswer\" type=\"hidden\" value=\"" + correctAnswerID + "\" />" +
                "    <main class=\"quiz-container\">\n" +
                "        <div id=\"quiz-content\">\n" +
                questionHtml.toString() +
                "        </div>\n" +
                "    </main>\n";
        finalHtml += "</body>\n" +
                        "</html>";
        out.println(finalHtml);
    }

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}