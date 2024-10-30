import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.nio.*;
import java.sql.SQLException;
import java.util.stream.*;

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
					"<form action=\"editQuiz/index.html\" method=\"GET\">\n" +
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

		html += "<form>" +
				"<input type=\"button\" onclick=\"window.location.href='moderatedQuiz.html';\" value=\"Join Moderated Quiz\"/>\n"
				+
				"</form>";

		// Categories
		html += "<div class=\"categories\">";

		// Rendering questions Netflix style
		Repository repo = new Repository();
		repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
		repo.select("*", "categories");

		// Contains each category cards info
		List<Category> categoriesInfo = getCategories(repo);

		// Will contain each category card
		final ArrayList<String> categoryStrings = new ArrayList<String>();

		// Dynamically creates cards for each category in the database
		categoriesInfo.stream().forEach(category -> {
			categoryStrings.add("<br><br><div class=\"categoryContainer\" style=\"display:flex;\">\n" +
					"<h3>" + category.getName() + "</h3>\n" +
					"<img class=\"categoryImg\" src=\"data:" + category.getImageType() + ";base64," +
					Base64.getEncoder().encodeToString(category.getImage()) + "\" />" +
					"<form style=\"border:0px;\" action=\"Quizpage\" method=\"get\">\n" +
					"<input type=\"hidden\" name=\"category_name\" value=\" " + category.getName() + "\">\n"
					+
					"<input type=\"hidden\" name=\"autoplay\" value=\"false\">\n" +
					"<input type=\"submit\" value=\" Play Quiz" + "\" />\n" +
					"</form>\n" +
					"<form style=\"border:0px;\" action=\"Quizpage\" method=\"get\">\n" +
					"<input type=\"hidden\" name=\"category_name\" value=\" " + category.getName() + "\">\n"
					+
					"<input type=\"hidden\" name=\"autoplay\" value=\"true\">\n" +
					"<input type=\"submit\" value=\"Autoplay Quiz\" />\n" +
					"</form>\n" +
					"</div><br><br>\n");
		});

		// Puts all the cards in the html string
		for (int i = 0; i < categoryStrings.size(); i++) {
			html += categoryStrings.get(i);
		}

		html += "</div>";
		html += "<br><br><br><form action=\"main\" method=\"get\">" +
				"<input type=\"submit\" value=\"Back to Main Page\"/>\n" +
				"</form>";

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

	// Creates a list of Category objects that contain the category info from the
	// database
	public List<Category> getCategories(Repository repo) {
		List<Category> categories = new ArrayList<>();
		try {
			while (repo.rs.next()) {
				String categoryName = repo.rs.getString("CATEGORY_NAME");
				String imgType = repo.rs.getString(3);
				byte[] image = repo.getBlobAsBytes("image");

				categories.add(new Category(categoryName, imgType, image));
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return categories;
	}
}

// Holds category info for single category
class Category {
	private String name;
	private String imageType;
	private byte[] image;

	public Category(String name, String imageType, byte[] image) {
		this.name = name;
		this.imageType = imageType;
		this.image = image;
	}

	public String getName() {
		return name;
	}

	public String getImageType() {
		return imageType;
	}

	public byte[] getImage() {
		return image;
	}
}
