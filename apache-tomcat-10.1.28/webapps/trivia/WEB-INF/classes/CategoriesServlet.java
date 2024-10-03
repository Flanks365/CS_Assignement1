import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Base64;
import java.util.UUID;
import java.nio.*;

public class CategoriesServlet extends HttpServlet {
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		if (session == null) {
			response.setStatus(302);
			response.sendRedirect("login");
			return;
		}
		String title = "Select quiz";
		response.setContentType("text/html");
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";

		String html = docType + "<html>\n" + "<head><title>" + title + "</title>"
				+ "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" +
				"</head>\n"
				+ "<body bgcolor=\"#f0f0f0\">\n";

		html += "<div id=\"controlButtons\">";

		String role = (String) session.getAttribute("ROLE");

		if (role.equals("admin")) {
			System.out.println("in admin");
			html += "<div style=\"text-align: center;\">\n" +
					"<form action=\"editQuizzes\" method=\"GET\">\n" +
					"<input type=\"submit\" value=\"Edit Quizzes\" />\n" +
					"</form>\n" +
					"</div>\n";
		}
		html += "<div style=\"text-align: center;\">\n" +
				"<form action=\"logout\" method=\"GET\">\n" +
				"<input type=\"submit\" value=\"LOGOUT\" />\n" +
				"</form>\n" +
				"</div>\n" + "</body></html>";

		html += "</div>" + "<h1 align=\"center\">" + title + "</h1>\n";

		// used for rendering image
		byte bArr[] = null;
		String imgType = null;

		// Rendering questions Netflix style
		Repository repo = new Repository();
    repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
    try {
			repo.select("*", "categories");
			boolean hasNextTuple = true; // used to check if there is another line in the categories table
			if (!repo.rs.next()) {
				html += "<br><br><br><h2 align=\"center\">No quizzes to display</h2>";
				hasNextTuple = false;
			}
			html += "<div class=\"categories\">";
			// loops through all the categories in categories table
			while (hasNextTuple) {

				// retrieving blob from database
				imgType = repo.rs.getString(3);
				bArr = repo.getBlobAsBytes("image");

				html += "<br><br><div class=\"categoryContainer\" style=\"display:flex;\">\n";

				html += "<h3>" + repo.rs.getString("CATEGORY_NAME") + "</h3>\n" +
						"<img class=\"categoryImg\" src=\"data:" + imgType + ";base64," +
						Base64.getEncoder().encodeToString(bArr) + "\" />" +
						"<form style=\"border:0px;\" action=\"Quizpage\" method=\"get\">\n" +
						"<input type=\"hidden\" name=\"category_name\" value=\" " + repo.rs.getString("CATEGORY_NAME") + "\">\n"
						+
						"<input type=\"hidden\" name=\"autoplay\" value=\"false\">\n" +
						"<input type=\"submit\" value=\" Play Quiz" + "\" />\n" +
						"</form>\n" +
						"<form style=\"border:0px;\" action=\"Quizpage\" method=\"get\">\n" +
						"<input type=\"hidden\" name=\"category_name\" value=\" " + repo.rs.getString("CATEGORY_NAME") + "\">\n"
						+
						"<input type=\"hidden\" name=\"autoplay\" value=\"true\">\n" +
						"<input type=\"submit\" value=\"Autoplay Quiz\" />\n" +
						"</form>\n" +
						"</div><br><br>\n";
				if (!repo.rs.next()) {
					break;
				}
			}
			html += "</div>";
			html += "<br><br><br><form action=\"main\" method=\"get\">" +
					"<input type=\"submit\" value=\"Back to Main Page\"/>\n" +
					"</form>";

			repo.close();
			System.out.println("\n\n");
		} catch (Exception e) {
			System.out.println(e);	
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
