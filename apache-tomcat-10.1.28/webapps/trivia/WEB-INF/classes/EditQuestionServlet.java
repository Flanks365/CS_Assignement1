import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.PrintWriter;
import java.sql.*;
import java.io.*;
import java.time.LocalDate;
import java.lang.StringBuilder;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.text.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

@MultipartConfig
public class EditQuestionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        System.out.println();
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";
        String html = docType + "<html>\n" +
                "<head><title>Edit Quiz</title>" +
                "<script src=\"resources/js/editQuestions.js\" async></script>" +
                "<link rel=\"stylesheet\" href=\"resources/css/styles.css\" type=\"text/css\">\n" + 
                "</head>\n" +
                "<body>\n";
        String errMsg = "";
        Connection con = null;
        try {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (Exception ex) {
                System.out.println("Message: " + ex.getMessage());
                return;
            }
            String quizName = request.getParameter("quizName");
            UUID quizId = UUID.fromString(request.getParameter("id"));

            html += "<h1 align=\"center\">Edit Quiz: " + quizName + "</h1>\n" +
                    "<p>Create a new question:</p>" +
                    "<form id=\"new-question-form\" action=\"editQuestions\" method=\"POST\" enctype=\"multipart/form-data\">"
                    +
                    "<input type=\"hidden\" name=\"id\" value=\"" + quizId + "\" />" +
                    "<input type=\"hidden\" name=\"quizName\" value=\"" + quizName + "\" />" +
                    "<input type=\"textarea\" name=\"Question\" placeholder=\"Question\" />" +
                    "<input type=\"textarea\" name=\"Answer\" placeholder=\"Answer\" />" +
                    "<input type=\"textarea\" name=\"Decoy1\" placeholder=\"Decoy answer\" />" +
                    "<input type=\"textarea\" name=\"Decoy2\" placeholder=\"Decoy answer\" />" +
                    "<input type=\"textarea\" name=\"Decoy3\" placeholder=\"Decoy answer\" /><br>" +
                    "Content type: <input type=\"radio\" id=\"content-quote\" name=\"ContentType\" value=\"quote\">" +
                    "<label for=\"content-quote\">Quote</label>" +
                    "<input type=\"radio\" id=\"content-image\" name=\"ContentType\" value=\"image\">" +
                    "<label for=\"content-image\">Image</label>" +
                    "<input type=\"radio\" id=\"content-audio\" name=\"ContentType\" value=\"audio\">" +
                    "<label for=\"content-audio\">Audio</label>" +
                    "<input type=\"radio\" id=\"content-video\" name=\"ContentType\" value=\"video\">" +
                    "<label for=\"content-video\">Video</label><br>" +
                    "Content: " +
                    "<input class=\"file-input\" type=\"file\" name=\"FileName\" />" +
                    "<input class=\"quote-input\" type=\"text\" name=\"QuoteText\" />" +
                    "<br><input type=\"submit\" value=\"Submit\"/>" +
                    "</form>" +
                    "<button id=\"form-toggle\">Create</button>";

            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");
            PreparedStatement stmt = con
                    .prepareStatement("select id, question_text from questions where category_id = ?");
            stmt.setBytes(1, asBytes(quizId));
            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;
            byte bArr[] = null;
            UUID sid = null;

            while (rs.next()) {
                if (!hasResults) {
                    html += "<p>Or edit an existing question: </p>";
                    hasResults = true;
                }

                byte[] raw = rs.getBytes(1);
                sid = asUuid(raw);
                String question = rs.getString(2);
                String answer = rs.getString(3);
                String decoy1 = rs.getString(4);
                String decoy2 = rs.getString(5);
                String decoy3 = rs.getString(6);

                html += "<div id=\"edit-question-" + sid + "\" class=\"question-edit-container\" " +
                        "questionId=\"" + sid + "\">" + question +
                        "<form class=\"question-edit-form\" id=\"edit-form-" + sid
                        + "\" action=\"editQuestions\" method=\"PUT\" enctype=\"multipart/form-data\">" +
                        "<input type=\"hidden\" name=\"id\" value=\"" + quizId + "\" />" +
                        "<input type=\"hidden\" name=\"quizName\" value=\"" + quizName + "\" />" +
                        "<input type=\"textarea\" name=\"Question\" placeholder=\"Question\" " +
                        "value=\"" + question + "\" />" +
                        "<input type=\"textarea\" name=\"Answer\" placeholder=\"Answer\" " +
                        "value=\"" + answer + "\" />" +
                        "<input type=\"textarea\" name=\"Decoy1\" placeholder=\"Decoy answer\" " +
                        "value=\"" + decoy1 + "\" />" +
                        "<input type=\"textarea\" name=\"Decoy2\" placeholder=\"Decoy answer\" " +
                        "value=\"" + decoy2 + "\" />" +
                        "<input type=\"textarea\" name=\"Decoy3\" placeholder=\"Decoy answer\" " +
                        "value=\"" + decoy3 + "\" /><br>" +
                        "Content type: <input type=\"radio\" class=\"radio-default\" id=\"content-quote\" name=\"ContentType\" value=\"quote\">"
                        +
                        "<label for=\"content-quote\">Quote</label>" +
                        "<input type=\"radio\" id=\"content-image\" name=\"ContentType\" value=\"image\">" +
                        "<label for=\"content-image\">Image</label>" +
                        "<input type=\"radio\" id=\"content-audio\" name=\"ContentType\" value=\"audio\">" +
                        "<label for=\"content-audio\">Audio</label>" +
                        "<input type=\"radio\" id=\"content-video\" name=\"ContentType\" value=\"video\">" +
                        "<label for=\"content-video\">Video</label><br>" +
                        "Content: " +
                        "<input class=\"file-input\" type=\"file\" name=\"FileName\" />" +
                        "<input class=\"quote-input\" type=\"text\" name=\"QuoteText\" />" +
                        "<br><input type=\"submit\" value=\"Update\" />" +
                        "</form>" +
                        "<button class=\"question-edit-toggle\" id=\"question-edit-toggle-" + sid + "\">Edit</button>" +
                        "<button class=\"question-delete\" id=\"question-delete-" + sid + "\">Delete</button><br>" +
                        "</div>";
            }
            stmt.close();
            con.close();
        } catch (SQLException ex) {

            errMsg = errMsg + "\n--- SQLException caught ---\n";
            while (ex != null) {
                errMsg += "Message: " + ex.getMessage();
                errMsg += "SQLState: " + ex.getSQLState();
                errMsg += "ErrorCode: " + ex.getErrorCode();
                ex = ex.getNextException();
                errMsg += "";
            }
            System.out.println(errMsg);
        }

        html += "</body></html>";
        PrintWriter out = response.getWriter();
        out.println(html);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        UUID quizId = UUID.fromString(request.getParameter("id"));
        String name = request.getParameter("QuizName");
        String question = request.getParameter("Question");
        Part filePart = request.getPart("FileName");
        String contentType = request.getParameter("ContentType");
        InputStream is;
        if (contentType != "quote") {
            contentType = filePart.getContentType();
            is = filePart.getInputStream();
        } else {
            String quote = request.getParameter("QuoteText");
            is = new ByteArrayInputStream(quote.getBytes(StandardCharsets.UTF_8));
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");
            PreparedStatement preparedStatement = con
                    .prepareStatement(
                            "INSERT INTO questions (" +
                                    "id, category_id, question, content_type, content" +
                                    ") VALUES (?,?,?,?,?)");
            UUID uuid = UUID.randomUUID();
            preparedStatement.setBytes(1, asBytes(uuid));
            preparedStatement.setBytes(2, asBytes(quizId));
            preparedStatement.setString(3, question);
            preparedStatement.setString(4, contentType);
            preparedStatement.setBinaryStream(5, is);

            int row = preparedStatement.executeUpdate();
            preparedStatement.close();
            con.close();
        } catch (SQLException ex) {
            while (ex != null) {
                System.out.println("Message: " + ex.getMessage());
                System.out.println("SQLState: " + ex.getSQLState());
                System.out.println("ErrorCode: " + ex.getErrorCode());
                ex = ex.getNextException();
                System.out.println("");
            }
        }
        response.setStatus(200);
        response.sendRedirect("/trivia/editQuestions?id=" + quizId + "&quizName=" + name);
    }

    // @Override
    // protected void doPut(HttpServletRequest request, HttpServletResponse response)
    //         throws ServletException, IOException {

    //     UUID quizId = UUID.fromString(request.getParameter("id"));
    //     String name = request.getParameter("QuizName");
    //     String question = request.getParameter("Question");
    //     String answer = request.getParameter("Answer");
    //     String decoy1 = request.getParameter("Decoy1");
    //     String decoy2 = request.getParameter("Decoy2");
    //     String decoy3 = request.getParameter("Decoy3");
    //     Part filePart = request.getPart("FileName");
    //     String contentType = request.getParameter("ContentType");
    //     InputStream is;
    //     if (contentType != "quote") {
    //         contentType = filePart.getContentType();
    //         is = filePart.getInputStream();
    //     } else {
    //         String quote = request.getParameter("QuoteText");
    //         is = new ByteArrayInputStream(quote.getBytes(StandardCharsets.UTF_8));
    //     }

    //     Connection con = null;
    //     try {
    //         con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
    //                 "system", "oracle1");
    //         PreparedStatement preparedStatement = con
    //                 .prepareStatement(
    //                         "INSERT INTO questions (" +
    //                                 "id, quiz_id, question, answer, decoy1, decoy2, decoy3, content_type, content" +
    //                                 ") VALUES (?,?,?,?,?,?,?,?,?)");
    //         UUID uuid = UUID.randomUUID();
    //         preparedStatement.setBytes(1, asBytes(uuid));
    //         preparedStatement.setBytes(2, asBytes(quizId));
    //         preparedStatement.setString(3, question);
    //         preparedStatement.setString(4, answer);
    //         preparedStatement.setString(5, decoy1);
    //         preparedStatement.setString(6, decoy2);
    //         preparedStatement.setString(7, decoy3);
    //         preparedStatement.setString(8, contentType);
    //         preparedStatement.setBinaryStream(9, is);

    //         int row = preparedStatement.executeUpdate();
    //         preparedStatement.close();
    //         con.close();
    //     } catch (SQLException ex) {
    //         while (ex != null) {
    //             System.out.println("Message: " + ex.getMessage());
    //             System.out.println("SQLState: " + ex.getSQLState());
    //             System.out.println("ErrorCode: " + ex.getErrorCode());
    //             ex = ex.getNextException();
    //             System.out.println("");
    //         }
    //     }
    //     response.setStatus(200);
    //     response.sendRedirect("/trivia/editQuiz?id=" + quizId + "&quizName=" + name);
    // }

    public static byte[] asBytes(UUID uuid) {
        ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
        bb.putLong(uuid.getMostSignificantBits());
        bb.putLong(uuid.getLeastSignificantBits());
        return bb.array();
    }

    public static UUID asUuid(byte[] bytes) {
        ByteBuffer bb = ByteBuffer.wrap(bytes);
        long firstLong = bb.getLong();
        long secondLong = bb.getLong();
        return new UUID(firstLong, secondLong);
    }
}
