import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
//import jakarta.servlet.http.Part;
import java.sql.*;
import java.io.*;
import java.util.*;
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
public class BlobTest extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        String docType = "<!doctype html public \"-//w3c//dtd html 4.0 transitional//en\">\n";

        String html = docType + "<html>\n" +
                "<head><title>BlobTest</title>" +
                "<link rel=\"stylesheet\" href=\"resources/css/styles.css\" type=\"text/css\">\n" + "</head>\n" +
                "<body>\n" +
                "Blob test";

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
            ResultSet rs = stmt.executeQuery("select * from categories where category_name = 'BlobTest'");
            byte bArr[] = null;
            UUID sid = null;
            String name = null;
            String imgType = null;

            if (rs.next()) {
                System.out.println("got row");
                byte[] raw = rs.getBytes(1);
                sid = asUuid(raw);
                name = rs.getString(2);
                imgType = rs.getString(3);
                Blob b = rs.getBlob(4);
				bArr = b.getBytes(1, (int) b.length());

                html += "<img src=\"data:" + imgType + ";base64," +
                    Base64.getEncoder().encodeToString(bArr) + "\" />";

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
        // String html = "<html><body>got page</body></html>";
        PrintWriter out = response.getWriter();
        out.println(html);
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
