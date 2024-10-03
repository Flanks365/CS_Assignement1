import jakarta.servlet.http.*;
import jakarta.servlet.*;
import BCrypt.*;
import java.io.*;

public class LoginServlet extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>\n" + "<head><title>" + "Login" + "</title>"
            + "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" +"</head>\n" + "<body>\n"
            + "<h1 align=\"center\">" + "Login here" + "</h1>\n" + "<form action=\"login\" method=\"POST\">\n"
            + "Username: <input type=\"text\" name=\"username\">\n" + "<br />\n"
            + "Password: <input type=\"password\" name=\"password\" />\n" + "<br />\n"
            + "<input type=\"submit\" value=\"Log in\" />\n" + "</form>\n"
            + "</form>\n"  
            + "<div style=\"text-align: center;\">\n" 
		      + "<form action=\"signup\" method=\"GET\">\n" 
		      + "<input type=\"submit\" value=\"Sign up\" />\n" 
		      + "</form>\n" 
		      + "</div>\n" + "</body>\n</html\n");

   }

   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      Repository repo = new Repository();
      repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
      try {
         String user = request.getParameter("username");
         repo.select( "*", "users",  "username ='" + user + "'");
         if (!repo.rs.next()) {
            response.sendRedirect("/trivia/signup");
         } else {
            String role = repo.rs.getString("role");
            String password = repo.rs.getString("password");
            String pass = request.getParameter("password");
            if (BCrypt.checkpw(pass, password)) {
               HttpSession session = request.getSession(true);
               session.setAttribute("USER_ID", user);
               session.setAttribute("ROLE", role);
               response.setStatus(302);
               response.sendRedirect("/trivia/main");
            } else {
               response.sendRedirect("/trivia/login");
            }
         }
         System.out.println("\n\n");
         repo.close();
      } catch (Exception e) {
         System.out.println(e);
         response.sendRedirect("/trivia/signup");
      } 
   }
}
