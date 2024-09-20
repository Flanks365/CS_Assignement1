import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;

public class GetCorrectAnswerServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        Connection con = null;

        try {
            // Database connection
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");

            // Get the questionId and answerId from the request
            String questionId = request.getParameter("questionId");
            String answerId = request.getParameter("answerId");

            // Basic validation
            if (questionId == null || questionId.isEmpty() || answerId == null || answerId.isEmpty()) {
                out.print("error: missing parameters");
                return;
            }

            // Check if the answer is correct
            if (isCorrectAnswer(answerId, con)) {
                out.print("correct");                
            } else {
                out.print("incorrect");
            }

            out.flush();
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("error");
            out.flush();
        } finally {
            if (con != null) {
                try {
                    con.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Method to check if the answerId corresponds to a correct answer
    private boolean isCorrectAnswer(String answerId, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT is_correct FROM answers WHERE id = ?");
        stmt.setString(1, answerId);
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            return "Y".equals(rs.getString("is_correct"));
        }
        return false;
    }
}
