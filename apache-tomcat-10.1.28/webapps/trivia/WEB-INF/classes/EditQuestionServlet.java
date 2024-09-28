import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.*;
import java.util.*;
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
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            String quizName = request.getParameter("quizName");
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
                    "<input class=\"quote-input\" type=\"text\" name=\"QuoteText\" placeholder=\"Quote\" required />" +
                    "<br><input type=\"submit\" value=\"Submit\"/>" +
                    "</form>" +
                    "<button id=\"form-toggle\">Create</button>";
            repo.select("id, question_text, media_type, media_preview", "questions", "category_id = "+asBytes(quizId));
            boolean hasResults = false;
            UUID sid = null;
            String[] answers = null;

            while (repo.rs.next()) {
                if (!hasResults) {
                    html += "<p>Or edit an existing question: </p>";
                    hasResults = true;
                }

                byte[] raw = repo.rs.getBytes(1);
                sid = asUuid(raw);
                String question = repo.rs.getString(2);
                String mediaType = repo.rs.getString(3);
                String mediaPreview = repo.rs.getString(4);

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
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
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
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            if (sidRaw != null) {
                repo.delete("answers", "question_id = "+sidRaw);
                repo.delete("questions", "id = "+sidRaw);
            }

            UUID uuid = UUID.randomUUID();
            repo.insert("questions",
                "(id, category_id, question_text, media_type, media_content, media_preview)",
                asBytes(uuid)+","+asBytes(quizId)+","+question+","+contentType+",?,"+contentPreview, "blob", is);
            UUID answerUuid = UUID.randomUUID();
            repo.insert("answers", "(id, question_id, answer_text, is_correct, answer_index)",
                asBytes(answerUuid)+","+asBytes(uuid)+","+answer+",\"Y\",0,");
            UUID decoy1Uuid = UUID.randomUUID();
            repo.insert("answers", "(id, question_id, answer_text, is_correct, answer_index)",
                asBytes(decoy1Uuid)+","+asBytes(uuid)+","+decoy1+",\"N\",1,");

            if (decoy2 != null && !decoy2.trim().equals("")) {
                UUID decoy2Uuid = UUID.randomUUID();
                repo.insert("answers", "(id, question_id, answer_text, is_correct, answer_index)",
                    asBytes(decoy2Uuid)+","+asBytes(uuid)+","+decoy2+",\"N\",2,");
            }

            if (decoy3 != null && !decoy3.trim().equals("")) {
                UUID decoy3Uuid = UUID.randomUUID();
                repo.insert("answers", "(id, question_id, answer_text, is_correct, answer_index)",
                    asBytes(decoy3Uuid)+","+asBytes(uuid)+","+decoy3+",\"N\",3,");
            }
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        response.setStatus(200);
        response.sendRedirect("/trivia/editQuestions?id=" + quizId + "&quizName=" + name);
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        UUID sid = null;
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            sid = UUID.fromString(request.getParameter("questionId"));
            repo.delete("questions", "id = "+asBytes(sid));
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        response.setStatus(200);
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
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        String[] answers = { null, null, null, null };

        try {
            repo.select("answer_text, is_correct, answer_index", "answers", "question_id = "+questionId);
            String aText;
            int aIdx;
            while (repo.rs.next()) {
                aText = repo.rs.getString(1);
                aIdx = repo.rs.getInt(3);

                answers[aIdx] = aText;
            }
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        return answers;
    }
}
