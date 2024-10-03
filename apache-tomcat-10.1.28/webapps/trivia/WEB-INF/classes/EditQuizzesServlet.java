import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.nio.*;

@MultipartConfig
public class EditQuizzesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);
        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
        }
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";

        String html = docType + "<html>\n" +
                "<head><title>Edit Quizzes</title>" +
                "<script src=\"resources/js/editQuizzes.js\" async></script>" +
                "<link rel=\"stylesheet\" href=\"resources/css/styles.css\" type=\"text/css\">\n" + "</head>\n" +
                "<body>\n" +
                "<h1 align=\"center\">Edit Quizzes</h1>\n" +
                "<p>Create a new quiz:</p>" +
                "<div id=\"create-quiz-container\">" +
                "<form id=\"create-quiz-form\" action=\"editQuizzes\" method=\"POST\" enctype=\"multipart/form-data\">"
                +
                "<input required id=\"new-quiz-name\" type=\"text\" name=\"QuizName\" placeholder=\"Name\" />" +
                "Image: <input required type=\"file\" name=\"FileName\" accept=\"image/*\" />" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>" +
                "<button id=\"quiz-create-button\" value=\"Create\">Create</button>\n" +
                "</div>\n";
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            repo.select("id, category_name", "categories");
            boolean hasResults = false;
            UUID sid = null;
            String name = null;

            while (repo.rs.next()) {
                if (!hasResults) {
                    html += "<p>Or edit an existing quiz: </p>";
                    hasResults = true;
                }

                byte[] raw = repo.rs.getBytes(1);
                sid = asUuid(raw);
                name = repo.rs.getString(2);
                System.out.println(name);
                html += name + "<div class=\"quiz-edit-container\">" +
                        "<form class=\"quiz-edit-form\" action=\"editQuizzes\" method=\"POST\" enctype=\"multipart/form-data\" >\n"
                        +
                        "<input type=\"hidden\" name=\"id\" value=\"" + sid + "\" />" +
                        "<input type=\"hidden\" name=\"quizName\" value=\"" + name + "\" />" +
                        "<input required id=\"new-quiz-name\" type=\"text\" name=\"QuizName\" " +
                        "value=\"" + name + "\" placeholder=\"Name\" />" +
                        "Image: <input required type=\"file\" name=\"FileName\" accept=\"image/*\" />" +
                        "<input type=\"submit\" value=\"Submit\"/>" +
                        "</form>" +
                        "<button class=\"quiz-edit-button\" value=\"Edit\">Edit</button>\n" +
                        "<button class=\"quiz-questions-button\" value=\"Questions\">Edit Questions</button>\n" +
                        "<button class=\"quiz-delete-button\" value=\"Delete\">Delete</button>\n" +
                        "</div>\n";
            }
            html += "<br><br><br><form action=\"main\" method=\"get\">" +
                    "<input type=\"submit\" value=\"Back to Main Page\"/>\n" +
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
        String name = request.getParameter("QuizName");
        Part filePart = request.getPart("FileName");
        String contentType = filePart.getContentType();
        UUID quizId = null;
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            try {
                quizId = UUID.fromString(request.getParameter("id"));
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
            if (quizId != null) {
                String quizIdAsString = quizId.toString().replace("-", "");
                repo.update("categories", "category_name = '"+name+"', image_type = '"+contentType
                    + "', image = ?, id = '"+quizIdAsString+"'",filePart.getInputStream());
            } else {
                quizId = UUID.randomUUID();
                String quizIdAsString = quizId.toString().replace("-", "");
                repo.insert("categories", "category_name, image_type, image, id", 
                "'"+name+"','"+contentType+"',?,'"+quizIdAsString+"'","stream",filePart.getInputStream());
            }
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        response.setStatus(200);
        response.sendRedirect("/trivia/editQuizzes");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        System.out.println("In doDelete");
        System.out.println(request.getParameter("id"));
        UUID quizId = null;
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            quizId = UUID.fromString(request.getParameter("id"));
            String quizIdAsString = quizId.toString().replace("-", "").toUpperCase();
            repo.delete("categories", "id = '"+quizIdAsString+"'");
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
}
