import java.io.InputStream;

public interface IRepository  {
  void init(String connectString, String user, String password);
  void close();
  void insert(String tableString, String valueString);
  void insert(String tableString, String setString, String valueString);
  void insert(String tableString, String setString, String valueString, String type, InputStream is);
  void update(String tableString, String setString, String conditionString);
  void update(String tableString, String setString, InputStream is);
  void delete(String tableString, String conditionString);
  void select(String fieldString, String tableString, String conditionString);
  void select(String fieldString, String tableString);
  byte[] getBlobAsBytes(String columnName);
}