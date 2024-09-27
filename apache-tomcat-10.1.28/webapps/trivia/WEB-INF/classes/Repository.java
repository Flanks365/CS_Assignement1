import java.io.InputStream;
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

  public void insert(String tableString, String setString, String valueString) {
    try {
      PreparedStatement stmt = con.prepareStatement("insert into "+tableString+"("+setString+") values ("+valueString+")");
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

  public void insert(String tableString, String setString, String valueString, InputStream is) {
    try {
      PreparedStatement stmt = con.prepareStatement("insert into "+tableString+"("+setString+") values ("+valueString+")");
      stmt.setBlob(1, is);
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

  public void insert(String tableString, String valueString) {
    try {
      PreparedStatement stmt = con.prepareStatement("insert into "+" values ("+valueString+")");
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

  public void update(String tableString, String setString, String conditionString) {
    try {
      PreparedStatement stmt = con.prepareStatement("update "+ tableString +" set " + setString + "where" + conditionString);
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

  public void delete(String tableString, String conditionString) {
    try {
      PreparedStatement stmt = con.prepareStatement("delete from "+tableString+" where " + conditionString);
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

  public byte[] getBlobAsBytes(String columnName) {
    byte bArr[] = null;
    try {
      Blob b = rs.getBlob(columnName);
      bArr = b.getBytes(1, (int) b.length());
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
    return bArr;
  }
  
}