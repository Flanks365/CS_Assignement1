import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class MainServlet extends HttpServlet {

	public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
		HttpSession session = request.getSession(false);

		if (session == null) {
			response.setStatus(302);
			response.sendRedirect("login");
		}
		String title = "Logged in as the user name: ";
		title += session.getAttribute("USER_ID");
		response.setContentType("text/html");
		String docType = "<!doctype html public \"-//w3c//dtd html 4.0 " + "transitional//en\">\n";

		//just do play quiz button at first and have an if statement that checks if admin
		//if admin == true, load the other buttons as well

		String html = docType + "<html>\n" + "<head><title>" + title + "</title></head>\n"
		+ "<body bgcolor=\"#f0f0f0\">\n" + "<h1 align=\"center\">" + title + "</h1>\n";
		if (session.getAttribute("ROLE") == "admin") {
			html += "<div style=\"text-align: center;\">\n" +
			"<form action=\"upload\" method=\"GET\">\n" +
			"<input type=\"submit\" value=\"UPLOAD\" />\n" +
			"</form>\n" +
			"</div>\n";
		}
		html += "<div style=\"text-align: center;\">\n" +
		"<form action=\"play\" method=\"GET\">\n" +
		"<input type=\"submit\" value=\"Quizzes\" />\n" +
		"</form>\n" +
		"</div>\n" +
		"<div style=\"text-align: center;\">\n" +
		"<form action=\"logout\" method=\"GET\">\n" +
		"<input type=\"submit\" value=\"LOGOUT\" />\n" +
		"</form>\n" +
		"</div>\n" + "</body></html>";
		System.out.println("ROLE: [" + session.getAttribute("ROLE") + "]");
		PrintWriter out = response.getWriter();
		out.println(html);
	}
}
