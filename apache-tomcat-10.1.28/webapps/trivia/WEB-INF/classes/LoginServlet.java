import jakarta.servlet.http.*;
import jakarta.servlet.*;
import BCrypt.*;
import java.sql.*;
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
            + "<input type=\"submit\" value=\"Sign in\" />\n" + "</form>\n"
            + "</form>\n" + "</body>\n</html\n");

   }

   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
         String user = request.getParameter("username");
         ResultSet rs = stmt2.executeQuery("select * from users WHERE username ='" + user + "'");
         
        if (!rs.next()) {
         
         response.sendRedirect("/trivia/signup");
         } else {
         
         String role = rs.getString("role");
         String password = rs.getString("password");

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
         }
         response.sendRedirect("/trivia/signup");
      }
    
   }
}
