import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;

public class GetCorrectAnswerServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        Connection con = null;

        try {
            // Database connection
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");

            // Get the question ID from the request
            int questionId = Integer.parseInt(request.getParameter("questionId"));

            // Retrieve the correct answer ID
            int correctAnswerId = getCorrectAnswerId(questionId, con);

            // Prepare JSON response
            out.print("{\"correctAnswerId\": " + correctAnswerId + "}");
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("{\"error\": \"An error occurred\"}");
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

    private int getCorrectAnswerId(int questionId, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT id FROM answers WHERE question_id = ? AND is_correct = 'T'");
        stmt.setInt(1, questionId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) {
            return rs.getInt("id");
        }
        return -1; // No correct answer found
    }
}
