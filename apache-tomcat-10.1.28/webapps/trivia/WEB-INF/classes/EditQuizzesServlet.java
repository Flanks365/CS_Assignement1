import jakarta.servlet.http.*;
import jakarta.servlet.*;
import jakarta.servlet.annotation.MultipartConfig;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.UUID;
import java.nio.*;


import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;


@MultipartConfig
public class EditQuizzesServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        HttpSession session = request.getSession(false);

        List<DataBundle> sender = new ArrayList<>();

        if (session == null) {
            response.setStatus(302);
            response.sendRedirect("login");
        }
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            repo.select("id, category_name", "categories");
            boolean hasResults = false;
            UUID sid = null;
            String name = null;

            while (repo.rs.next()) {
                byte[] raw = repo.rs.getBytes(1);
                sid = asUuid(raw);
                name = repo.rs.getString(2);
                System.out.println(name);

                DataBundle data = new DataBundle(sid, name);

                sender.add(data);
            }
    
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }
   
        Gson gson = new Gson();
        String jsonResponse = gson.toJson(sender);

        System.out.println(jsonResponse);

        PrintWriter out = response.getWriter();
        out.write(jsonResponse);
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        String name = request.getParameter("QuizName");
        Part filePart = request.getPart("FileName");

        System.out.println(filePart.getSubmittedFileName());

        String contentType = filePart.getContentType();
        System.out.println(contentType);
        
        UUID quizId = null;
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            try {
                quizId = UUID.fromString(request.getParameter("id"));
            } catch (Exception ex) {
                System.out.println("Error: " + ex.getMessage());
            }
            if (quizId != null) {
                String quizIdAsString = quizId.toString().replace("-", "");
                repo.update("categories", "category_name = '"+name+"', image_type = '"+contentType
                    + "', image = ?, id = '"+quizIdAsString+"'",filePart.getInputStream());
            } else {
                quizId = UUID.randomUUID();
                String quizIdAsString = quizId.toString().replace("-", "");
                repo.insert("categories", "category_name, image_type, image, id", 
                "'"+name+"','"+contentType+"',?,'"+quizIdAsString+"'","stream",filePart.getInputStream());
            }
            repo.close();
        } catch (Exception e) {
            System.out.println(e);
        }
        response.setStatus(200);

        response.sendRedirect("/trivia/editQuizzes");
    }

    @Override
    protected void doDelete(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, java.io.IOException {
        System.out.println("In doDelete");
        System.out.println(request.getParameter("id"));
        UUID quizId = null;
        Repository repo = new Repository();
        repo.init("jdbc:oracle:thin:@localhost:1521:XE", "system", "oracle1");
        try {
            quizId = UUID.fromString(request.getParameter("id"));
            String quizIdAsString = quizId.toString().replace("-", "").toUpperCase();
            repo.delete("categories", "id = '"+quizIdAsString+"'");
            repo.close();

        } catch (Exception e) {
            System.out.println(e);
        }
        response.setStatus(200);
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

    public class DataBundle{

    private UUID sid;
    private String name;

    public DataBundle(UUID a, String b){
       this.sid = a;
       this.name = b;
    };
};

}


