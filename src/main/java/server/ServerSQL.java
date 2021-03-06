package server;

import java.sql.*;

public class ServerSQL {
    private Connection conn = null;

    public ServerSQL() {

        ServerConfig cfg = new ServerConfig();


        try {
            conn = DriverManager.getConnection(cfg.getProperty("sqluri", "jdbc:mysql://localhost:3306/test?user=test&autoReconnect=true"));
        } catch (SQLException ex) {

            ServerMain.debug("SQLException: " + ex.getMessage());
            ServerMain.debug("SQLState: " + ex.getSQLState());
            ServerMain.debug("VendorError: " + ex.getErrorCode());
            ServerMain.error("SQL ühendumine katki", ex); // error() laseb prgrammi niikuinii õhku
            //throw new RuntimeException(ex);
        }

    }

    // selle classi töö testimiseks. Normaalolus pole vaja käivitada
    public static void main(String[] argv) throws Exception {


        ServerSQL sql = new ServerSQL();
        Connection conn = sql.getConn();

        System.out.println("insertID: " + sql.insert("insert into test (id,data) values (0,?)", "Mingi 'imelik\" data"));
        System.out.println(sql.getstring("select data from test where id>?", "188"));


        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();

            String query = "SELECT id,data FROM test order by id desc limit 1";
            // create the java statement
            Statement st = conn.createStatement();

            // execute the query, and get a java resultset
            rs = st.executeQuery(query);

            // iterate through the java resultset
            while (rs.next()) {
                System.out.println(rs.getInt("id") + " " + rs.getString("data"));
            }
            st.close();

        } finally {

            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException sqlEx) {
                } // ignore

                rs = null;
            }

            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException sqlEx) {
                } // ignore

                stmt = null;
            }


        } // finally


    } // main

    public Connection getConn() {
        return conn;
    }

    public int query(String... args) throws Exception {
        synchronized (conn) {
            PreparedStatement preparedStmt = conn.prepareStatement(args[0]);
            for (int i = 1; i < args.length; i++) {
                preparedStmt.setString(i, args[i]);
            }
            preparedStmt.execute();
        } // sync
        return 0;
    } // query

    public int insert(String... args) {  // feilimise returnime 0 ja feili põhjus meid väga ei huvita
        synchronized (conn) {
            Statement stmt = null;
            ResultSet rs = null;
            int autoIncKeyFromApi = 0;
            try {
                stmt = conn.createStatement();
                PreparedStatement preparedStmt = conn.prepareStatement(args[0], Statement.RETURN_GENERATED_KEYS);
                for (int i = 1; i < args.length; i++) {
                    preparedStmt.setString(i, args[i]);
                }
                preparedStmt.execute();


                rs = preparedStmt.getGeneratedKeys();
                if (rs.next()) {
                    autoIncKeyFromApi = rs.getInt(1);
                } else {
                    // insert ebaõnnestus, tagastame ikkagi 0
                    ServerMain.debug(6, "SQL insert failed: " + args[0]);
                }
            } catch (Exception e) {
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (SQLException sqlEx) {
                    } // ignore

                    rs = null;
                }

                if (stmt != null) {
                    try {
                        stmt.close();
                    } catch (SQLException sqlEx) {
                    } // ignore

                    stmt = null;
                }
            }

            return autoIncKeyFromApi;
        }
    }


    public String getstring(String... args) throws Exception {
        synchronized (conn) {

            PreparedStatement preparedStatement = conn.prepareStatement(args[0]);
            String response = "";
            for (int i = 1; i < args.length; i++) {
                preparedStatement.setString(i, args[i]);
            }
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                response = rs.getString(1);
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            return response;

        }
    } // getstring

    public String[] query(int mitu, String... args) throws Exception {
        synchronized (conn) {

            PreparedStatement preparedStatement = conn.prepareStatement(args[0]);
            String[] response = new String[mitu];
            for (int i = 1; i < args.length; i++) {
                preparedStatement.setString(i, args[i]);
            }
            ResultSet rs = preparedStatement.executeQuery();

            if (rs.next()) {
                for (int i = 0; i < mitu; i++) {
                    response[i] = rs.getString(i + 1);
                }

            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            return response;

        }
    } // query


}