package edu.ualberta.med.biobank.server.logging;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;

import edu.ualberta.med.biobank.common.util.LogSql;
import edu.ualberta.med.biobank.model.Log;

/**
 * 
 * Runnable class responsible for inserting the log messages in batch
 * 
 * Copy from CLM
 */
public class JDBCLogExecutor implements Runnable {

    private Log log;
    private Properties props;
    private String dbUrl = null;
    private String dbDriverClass = null;
    private String dbUser = null;
    private String dbPwd = null;

    /**
     * Constructor for JDBCExcecutor.
     * 
     * @param rows -
     */
    public JDBCLogExecutor(Log log, Properties props) {
        this.log = log;
        setProps(props);
        setDBProperties();
    }

    private void setDBProperties() {
        if (this.props != null) {

            this.setDbDriverClass(LogSql.initString(this.props
                .getProperty("hibernate.connection.driver_class")));
            this.setDbPwd(LogSql.initString(this.props
                .getProperty("hibernate.connection.password")));
            this.setDbUser(LogSql.initString(this.props
                .getProperty("hibernate.connection.username")));
            this.setDbUrl(LogSql.initString(this.props
                .getProperty("hibernate.connection.url")));
        }
    }

    @Override
    public void run() {
        try {
            Connection conn = null;
            Statement stmt = null;
            try {
                conn = createConn();
                conn.setAutoCommit(false);
                stmt = conn.createStatement();
                String statement = LogSql.getLogMessageSQLStatement(log);
                stmt.execute(statement);
                conn.commit();
            } catch (Exception e) {
                conn.rollback();
                conn.close();
                throw e;
            } finally {
                try {
                    stmt.close();
                } catch (Exception ex) {
                }
                try {
                    conn.close();
                } catch (Exception ex) {
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            ExceptionUtils.writeMsgToTmpFile("biobanklogappender", ex);
        }
    }

    protected Connection createConn() {
        Connection con = null;
        try {
            if (getDbDriverClass() != null) {
                Class.forName(getDbDriverClass());
            }
            con = DriverManager.getConnection(getDbUrl(), getDbUser(),
                getDbPwd());
        } catch (Throwable t) {
            t.printStackTrace();
        }
        return con;
    }

    public Properties getProps() {
        return props;
    }

    public void setProps(Properties props) {
        this.props = props;
    }

    public String getDbDriverClass() {

        return dbDriverClass;
    }

    public void setDbDriverClass(String dbDriverClass) {
        this.dbDriverClass = dbDriverClass;
    }

    public String getDbPwd() {
        return dbPwd;
    }

    public void setDbPwd(String dbPwd) {
        this.dbPwd = dbPwd;
    }

    public String getDbUrl() {

        return dbUrl;
    }

    public void setDbUrl(String dbUrl) {
        this.dbUrl = dbUrl;
    }

    public String getDbUser() {
        return dbUser;
    }

    public void setDbUser(String dbUser) {
        this.dbUser = dbUser;
    }

}
