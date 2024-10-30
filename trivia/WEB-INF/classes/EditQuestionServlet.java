import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.*;
import java.util.*;
import java.nio.*;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;

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

        

            List<DataBundle> sender = new ArrayList<>();
        System.out.println();
         response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        

        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {

            String quizName = request.getParameter("quizName");
            System.out.println("params: " + Arrays.toString(request.getParameterValues("quizName")));

            UUID quizId = UUID.fromString(request.getParameter("id"));

            String id= request.getParameter("id").replace("-","").toUpperCase();
            repo.select("id, question_text, media_type, media_preview", "questions", "category_id = '"+id+"'");
            boolean hasResults = false;
            UUID sid = null;
            String[] answers = null;

            

            while (repo.rs.next()) {
                if (!hasResults) {
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

                DataBundle a = new DataBundle(quizName,sid,corrAns,decAns1,decAns2,decAns3,
                mediaType,mediaPreview,question);


                sender.add(a);
            }
         
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }

        Gson gson = new Gson();
        // Convert to JSON
        String jsonResponse = gson.toJson(sender);
    

        try {
             PrintWriter out = response.getWriter();
             out.write(jsonResponse);
            out.flush(); // Ensure everything is written out
             out.close();
            } catch (IOException e) {
          System.err.println("Error writing response: " + e.getMessage());
};
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

        System.out.println("Quiz id: " + quizId);
        System.out.println("Quiz Name: " + name);

        System.out.println("questionId: " + sid);
        System.out.println("answer: " + answer);
        System.out.println("decoy1: " + decoy1);
        System.out.println("decoy2: " + decoy2);
        System.out.println("decoy3: " + decoy3);
        System.out.println("Content Type: " + contentType);

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
                String qid = request.getParameter("questionId").replace("-", "").toUpperCase();
                repo.delete("answers", "question_id = '"+qid+"'");
                repo.delete("questions", "id = '"+qid+"'");
            }

            UUID uuid = UUID.randomUUID();
            String id = uuid.toString().replace("-", "").toUpperCase();
            String quizIDString = request.getParameter("id").replace("-", "").toUpperCase();
            repo.insert("questions","id, category_id, question_text, media_type, media_content, media_preview",
                "'"+id+"','"+quizIDString+"','"+question+"','"+contentType+"',?,'"+contentPreview+"'", "blob", is);
            UUID answerUuid = UUID.randomUUID();
            String answerId = answerUuid.toString().replace("-", "").toUpperCase();
            repo.insert("answers", "id, question_id, answer_text, is_correct, answer_index",
                "'"+answerId+"','"+id+"','"+answer+"','Y',0");
            UUID decoy1Uuid = UUID.randomUUID();
            String decoy1Id = decoy1Uuid.toString().replace("-", "").toUpperCase();
            repo.insert("answers", "id, question_id, answer_text, is_correct, answer_index",
                "'"+decoy1Id+"','"+id+"','"+decoy1+"','N',1");

            if (decoy2 != null && !decoy2.trim().equals("")) {
                UUID decoy2Uuid = UUID.randomUUID();
                String decoy2Id = decoy2Uuid.toString().replace("-", "").toUpperCase();
                repo.insert("answers", "id, question_id, answer_text, is_correct, answer_index",
                    "'"+decoy2Id+"','"+id+"','"+decoy2+"','N',2");
            }

            if (decoy3 != null && !decoy3.trim().equals("")) {
                UUID decoy3Uuid = UUID.randomUUID();
                String decoy3Id = decoy3Uuid.toString().replace("-", "").toUpperCase();
                repo.insert("answers", "id, question_id, answer_text, is_correct, answer_index",
                    "'"+decoy3Id+"','"+id+"','"+decoy3+"','N',3");
            }
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        response.setStatus(200);
    }

    public class DataBundle{
                public String quizName = null;
                public UUID sid = null;
                public String corrAns;
                public  String decAns1;
                public  String decAns2;
                public  String decAns3;

                public  String media_type;
                public  String media_preview;

                public  String question;

                public DataBundle(String qName, UUID id, String corr, String decoy,
                String decoy2, String decoy3, String medTyp, String medPre, String quest){
                        this.quizName = qName;
                        this.sid = id;
                        this.corrAns = corr;
                        this.decAns1 = decoy;
                        this.decAns2 = decoy2;
                        this.decAns3 = decoy3;

                        this.media_type = medTyp;
                        this.media_preview = medPre;

                        this.question = quest;
                }
            }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

                System.out.println("delete has been requested");
        UUID sid = null;
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            System.out.println(request.getParameter("questionId"));
            sid = UUID.fromString(request.getParameter("questionId"));
            String id = sid.toString().replace("-", "").toUpperCase();

            System.out.println("this is the qid being delete: " + id);
            repo.delete("questions", "id ='"+id+"'");
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
            String id = asUuid(questionId).toString().replace("-", "").toUpperCase();
            repo.select("answer_text, is_correct, answer_index", "answers", "question_id = '"+id+"'");
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
