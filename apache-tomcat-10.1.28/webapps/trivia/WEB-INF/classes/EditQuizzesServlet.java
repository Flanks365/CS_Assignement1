import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;
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
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";

        String html = docType + "<html>\n" +
                "<head><title>Edit Quizzes</title></head>\n" +
                "<body>\n" +
                "<h1 align=\"center\">Edit Quizzes</h1>\n" +
                "<p>Create a new quiz:</p>" +
                "<form action=\"editQuizzes\" method=\"POST\" enctype=\"multipart/form-data\">" +
                "<input id=\"new-quiz-name\" type=\"text\" name=\"QuizName\" placeholder=\"Name\" />" +
                "Image: <input type=\"file\" name=\"FileName\" accept=\"image/*\" />" +
                "<input type=\"submit\" value=\"Submit\"/>" +
                "</form>";

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
            ResultSet rs = stmt.executeQuery("select * from quizzes");
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
                html += "<form action=\"editQuizzes\" method=\"GET\">\n" +
                        name +
                        "<input type=\"submit\" value=\"Edit\" />\n" +
                        "<input type=\"submit\" value=\"Delete\" />\n" +
                        "</form>\n";
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
            // response.sendRedirect("/signup");
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
        // if (true)
        //     return;
        try {
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
            PreparedStatement preparedStatement = con
                    .prepareStatement("INSERT INTO quizzes (id, name, image_type, image) VALUES (?,?,?,?)");
            UUID uuid = UUID.randomUUID();
            preparedStatement.setBytes(1, asBytes(uuid));
            preparedStatement.setString(2, name);
            preparedStatement.setString(3, contentType);
            preparedStatement.setBinaryStream(4, filePart.getInputStream());
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
