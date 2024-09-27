public interface IRepository  {
  void init(String connectString, String user, String password);
  void close();
  void insert(String uuidAsString,String user_id, String hashedPassword);
  void update();
  void delete();
  void select(String fieldString, String tableString, String conditionString);
  void select(String fieldString, String tableString);
}