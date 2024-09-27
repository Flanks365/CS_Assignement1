import java.sql.*;

public class Repository implements IRepository{
  Connection con;
  ResultSet rs;
  Statement stmt;
  Repository() {
    con = null;
    rs = null;
    stmt = null;
  }

  public void init(String connectString, String user, String password) {
    try {
      con = DriverManager.getConnection(connectString, user, password);
    } catch (SQLException ex) {
      String errMsg = "";
      errMsg += "\n--- SQLException caught ---\n";
      while (ex != null) {
         errMsg += "Message: " + ex.getMessage();
         errMsg += "SQLState: " + ex.getSQLState();
         errMsg += "ErrorCode: " + ex.getErrorCode();
         ex = ex.getNextException();
         errMsg += "";
      }
      System.out.println(errMsg);    
    }
  }

  public void close() {
    try {
      con.close();
    } catch (SQLException ex) {
      String errMsg = "";
      errMsg += "\n--- SQLException caught ---\n";
      while (ex != null) {
         errMsg += "Message: " + ex.getMessage();
         errMsg += "SQLState: " + ex.getSQLState();
         errMsg += "ErrorCode: " + ex.getErrorCode();
         ex = ex.getNextException();
         errMsg += "";
      }
      System.out.println(errMsg);    
    }
  }

  public void insert(String uuidAsString,String user_id, String hashedPassword) {
    try {
      PreparedStatement stmt = con.prepareStatement("insert into users values ('" + uuidAsString + "','" + user_id + "','" + hashedPassword + "', 'user')");
      rs = stmt.executeQuery();
    } catch (SQLException ex) {
      String errMsg = "";
      errMsg += "\n--- SQLException caught ---\n";
      while (ex != null) {
         errMsg += "Message: " + ex.getMessage();
         errMsg += "SQLState: " + ex.getSQLState();
         errMsg += "ErrorCode: " + ex.getErrorCode();
         ex = ex.getNextException();
         errMsg += "";
      }
      System.out.println(errMsg);    
    }
  }

  public void update() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'update'");
  }

  public void delete() {
    // TODO Auto-generated method stub
    throw new UnsupportedOperationException("Unimplemented method 'delete'");
  }

  public void select(String fieldString, String tableString, String conditionString) {
    try {
      PreparedStatement stmt = con.prepareStatement("select "+fieldString+" from "+tableString+" where "+conditionString);
      rs = stmt.executeQuery();
    } catch (SQLException ex) {
      String errMsg = "";
      errMsg += "\n--- SQLException caught ---\n";
      while (ex != null) {
         errMsg += "Message: " + ex.getMessage();
         errMsg += "SQLState: " + ex.getSQLState();
         errMsg += "ErrorCode: " + ex.getErrorCode();
         ex = ex.getNextException();
         errMsg += "";
      }
      System.out.println(errMsg);    
    }
  }

  public void select(String fieldString, String tableString) {
    try {
      PreparedStatement stmt = con.prepareStatement("select "+fieldString+" from "+tableString);
      rs = stmt.executeQuery();
    } catch (SQLException ex) {
      String errMsg = "";
      errMsg += "\n--- SQLException caught ---\n";
      while (ex != null) {
         errMsg += "Message: " + ex.getMessage();
         errMsg += "SQLState: " + ex.getSQLState();
         errMsg += "ErrorCode: " + ex.getErrorCode();
         ex = ex.getNextException();
         errMsg += "";
      }
      System.out.println(errMsg);    
    }
  }
  
}