import java.util.*;
import java.sql.*;

public class Miner {
    public long startTime;
    public String mID;
    public int length = 0;

    public Miner() throws SQLException, ClassNotFoundException {
        mID = makeID();
    }

    // generate random ID
    public String makeID() throws ClassNotFoundException, SQLException {
        String mID = UUID.randomUUID().toString();

        //check if it exists in the db
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();

        String sql = "select mid, count(*) from minedblocks where mid = '" + mID + "' group by mid;";
        ResultSet rs = s.executeQuery(sql);
        int check = 0;

        while(rs.next()){
            check = rs.getInt("count");
        }

        if(check > 0)
            mID = makeID();

        con.close();

        return mID;
    }

    // gets linked list of all transactions - a copy of the MEMPOOL
    public LinkedList<Transactions> getTransactions() throws SQLException, ClassNotFoundException{
        LinkedList<Transactions> transactions = new LinkedList<Transactions>();
        Transactions t = null;
        int i = 0;
        String item = "", location = "", trid = "";
        double fee = 0.0;
        long time = 0L;

        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();

        String sql = "select * from mempool;";
        ResultSet rs = s.executeQuery(sql);

        while(rs.next()){
            System.out.println("*------------ TRANSACTION " + i + " ------------*");

            item = rs.getString("item");
            System.out.println("Item: " + item);

            location = rs.getString("location");
            System.out.println("Location: " + location);

            fee = rs.getDouble("fee");
            System.out.println("Fee: " + fee);

            time = rs.getLong("dateOf");
            System.out.println("Timestamp: " + time);

            trid = rs.getString("trid");

            t = new Transactions(item, location, trid, time, fee);

            transactions.add(t);
            i++;
        }
        System.out.println("*******************************");

        con.close();

        return(transactions);
    }

    // update node's MEMPOOL to remove recently mined block
    public LinkedList<Transactions> updateTransactions(Transactions t, LinkedList<Transactions> transactions){
        // search node's MEMPOOL for id
        for(int i = 0; i < transactions.size(); i++){
            if((transactions.get(i).tID).equals(t.tID)){
                transactions.remove(i);
            }
        }

        return transactions;
    }

    // search through node's MEMPOOL to grab transaction with greatest fee
    public Transactions getHighestTransaction(LinkedList<Transactions> transactions) {
        Transactions t = transactions.get(0);
        Transactions tCheck;

        for (int i = 1; i < transactions.size(); i++) {
            // grab each element of MEMPOOL
            tCheck = transactions.get(i);

            // compare current highest with recently grabbed element
            if (t.fee <= tCheck.fee) {

                // storing current highest
                t = tCheck;
            }
        }

        System.out.println("Transaction with highest fee: ");
        System.out.println("Fee: " + t.fee);
        System.out.println("Item: " + t.item);
        System.out.println("Location: " + t.location);

        System.out.println("*******************************");

        return t;
    }

    // uses highest transaction info from node MEMPOOL to generate hash based on difficulty of node, then append to
    // temporary minedBlocks database
    public BlockNodes mineBlock(int difficulty, int i, Transactions t, LinkedList<BlockNodes> blockChain) throws SQLException, ClassNotFoundException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();
        String sql = "";
        ResultSet rs;

        BlockNodes b = new BlockNodes();
        b.timeStamp = t.timeStamp;
        b.mID = this.mID;
        b.tID = t.tID;
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!b.currHash.substring( 0, difficulty).equals(target)) {
            b.nonce++;
            b.currHash = b.calculateHash();
        }
        System.out.println("Mine Successful. Hash is: " + b.currHash);

        // get blocknum & prevHash
        if(i == 0){
            sql = "select blocknum from foodchain where blocknum = (select max(blocknum) from foodchain);";
            rs = s.executeQuery(sql);

            while(rs.next()){
                b.blockNum = rs.getInt("blocknum") + 1;
            }

            // get prevHash from database
            sql = "select currhash from foodchain where blocknum = " + (b.blockNum - 1) + ";";
            rs = s.executeQuery(sql);

            while(rs.next()){
                b.prevHash = rs.getString("currhash");
            }
        }
        else{
            b.blockNum = i+1;
            b.prevHash = blockChain.get(blockChain.size()-1).currHash; // grab hash value of previous block in temporary node chain
        }

        // get prevHash
        sql = "select currhash from foodchain where blocknum = " + (b.blockNum - 1) + ";";
        rs = s.executeQuery(sql);

        while(rs.next()){
            b.prevHash = rs.getString("currhash");
        }

        // set transaction values to block
        b.item = t.item;
        b.location = t.location;

        addToMinedBlocks(t);

        con.close();

        return b;
    }

    // get time for hash value generation
    public double elapsedTime() {
        long now = System.currentTimeMillis();
        return (now - startTime) / 1000.0;
    }

    // add miner's mined blocks to a temporary table
    public void addToMinedBlocks(Transactions t) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();
        String sql = "insert into minedblocks values ('" + mID + "', '" + t.tID + "');";

        s.executeUpdate(sql);

        con.close();
    }

    // check to see if any miner has same tID as another mID
    public LinkedList<String> checkMinedBlocks(Transactions t) throws ClassNotFoundException, SQLException {
        LinkedList<String> mIDs = new LinkedList<>();
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();
        String sql = "select distinct mid from minedblocks where mid != '" + mID + "' AND trid = '" + t.tID + "';";

        ResultSet rs = s.executeQuery(sql);

        while(rs.next()){
            mIDs.add(rs.getString("mid"));
        }

        con.close();

        if(mIDs.isEmpty())
            return null;

        return mIDs;
    }

    // competing chairs - check length of miner chain through IDs in foodchain table
    // get length of chain miner has mined
    // only call this when we see overlapping trids
    public boolean checkChainLength(Transactions t) throws ClassNotFoundException, SQLException {

        if (checkMinedBlocks(t) == null)
            return true; // no competitors

        LinkedList<String> mIDs = checkMinedBlocks(t);

        int competingLength = 0;

        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();

        String sql = "select mid, count(*) from minedblocks where mid = '" + mID + "' group by mid;";
        ResultSet rs = s.executeQuery(sql);

        while(rs.next()){
            length = rs.getInt("count");
        }

        int i = 0;

        while(i < mIDs.size()){
            // go through mIDs list to see who also tried to mine the same transaction as you
            // get the length of their chain and compare competing chair's lengths
            sql = "select mid, count(*) from minedblocks where mid = '" + mIDs.get(i) + "' group by mid;";
            rs = s.executeQuery(sql);

            while(rs.next()){
                competingLength = rs.getInt("count");
            }

            if(length < competingLength){
                con.close();
                return false; // current miner lost - competing chair won
            }

            i++;
        }

        con.close();
        return true; // current miner won - competing chair lost

    }

    // pass in temporary blockchain of winning node to delete transactions from MEMPOOL
    public void deleteFromTransactions(LinkedList<BlockNodes> t) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();
        String sql = "";

        for (BlockNodes b : t) {
            sql = "delete from mempool where trid = '" + b.tID + "';";
            s.executeUpdate(sql);
        }

        s.executeUpdate(sql);
        con.close();
    }

    // append winning node's blockchain to final foodchain
    public void addToFoodChain(LinkedList<BlockNodes> blocks) throws ClassNotFoundException, SQLException {
        Class.forName("org.postgresql.Driver");
        Connection con = DriverManager.getConnection("jdbc:postgresql://34.67.145.192:5432/postgres", "postgres", "xCqm5JFEJH2mrwuj");
        Statement s = con.createStatement();
        String sql = "";

        for (BlockNodes b : blocks) {
            sql = "insert into foodchain values (" + b.blockNum + ", '" + b.currHash + "', '" + b.prevHash + "', "
                    + b.nonce + ", '" + b.timeStamp + "', '" + b.location + "' , '" + b.item + "', '" + mID + "', '"
                    + b.tID + "');";
            s.executeUpdate(sql);
        }
        con.close();
    }

    // if this mID wins and has longest chain, follow through with pushing to blockchain and delete transaction
    public void consensusProtocol(LinkedList<BlockNodes> b, Transactions t) throws ClassNotFoundException, SQLException {
        if(checkChainLength(t)){
            deleteFromTransactions(b);
            addToFoodChain(b);

            System.out.println("Successfully appended to blockchain! Here's your money $$$$$$$");
            return;
        }

        System.out.println("Unfortunately another miner has beat you to the prize... aborting attempt.");

    }





}



