import java.security.MessageDigest;
import java.util.Scanner;
import java.util.Date;
import java.util.Calendar;
import java.util.*;

public class BlockNodes {
    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

    public Integer blockNum = 0;
    public String currHash;
    public String prevHash;
    public String data;
    private long timeStamp;
    private int nonce = 0;
    public long startTime;

    public BlockNodes(String data, Integer blockNum, long timeStamp){//removed String prevHash
        this.data = data;
        //this.prevHash = prevHash;
        this.blockNum = blockNum;
        this.timeStamp = timeStamp;
        this.currHash = calculateHash();
    }

    public String calculateHash(){
        String dataToHash = prevHash
                + Long.toString(timeStamp)
                + Integer.toString(nonce)
                + data;

        return sha256(dataToHash);
    }

    public void setPrevHash(BlockNodes b){
        prevHash = b.currHash;
    }

    public void setFirstHash(){
        prevHash = "0";
    }

    public void mineBlock(int difficulty) {
        String target = new String(new char[difficulty]).replace('\0', '0'); //Create a string with difficulty * "0"
        while(!currHash.substring( 0, difficulty).equals(target)) {
            nonce++;
            currHash = calculateHash();
        }
        System.out.println("Mine Successful. Hash is: " + currHash);
    }

        public double elapsedTime() {
            long now = System.currentTimeMillis();
            return (now - startTime) / 1000.0;
        }

    //MAIN
    public static void main(String[] args){
        LinkedList<BlockNodes> blockChain = new LinkedList<BlockNodes>();
        int i = 0, diff;

        Calendar c1 = Calendar.getInstance();
        Date d1 = c1.getTime();

        Scanner input = new Scanner(System.in);
        Scanner input2 = new Scanner(System.in);
        String ans, data;
        BlockNodes block = null;

        do{
            System.out.print("Create new entry? (y/n): ");
            ans = input.nextLine();

            if(ans.equals("n")){
                break;
            }

            while(!ans.equals("y")){
                System.out.print("Invalid entry, please try again: ");
                ans = input.nextLine();

                if(ans.equals("y")){
                    continue;
                }
            }

            System.out.print("Enter data: ");
            data = input.nextLine();

            block = new BlockNodes(data, i,d1.getTime());

            if (blockChain.size() == 0)
                block.setFirstHash();

            else {
                int prevIndex = blockChain.size() - 1;
                block.setPrevHash(blockChain.get(prevIndex));
            }

            blockChain.add(block);

            System.out.print("Enter desired difficulty: ");
            diff = input2.nextInt();


            // mining block
            block.startTime = System.currentTimeMillis();
            System.out.println("Mining block " + i + ": ");
            block.mineBlock(diff);
            System.out.println("Time taken: " + block.elapsedTime());

            i++;

        } while(ans.equals("y"));

        for(BlockNodes b : blockChain) {
            System.out.println("Block Number: " + b.blockNum);
            System.out.println("Current Hash: " + b.currHash);
            System.out.println("Previous Hash: " + b.prevHash);
            System.out.println("Nonce: " + b.nonce);
            System.out.println("Timestamp: " + b.timeStamp);
            System.out.println("Data: " + b.data);
        }
    }
}
