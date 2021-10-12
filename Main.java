import java.util.Scanner;
import java.util.*;
import java.sql.*;

public class Main {

    //MAIN
    public static void main(String[] args) throws SQLException, ClassNotFoundException {

        LinkedList<BlockNodes> blockChain = new LinkedList<BlockNodes>();
        LinkedList<Transactions> transactions = new LinkedList<Transactions>();
        Miner m = new Miner();
        Transactions t = null;
        BlockNodes b = new BlockNodes();

        System.out.println("Menu - enter one of the following: ");
        System.out.println("type '1' if you want to add a transaction ");
        System.out.println("type '2' if you want to mine a block ");
        Scanner input = new Scanner(System.in);
        int choice, i = 0;
        choice = input.nextInt();

        String ans;

        //  *========== TRANSACTION SELECTED ==========*
        if(choice == 1){

            do {
                System.out.print("Create new entry? (y/n): ");
                Scanner input2 = new Scanner(System.in);
                ans = input2.nextLine();

                if (ans.equals("n")) {
                    break;
                }

                while (!ans.equals("y")) {
                    System.out.print("Invalid entry, please try again: ");
                    ans = input.nextLine();

                    if (ans.equals("y")) {
                        continue;
                    }
                }

                System.out.print("Enter item: ");
                Scanner input3 = new Scanner(System.in);
                String item = input3.nextLine();

                System.out.print("Enter location(store name, address): ");
                Scanner input4 = new Scanner(System.in);
                String location = input4.nextLine();

                t = new Transactions(item, location);

                //add to mempool - item, location, and generate fee
                t.addToMempool();

            } while (ans.equals("y"));

            System.out.println("Exiting program...");
        }

        //  *========== MINER SELECTED ==========*
        else if(choice == 2) {
            // get transactions from database
            System.out.println("Grabbing all transactions...");
            transactions = m.getTransactions();

            do {
                System.out.print("Mine a block? (y/n): ");
                Scanner input2 = new Scanner(System.in);
                ans = input2.nextLine();

                if (ans.equals("n")) {
                    break;
                }

                while (!ans.equals("y")) {
                    System.out.print("Invalid entry, please try again: ");
                    ans = input.nextLine();

                    if (ans.equals("y")) {
                        continue;
                    }
                }

                int diff = 0;

                //get highest transaction from database
                System.out.println("Getting highest fee for transaction...");
                t = m.getHighestTransaction(transactions);

                // validate if miner wants to keep going
                System.out.println("Continue? (y/n)");

                ans = input2.nextLine();
                if (ans.equals("n")) {
                    break;
                }

                while (!ans.equals("y")) {
                    System.out.print("Invalid entry, please try again: ");
                    ans = input.nextLine();

                    if (ans.equals("y")) {
                        continue;
                    }
                }

                transactions = m.updateTransactions(t,transactions);

                System.out.print("Enter desired difficulty: ");
                diff = input.nextInt();


                // mining block
                m.startTime = System.currentTimeMillis();
                System.out.println("Mining block " + i + ": ");
                b = m.mineBlock(diff, i, t, blockChain);
                i = b.blockNum;
                System.out.println("Time taken: " + m.elapsedTime());

                blockChain.add(b);

            } while (ans.equals("y"));

            if(blockChain.isEmpty()){
                System.out.println("No chain detected. Exiting program...");
                return;
            }

            System.out.println("*******************************");
            System.out.println("*------------ FINAL BLOCKCHAIN ------------*");

            for (BlockNodes bL : blockChain) {
                System.out.println("Block Number: " + bL.blockNum);
                System.out.println("Current Hash: " + bL.currHash);
                System.out.println("Previous Hash: " + bL.prevHash);
                System.out.println("Nonce: " + bL.nonce);
                System.out.println("Timestamp: " + bL.timeStamp);
                System.out.println("Item: " + bL.item);
                System.out.println("Location: " + bL.location);

            }

            m.consensusProtocol(blockChain, t);
        }

        else{
            System.out.print("Invalid entry. Please enter 1 or 2.");

        }

    }
}
