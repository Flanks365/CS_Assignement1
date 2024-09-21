import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import BCrypt.*;
import java.sql.*;
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
import jakarta.servlet.annotation.MultipartConfig;

public class CategoriesServlet extends HttpServlet {
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		if (session == null) {
			response.setStatus(302);
			response.sendRedirect("login");
		}
		String title = "Select quiz";
		response.setContentType("text/html");
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";

		String html = docType + "<html>\n" + "<head><title>" + title + "</title>"
    + "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" + 
	"</head>\n"
		+ "<body bgcolor=\"#f0f0f0\">\n" + "<h1 align=\"center\">" + title + "</h1>\n";

        String errMsg = "";
        Connection con = null;
        String categories[];

		//used for rendering image
		byte bArr[] = null;
		UUID sid = null;
		String name = null;
		String imgType = null;

		//Rendering questions Netflix style
        try {
            try {
               Class.forName("oracle.jdbc.OracleDriver");
            } catch (Exception ex) {
            }
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
            Statement stmt2 = con.createStatement();

            ResultSet categoryRS = stmt2.executeQuery("select * from categories");
            html += "<div style=\"display:flex;flex-direction:row;flex-wrap:wrap;justify-contents:center\">";
			//loops through all the categories in categories table
			while (categoryRS.next()) {
				//retrieving blob from database
                imgType = categoryRS.getString(3);
                Blob b = categoryRS.getBlob(4);
				bArr = b.getBytes(1, (int) b.length());
				
				html += "<div style=\"display:flex;\">\n";
				
            	html += "<form style=\"border:0px;\" action=\"Quizpage\" method=\"get\">\n" +
				"<img src=\"data:" + imgType + ";base64," +
                    Base64.getEncoder().encodeToString(bArr) + "\" style=\"width:180px;height:300px;align-items:center;\"/>" +
            	"<input type=\"hidden\" name=\"category_name\" value=\" " + categoryRS.getString("CATEGORY_NAME") + "\">\n" +
            	"<input type=\"submit\" value=\" " + categoryRS.getString("CATEGORY_NAME") + "\" />\n" +
            	"</form>\n" +
            	"</div>\n";	
			}
			html += "</div>";
			html += "<br><br><br><form action=\"main\" method=\"get\">" +  
			"<input type=\"submit\" value=\"Back to Main Page\"/>\n" +
			"</form>";

            stmt2.close();
            con.close();
            System.out.println("\n\n");
         } catch (SQLException ex) {
            errMsg = errMsg + "\n--- SQLException caught ---\n";
            while (ex != null) {
               errMsg += "Message: " + ex.getMessage();
               errMsg += "SQLState: " + ex.getSQLState();
               errMsg += "ErrorCode: " + ex.getErrorCode();
               ex = ex.getNextException();
               errMsg += "";
			   System.out.println(errMsg);
            }
         }

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
