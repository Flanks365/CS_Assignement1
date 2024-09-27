import jakarta.servlet.http.*;
import jakarta.servlet.*;
import java.io.*;

public class GetCorrectAnswerServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/plain");
        PrintWriter out = response.getWriter();
        // Database connection
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            // Get the questionId and answerId from the request
            String questionId = request.getParameter("questionId");
            String answerId = request.getParameter("answerId");

            // Basic validation
            if (questionId == null || questionId.isEmpty() || answerId == null || answerId.isEmpty()) {
                out.print("error: missing parameters");
                return;
            }

            // Check if the answer is correct
            if (isCorrectAnswer(answerId, repo)) {
                out.print("correct");                
            } else {
                out.print("incorrect");
            }

            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            out.print("error");
            out.flush();
        } finally {
            if (repo != null) {
                try {
                    repo.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    // Method to check if the answerId corresponds to a correct answer
    private boolean isCorrectAnswer(String answerId, Repository repo) {
        repo.select("is_correct", "answers", "id = "+ answerId);
        try {
            if (repo.rs.next()) {
                return "Y".equals(repo.rs.getString("is_correct"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println(e);
        }
        return false;
    }
}