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

        String errMsg = "";
        Connection con = null;
        try {
            try {
                Class.forName("oracle.jdbc.OracleDriver");
            } catch (Exception ex) {
            }
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");
            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select id, category_name from categories");
            boolean hasResults = false;
            byte bArr[] = null;
            UUID sid = null;
            String name = null;

            while (rs.next()) {
                if (!hasResults) {
                    html += "<p>Or edit an existing quiz: </p>";
                    hasResults = true;
                }

                byte[] raw = rs.getBytes(1);
                sid = asUuid(raw);
                name = rs.getString(2);
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
            return;
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
        String fileName = filePart.getSubmittedFileName();
        Connection con = null;
        UUID quizId = null;

        try {
            try {
                quizId = UUID.fromString(request.getParameter("id"));
                asBytes(quizId);
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }

            String stmt = null;
            if (quizId != null) {
                stmt = "UPDATE categories SET category_name = ?, image_type = ?, image = ? where id = ?";
            } else {
                stmt = "INSERT INTO categories (category_name, image_type, image, id) VALUES (?,?,?,?)";
                quizId = UUID.randomUUID();
            }

            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
            PreparedStatement preparedStatement = con.prepareStatement(stmt);
            preparedStatement.setString(1, name);
            preparedStatement.setString(2, contentType);
            preparedStatement.setBinaryStream(3, filePart.getInputStream());
            preparedStatement.setBytes(4, asBytes(quizId));
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
        response.sendRedirect("/trivia/editQuizzes");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        System.out.println("In doDelete");
        System.out.println(request.getParameter("id"));
        UUID quizId = null;
        Connection con = null;
        try {
            quizId = UUID.fromString(request.getParameter("id"));

            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE",
                    "system", "oracle1");

            PreparedStatement preparedStatement = con
                    .prepareStatement("delete from categories where id = ?");
            preparedStatement.setBytes(1, asBytes(quizId));
            preparedStatement.executeUpdate();
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
