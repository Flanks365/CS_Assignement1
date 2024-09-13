import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.sql.*;
import java.util.UUID;
import java.io.*;

public class SignupServlet extends HttpServlet {
   public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter out = response.getWriter();
      out.println("<html>\n" + "<head><title>" + "Sign up" + "</title></head>\n" + "<body>\n"
            + "<h1 align=\"center\">" + "Sign up" + "</h1>\n" + "<form action=\"signup\" method=\"POST\">\n"
            + "Username: <input type=\"text\" name=\"user_id\">\n" + "<br />\n"
            + "Password: <input type=\"password\" name=\"password\" />\n" + "<br />\n"
            + "<input type=\"submit\" value=\"Sign up\" />\n" + "</form>\n"
            + "</form>\n" + "</body>\n</html\n");

   }

   public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
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
         ResultSet rs = stmt2.executeQuery("select * from users");
         if (rs.next()) {
            String username = rs.getString("username");
            String password = rs.getString("password");
            System.out.println("Sorry" + username + "already exists");
            errorFlag = true;
         } else {
            System.out.println("Creating new user");
            UUID uuid = UUID.randomUUID();
            String uuidAsString = uuid.toString().replace("-", "");
            Statement insertStatement = con.createStatement();
            String query = "insert into users values ('" + uuidAsString + "','" + request.getParameter("user_id")
                  + "','" +
                  request.getParameter("password") + "', 'user')";
            System.out.println(query);
            insertStatement.executeUpdate(query);
            insertStatement.close();
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
         System.out.println(errMsg);
      }

      if (errorFlag) {
         PrintWriter out = response.getWriter();
         out.println("<html>\n" + "<head><title>" + "Sign up" + "</title></head>\n" + "<body>\n" 
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
