import java.util.Date;
import java.util.Calendar;
import java.util.*;
import java.sql.*;

public class Transactions {

    public String item;
    public String location;
    public long timeStamp;
    public double fee;
    public String tID;

    public Transactions(String item, String location) throws SQLException, ClassNotFoundException {
        this.item = item;
        this.location = location;
        this.timeStamp = createTimeStamp();
        this.fee = createFee();
        this.tID = makeID();
    }

    public Transactions(String item, String location, String trid, Long time, double tfee) throws SQLException, ClassNotFoundException {
        this.item = item;
        this.location = location;
        this.timeStamp = time;
        this.fee = tfee;
        this.tID = trid;
    }

    public String makeID() throws ClassNotFoundException, SQLException {
        String tID = UUID.randomUUID().toString();

        //check if it exists in the db
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();

        String sql = "select trid, count(*) from mempool where trid = '" + tID + "' group by trid;";
        ResultSet rs = s.executeQuery(sql);
        int check = 0;

        while(rs.next()){
            check = rs.getInt("count");
        }

        if(check > 0)
            tID = makeID();

        return tID;
    }


    public Transactions() {
        this.item = "";
        this.location = "";
        this.fee = 0;
        this.timeStamp = 0;
    }


    public long createTimeStamp(){
        Calendar c1 = Calendar.getInstance();
        Date d1 = c1.getTime();
        return d1.getTime();
    }

    public double createFee() throws SQLException, ClassNotFoundException{
        double f = 1 + new Random().nextDouble() * (100.00 - 1.00);
        return f;
    }

    //
    public void addToMempool() {
        // POSTGRESQL
        Connection c = null;
        Statement stmt = null;
        try {
            Class.forName("org.postgresql.Driver");
            c = DriverManager
                    .getConnection("jdbc:postgresql://34.67.145.192:5432/postgres",
                            "postgres", "xCqm5JFEJH2mrwuj");
            stmt = c.createStatement();

            String sql = "insert into mempool values ('" + item + "', '" + location + "', " + fee + ", '" + tID + "', '" + timeStamp + "');";
            stmt.executeUpdate(sql);
            System.out.println("Successfully added transaction to mempool!");

            // stmt.executeQuery(sql); grab data
            // stmt.executeUpdate(sql); change data
            stmt.close();
            c.close();
        } catch (Exception e) {
            System.err.println(e.getClass().getName() + ": " + e.getMessage());
            System.exit(0);
        }
    }





}