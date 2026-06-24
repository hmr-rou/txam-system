package hmr.utils;
import com.mchange.v2.c3p0.ComboPooledDataSource;
import javax.sql.DataSource;

//数据库连接池工具，提供 DataSource 实例，从 c3p0-config.xml 读取配置
public class C3p0Utils {
    private static DataSource ds = null;
    static {
//使用默认的配置创建数据源对象
        ds = new ComboPooledDataSource();
    }
    public static DataSource getDataSource() {
        return ds;
    }

}
