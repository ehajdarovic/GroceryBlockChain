import java.security.MessageDigest;

public class BlockNodes {
    public Integer blockNum = 0;
    public String currHash;
    public String mID = "";
    public String prevHash = "";
    public String location = "";
    public String item = "";
    public long timeStamp = 0;
    public int nonce = 0;
    public String tID = "";

    // basic constructor so every new blocknode has a hash value
    public BlockNodes(){
        currHash = calculateHash();
    }

    // generate hash value
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
    // take in specific block data to generate hash value
    public String calculateHash(){
        String dataToHash = prevHash
                + timeStamp
                + nonce
                + item
                + location;

        return sha256(dataToHash);
    }

}
