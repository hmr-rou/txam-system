package hmr.javabean;

//用户实体类，映射 user 表
public class User {
    private int id;
    private String idCardNumber;
    private String name;
    private String password;
    private String salt;
    private String role;

    public User() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getIdCardNumber() { return idCardNumber; }
    public void setIdCardNumber(String idCardNumber) { this.idCardNumber = idCardNumber; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getSalt() { return salt; }
    public void setSalt(String salt) { this.salt = salt; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}