import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;

public class CategoriesServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		// if (session == null) {
		// 	response.setStatus(302);
		// 	response.sendRedirect("login");
		// }
		String title = "Logged in as: user";
		// title += session.getAttribute("USER_ID");
		response.setContentType("text/html");
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";

		// just do main quiz button at first and have an if statement that checks if
		// admin
		// if admin == true, load the other buttons as well

		String html = docType + "<html>\n" + "<head><title>" + title + "</title></head>\n"
				+ "<body bgcolor=\"#f0f0f0\">\n" + "<h1 align=\"center\">" + title + "</h1>\n";
		// if (session.getAttribute("ROLE") == "admin") {
		// html += "<div style=\"text-align: center;\">\n" +
		// "<form action=\"upload\" method=\"GET\">\n" +
		// "<input type=\"submit\" value=\"UPLOAD\" />\n" +
		// "</form>\n" +
		// "</div>\n";
		// }
		boolean errorFlag = false;
		response.setContentType("text/html");
		String errMsg = "";
		Connection con = null;
		try {
			try {
				Class.forName("oracle.jdbc.OracleDriver");
			} catch (Exception ex) {

			}
			con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
			Statement stmt2 = con.createStatement();
			ResultSet rs = stmt2.executeQuery("select * from categories");
			int id = 0;
			while (rs.next()) {
				String category = rs.getString("category");
				html += "<div id=" + id++ +" style=\"text-align: center;\">\n" + "<form action=\"quiz\" method=\"GET\">\n"
						+ "<input type=\"submit\" value=\"" + category + "\" />\n" + "<input type=\"hidden\" name=\"category\" value=\"" + category + "\" />\n"
						+ "</form>\n" + "</div>\n";
			}
			stmt2.close();
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
}
