import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;
import java.sql.*;
import java.util.Base64;

public class GetCorrectAnswerServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");  // Return plain text response
        PrintWriter out = response.getWriter();
        Connection con = null;

        try {
            // Database connection
            Class.forName("oracle.jdbc.OracleDriver");
            con = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");

            // Get the questionId and answerId from the request
            String questionId = request.getParameter("questionId");
            String answerId = request.getParameter("answerId");

            if (questionId == null || questionId.isEmpty() || answerId == null || answerId.isEmpty()) {
                out.print("error");  // Output error message
                return;
            }

            // Ensure both questionId and answerId only contain alphanumeric characters
            questionId = makeAlphanumeric(questionId);
            answerId = makeAlphanumeric(answerId);

            // Decode URL-safe Base64-encoded questionId and answerId to byte arrays
            byte[] questionIdBytes = Base64.getUrlDecoder().decode(questionId);
            byte[] answerIdBytes = Base64.getUrlDecoder().decode(answerId);

            // Retrieve the correct answer ID
            String correctAnswerId = getCorrectAnswerId(questionIdBytes, con);

            // Compare the provided answerId with the correctAnswerId
            if (correctAnswerId != null && correctAnswerId.equals(Base64.getUrlEncoder().encodeToString(answerIdBytes))) {
                out.print("correct");  // Answer is correct
            } else {
                out.print("incorrect");  // Answer is incorrect
            }

            out.flush();
        } catch (IllegalArgumentException e) {
            // Catch any Base64 decoding issues
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.print("error");
            out.flush();
        } catch (SQLException e) {
            // SQL-related errors
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("error");
            out.flush();
        } catch (Exception e) {
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

    // Function to ensure the string is alphanumeric (strips out non-alphanumeric characters)
    private String makeAlphanumeric(String input) {
        return input.replaceAll("[^A-Za-z0-9]", ""); // Strip out all non-alphanumeric characters
    }

    private String getCorrectAnswerId(byte[] questionIdBytes, Connection con) throws SQLException {
        PreparedStatement stmt = con.prepareStatement("SELECT id FROM answers WHERE question_id = ? AND is_correct = 'Y'");
        stmt.setBytes(1, questionIdBytes); // Use byte array for RAW(16) question ID
        ResultSet rs = stmt.executeQuery();

        if (rs.next()) {
            byte[] correctAnswerIdBytes = rs.getBytes("id"); // Get RAW(16) id as byte array
            return Base64.getUrlEncoder().encodeToString(correctAnswerIdBytes); // Convert the correct answer ID to URL-safe Base64 string
        }
        return null; // No correct answer found
    }
}
