import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.util.UUID;
import java.io.*;
import BCrypt.*;

public class SignupServlet extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>\n" + "<head><title>" + "Sign up" + "</title>"
            + "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" + "</head>\n" + "<body>\n"
            + "<h1 align=\"center\">" + "Sign up" + "</h1>\n" + "<form action=\"signup\" method=\"POST\">\n"
            + "Username: <input type=\"text\" name=\"user_id\">\n" + "<br />\n"
            + "Password: <input type=\"password\" name=\"password\" />\n" + "<br />\n"
            + "<input type=\"submit\" value=\"Sign up\" />\n" + "</form>\n"
            + "</form>\n" 
            + "<div style=\"text-align: center;\">\n" 
            + "<form action=\"login\" method=\"GET\">\n" 
            + "<input type=\"submit\" value=\"Log in\" />\n" 
            + "</form>\n" 
            + "</div>\n"+ "</body>\n</html\n");

   }

   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      boolean errorFlag = false;
      response.setContentType("text/html");
      Repository repo = new Repository();
      repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
      try {
         repo.select("*", "users", "username = '" + request.getParameter("user_id") + "'");
         if (repo.rs.next()) {
            errorFlag = true;
         } else {
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString().replace("-", "");
            String hashedPassword = BCrypt.hashpw(request.getParameter("password"), BCrypt.gensalt(10));
            repo.insert(uuidAsString, request.getParameter("user_id"), hashedPassword);
         }
         System.out.println("\n\n");
      } catch (Exception e) {
         System.out.println(e);
      }
      repo.close();

      if (errorFlag) {
         PrintWriter out = response.getWriter();
         out.println("<html>\n" + "<head><title>" + "Sign up" + "</title>"
         + "<link rel=\"stylesheet\" href=\"/trivia/resources/css/styles.css\" type=\"text/css\">\n" + "</head>\n" + "<body>\n" 
         + "<h1 align=\"center\">" + "Sign up" + "</h1>\n" 
         + "<div id='error' style= 'color:red;'> <strong> Sorry that username is already taken </strong> </div>" 
         + "<form action=\"signup\" method=\"POST\">\n"
         + "Username: <input type=\"text\" name=\"user_id\">\n" + "<br />\n"
         + "Password: <input type=\"password\" name=\"password\" />\n" + "<br />\n"
         + "<input type=\"submit\" value=\"Sign up\" />\n" + "</form>\n"
         + "</form>\n" + "</body>\n</html\n");
      } else {
         response.setContentType("text/html");
         String username = request.getParameter("user_id");
         HttpSession session = request.getSession(true);
         session.setAttribute("USER_ID", username);
         session.setAttribute("ROLE", "user");
         response.setStatus(302);
         response.sendRedirect("main");
      }
   }
}
