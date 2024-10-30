import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.util.Base64;
import java.util.UUID;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.StringBuilder;
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
        String mediaContentBase64 = "";
        String correctAnswerID = "";
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            System.out.println("autoplay: " + autoplay);
            if (!autoplay.equals("all")) {
                System.out.println("in not autoplay all");
                repo.select("id", "categories","UPPER(category_name) = UPPER('"+ categoryName +"')");

                if (!repo.rs.next()) {
                    response.getWriter().write("Invalid category name.");
                    return;
                }
                byte[] categoryIdRaw = repo.rs.getBytes("id");
                String categoryId = repo.rs.getString("id");
                repo.select("*", "(SELECT q.*, ROWNUM rnum FROM questions q WHERE q.category_id = '"+categoryId+"')", "rnum = " + (currentQuestionIndex + 1));
            } else {
                repo.select("*", "(SELECT q.*, ROWNUM rnum FROM questions q)", "rnum = "+currentQuestionIndex+1);
                System.out.println("Current Question Index: " + currentQuestionIndex);
            }
            if (repo.rs.next()) {
                String questionId = repo.rs.getString("id");
                byte[] questionIdRaw = repo.rs.getBytes("id");
                String questionText = repo.rs.getString("question_text");
                System.out.println("Question ID: " + questionId); // Log raw question ID

                String mediaType = repo.rs.getString("media_type");
                byte[] mediaBytes = repo.rs.getBytes("media_content");
                System.out.println("Media Type: " + mediaType);
                if (mediaType.equals("quote")) {
                    System.out.println("Quote: ");
                    String mediaContent = new String(mediaBytes);
                    mediaContentBase64 = "<div class='quote'> \"" + mediaContent + "\"</div>";
                } else if (mediaType.contains("image")) {
                    System.out.println("Image: " );
                    String mediaContent = Base64.getEncoder().encodeToString(mediaBytes);
                    mediaContentBase64 = "<img src=\"data:" + mediaType + ";base64," + mediaContent + "\" />";
                } else {
                    System.out.println("Video: " );
                    String mediaContent = Base64.getEncoder().encodeToString(mediaBytes);
                    mediaContentBase64 = "<video controls autoplay><source src=\"data:" + mediaType + ";base64,"
                            + mediaContent + "\" type=\"" + mediaType + "\"></video>";
                }

                questionHtml.append("<div class='question'>")
                        .append("<h3>").append(questionText).append("</h3>")
                        .append("<div id=\"quoteOrBlob\">" + mediaContentBase64 + "</div>\n")
                        .append("<div class='answers'>");
                String qid = repo.rs.getString("id");
                repo.select("*", "answers", "question_id = '"+qid+"'");

                while (repo.rs.next()) {
                    String answerText = repo.rs.getString("answer_text");
                    String answerId = repo.rs.getString("id");
                    System.out.println("Answer ID: " + answerId); // Log raw answer ID

                    questionHtml.append("<div class='button-container'><button data-answer-id='" + answerId + "' onclick='selectAnswer(this, \"")
                            .append(categoryName).append("\", \"")
                            .append(answerId).append("\", \"").append(questionId).append("\", ")
                            .append(autoplay).append(", ").append(currentQuestionIndex).append(")'");

                    if (!autoplay.equals("false")) {
                        questionHtml.append("id=\"" + answerId + "\" disabled>");
                    } else {
                        questionHtml.append(">");
                    }
                    questionHtml.append(answerText).append("</button><span class='answer-counter'>0</span></div>");
                }
                if (!autoplay.equals("false")) {
                    repo.select("id", "answers", "question_id = '"+qid+"' AND is_correct = 'Y'");
                    if (repo.rs.next()) {
                        correctAnswerID = repo.rs.getString("id");
                    }
                }

                questionHtml.append("</div>");
                questionHtml.append("<p id=\"questionInfo\"></p>");
                questionHtml.append("<button id='counter-toggle'>Show answer counts</button>");
                questionHtml
                        .append("<br><br><button onclick=\"window.location.href='main'\">Back to Main Page</button>");
                questionHtml.append("</div></div>");
            } else {
                questionHtml.append("<img src='https://images.slideplayer.com/20/5999287/slides/slide_30.jpg'>");
                questionHtml
                        .append("<br><br><button onclick=\"window.location.href='main'\">Back to Main Page</button>");
            }
            repo.close();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (repo != null) {
                try {
                    repo.close();
                } catch (Exception e) {
                    System.out.println(e);
                }
            }
        }
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