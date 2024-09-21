import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import BCrypt.*;
import java.sql.*;
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
    + "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" + "</head>\n"
		+ "<body bgcolor=\"#f0f0f0\">\n" + "<h1 align=\"center\">" + title + "</h1>\n";

        String errMsg = "";
        Connection con = null;
        String categories[];

		//Rendering questions Netflix style
        try {
            try {
               Class.forName("oracle.jdbc.OracleDriver");
            } catch (Exception ex) {
            }
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
            Statement stmt2 = con.createStatement();

            ResultSet categoryRS = stmt2.executeQuery("select * from categories");
            
			//loops through all the categories in categories table
			while (categoryRS.next()) {
				java.sql.Blob blob = categoryRS.getBlob("image");
				// System.out.println("Category: " + categoryRS.getString("CATEGORY_NAME"));
				html += "<div style=\"text-align: center;\">\n" +
            	"<form action=\"Quizpage\" method=\"get\">\n" +
            	"<input type=\"hidden\" name=\"category_name\" value=\" " + categoryRS.getString("CATEGORY_NAME") + "\">\n" +
            	"<input type=\"submit\" value=\" " + categoryRS.getString("CATEGORY_NAME") + "\" />\n" +
            	"</form>\n" +
            	"</div>\n";	
			}

			html += "<button onclick=\"window.location.href='main'\" >Back to Main Page</button>";

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
}
