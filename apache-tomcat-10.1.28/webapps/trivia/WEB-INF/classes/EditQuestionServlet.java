import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;

import java.sql.*;
import java.io.*;
import java.util.*;
import java.time.LocalDate;
import java.lang.StringBuilder;
import java.util.Date;
import java.text.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

@MultipartConfig
public class EditQuestionServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
        }
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
            Enumeration<String> params = request.getParameterNames();
            System.out.println("params: " + Arrays.toString(request.getParameterValues("quizName")));

            UUID quizId = UUID.fromString(request.getParameter("id"));

            html += "<h1 align=\"center\">Edit Quiz: " + quizName + "</h1>\n" +
                    "<p>Create a new question:</p>" +
                    "<form id=\"new-question-form\" action=\"editQuestions\" method=\"POST\" enctype=\"multipart/form-data\">"
                    +
                    "<input type=\"hidden\" name=\"id\" value=\"" + quizId + "\" />" +
                    "<input type=\"hidden\" name=\"quizName\" value=\"" + quizName + "\" />" +
                    "<input type=\"textarea\" name=\"Question\" placeholder=\"Question\" required />" +
                    "<input type=\"textarea\" name=\"Answer\" placeholder=\"Answer\" required />" +
                    "<input type=\"textarea\" name=\"Decoy1\" placeholder=\"Decoy answer\" required />" +
                    "<input type=\"textarea\" name=\"Decoy2\" placeholder=\"Decoy answer (optional)\" />" +
                    "<input type=\"textarea\" name=\"Decoy3\" placeholder=\"Decoy answer (optional)\" /><br>" +
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
                    "<input class=\"quote-input\" type=\"text\" name=\"QuoteText\" placeholder=\"Quote\" />" +
                    "<br><input type=\"submit\" value=\"Submit\"/>" +
                    "</form>" +
                    "<button id=\"form-toggle\">Create</button>";

            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");
            PreparedStatement stmt = con
                    .prepareStatement("select id, question_text, media_type, media_preview from questions where category_id = ?");
            stmt.setBytes(1, asBytes(quizId));
            ResultSet rs = stmt.executeQuery();
            boolean hasResults = false;
            byte bArr[] = null;
            UUID sid = null;
            String[] answers = null;

            while (rs.next()) {
                if (!hasResults) {
                    html += "<p>Or edit an existing question: </p>";
                    hasResults = true;
                }

                byte[] raw = rs.getBytes(1);
                sid = asUuid(raw);
                String question = rs.getString(2);
                String mediaType = rs.getString(3);
                String mediaPreview = rs.getString(4);

                String corrAns = null, decAns1 = null, decAns2 = null, decAns3 = null;
                answers = getAnswers(raw);
                if (answers != null) {
                    corrAns = answers[0];
                    decAns1 = answers[1];
                    decAns2 = answers[2] != null ? answers[2] : "";
                    decAns3 = answers[3] != null ? answers[3] : "";
                }

                if (!mediaType.equals("quote")) {
                    mediaType = mediaType.substring(0, mediaType.indexOf("/"));
                }

                html += "<div id=\"edit-question-" + sid + "\" class=\"question-edit-container\" " +
                        "questionId=\"" + sid + "\">" + question +
                        "<form class=\"question-edit-form\" id=\"edit-form-" + sid
                        + "\" action=\"editQuestions\" method=\"POST\" enctype=\"multipart/form-data\">" +
                        "<input type=\"hidden\" name=\"id\" value=\"" + quizId + "\" />" +
                        "<input type=\"hidden\" name=\"quizName\" value=\"" + quizName + "\" />" +
                        "<input type=\"hidden\" name=\"questionId\" value=\"" + sid + "\" />" +
                        "<input type=\"textarea\" name=\"Question\" placeholder=\"Question text\" " +
                        "value=\"" + question + "\" required />" +
                        "<input type=\"textarea\" name=\"Answer\" placeholder=\"Answer\" " +
                        "value=\"" + corrAns + "\" required />" +
                        "<input type=\"textarea\" name=\"Decoy1\" placeholder=\"Decoy answer\" " +
                        "value=\"" + decAns1 + "\" required />" +
                        "<input type=\"textarea\" name=\"Decoy2\" placeholder=\"Decoy answer (optional)\" " +
                        "value=\"" + decAns2 + "\" />" +
                        "<input type=\"textarea\" name=\"Decoy3\" placeholder=\"Decoy answer (optional)\" " +
                        "value=\"" + decAns3 + "\" /><br>" +
                        "Content type: " +
                        "<input type=\"hidden\" name=\"selectedContent\" value=\"" + mediaType + "\" />" +
                        "<input type=\"radio\" class=\"radio-default\" id=\"content-quote\" name=\"ContentType\" value=\"quote\">"
                        +
                        "<label for=\"content-quote\">Quote</label>" +
                        "<input type=\"radio\" id=\"content-image\" name=\"ContentType\" value=\"image\">" +
                        "<label for=\"content-image\">Image</label>" +
                        "<input type=\"radio\" id=\"content-audio\" name=\"ContentType\" value=\"audio\">" +
                        "<label for=\"content-audio\">Audio</label>" +
                        "<input type=\"radio\" id=\"content-video\" name=\"ContentType\" value=\"video\">" +
                        "<label for=\"content-video\">Video</label><br>" +
                        "Content: " +
                        "<p>Current: " + mediaPreview + "</p>" +
                        "<input class=\"file-input\" type=\"file\" name=\"FileName\" />" +
                        "<input class=\"quote-input\" type=\"text\" name=\"QuoteText\" placeholder=\"Quote\" />" +
                        "<br><input type=\"submit\" value=\"Update\" />" +
                        "</form>" +
                        "<button class=\"question-edit-toggle\" id=\"question-edit-toggle-" + sid + "\">Edit</button>" +
                        "<button class=\"question-delete\" id=\"question-delete-" + sid + "\">Delete</button><br>" +
                        "</div>";
            }
            html += "<br><br><br><form action=\"editQuizzes\" method=\"get\">" +
                    "<input type=\"submit\" value=\"Back to Edit Quizzes Page\"/>\n" +
                    "</form>";
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
        String name = request.getParameter("quizName");
        String question = request.getParameter("Question");
        Part filePart = request.getPart("FileName");
        String contentType = request.getParameter("ContentType");
        String contentPreview = null;
        UUID sid = null;
        byte[] sidRaw = null;
        if (request.getParameter("questionId") != null) {
            sid = UUID.fromString(request.getParameter("questionId"));
            sidRaw = asBytes(sid);
        }

        String answer = request.getParameter("Answer");
        String decoy1 = request.getParameter("Decoy1");
        String decoy2 = request.getParameter("Decoy2");
        String decoy3 = request.getParameter("Decoy3");

        System.out.println("questionId: " + sid);
        System.out.println("answer: " + answer);
        System.out.println("decoy1: " + decoy1);
        System.out.println("decoy2: " + decoy2);
        System.out.println("decoy3: " + decoy3);

        InputStream is;
        if (!contentType.equals("quote")) {
            contentType = filePart.getContentType();
            contentPreview = filePart.getSubmittedFileName();
            is = filePart.getInputStream();
        } else {
            String quote = request.getParameter("QuoteText");
            contentPreview = quote;
            is = new ByteArrayInputStream(quote.getBytes(StandardCharsets.UTF_8));
        }

        Connection con = null;
        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");

            if (sidRaw != null) {
                PreparedStatement deleteStatement = con
                        .prepareStatement(
                                "DELETE FROM answers WHERE question_id = ?");
                deleteStatement.setBytes(1, sidRaw);
                deleteStatement.executeUpdate();
                deleteStatement.close();

                PreparedStatement deleteQuestionStatement = con
                        .prepareStatement(
                                "DELETE FROM questions WHERE id = ?");
                deleteQuestionStatement.setBytes(1, sidRaw);
                deleteQuestionStatement.executeUpdate();
                deleteQuestionStatement.close();

            }

            PreparedStatement preparedStatement = con
                    .prepareStatement(
                            "INSERT INTO questions (" +
                                    "id, category_id, question_text, media_type, media_content, media_preview" +
                                    ") VALUES (?,?,?,?,?,?)");
            UUID uuid = UUID.randomUUID();
            preparedStatement.setBytes(1, asBytes(uuid));
            preparedStatement.setBytes(2, asBytes(quizId));
            preparedStatement.setString(3, question);
            preparedStatement.setString(4, contentType);
            preparedStatement.setBinaryStream(5, is);
            preparedStatement.setString(6, contentPreview);

            int row = preparedStatement.executeUpdate();
            preparedStatement.close();

            PreparedStatement answerStatement = con
                    .prepareStatement(
                            "INSERT INTO answers (" +
                                    "id, question_id, answer_text, is_correct, answer_index" +
                                    ") VALUES (?,?,?,?,?)");
            UUID answerUuid = UUID.randomUUID();
            answerStatement.setBytes(1, asBytes(answerUuid));
            answerStatement.setBytes(2, asBytes(uuid));
            answerStatement.setString(3, answer);
            answerStatement.setString(4, "Y");
            answerStatement.setInt(5, 0);
            answerStatement.executeUpdate();

            UUID decoy1Uuid = UUID.randomUUID();
            answerStatement.setBytes(1, asBytes(decoy1Uuid));
            answerStatement.setBytes(2, asBytes(uuid));
            answerStatement.setString(3, decoy1);
            answerStatement.setString(4, "N");
            answerStatement.setInt(5, 1);
            answerStatement.executeUpdate();

            if (decoy2 != null && !decoy2.trim().equals("")) {
                UUID decoy2Uuid = UUID.randomUUID();
                answerStatement.setBytes(1, asBytes(decoy2Uuid));
                answerStatement.setBytes(2, asBytes(uuid));
                answerStatement.setString(3, decoy2);
                answerStatement.setString(4, "N");
                answerStatement.setInt(5, 2);
                answerStatement.executeUpdate();
            }

            if (decoy3 != null && !decoy3.trim().equals("")) {
                UUID decoy3Uuid = UUID.randomUUID();
                answerStatement.setBytes(1, asBytes(decoy3Uuid));
                answerStatement.setBytes(2, asBytes(uuid));
                answerStatement.setString(3, decoy3);
                answerStatement.setString(4, "N");
                answerStatement.setInt(5, 3);
                answerStatement.executeUpdate();
            }

            answerStatement.close();
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

    public static String[] getAnswers(byte[] questionId) {
        Connection con = null;
        String[] answers = { null, null, null, null };
        String errMsg = null;

        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");
            PreparedStatement stmt = con
                    .prepareStatement(
                            "select answer_text, is_correct, answer_index from answers where question_id = ?");
            stmt.setBytes(1, questionId);
            ResultSet rs = stmt.executeQuery();

            String aText;
            String isCorr;
            int aIdx;
            while (rs.next()) {
                aText = rs.getString(1);
                isCorr = rs.getString(2);
                aIdx = rs.getInt(3);

                answers[aIdx] = aText;
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

        return answers;
    }
}
