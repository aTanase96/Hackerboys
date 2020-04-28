import java.security.*;
import java.util.*;

public class Main {
    public static void main(String[] args) throws NoSuchAlgorithmException, SignatureException {

        // Stores all accounts with the format HashMap<accountName,keyPair>
        HashMap<String, KeyPair> keyPairs = new HashMap<String, KeyPair>();

        // Set up initial account for Scrooge.
        keyPairs.put("Scrooge", KeyPairGenerator.getInstance("RSA").generateKeyPair());

        // Set up initial transaction. Scrooge will get 100 coins.
        Transaction initTx = new Transaction();
        initTx.addOutput(100, keyPairs.get("Scrooge").getPublic());
        initTx.finalize();

        // Set up utxoPool
        UTXOPool utxoPool = new UTXOPool();
        UTXO utxo = new UTXO(initTx.getHash(),0);
        utxoPool.addUTXO(utxo, initTx.getOutput(0));

        TxHandler txHandler = new TxHandler(utxoPool);


        /**
         * A terminal program used to interact with ScroogeCoin.
         *
         * Options:
         *
         * 1. Make new account.
         * 2. List accounts and balance.
         * 3. Pay recipients.
         * 4. Exit.
         *
         */
        boolean running = true;
        while (running){
            System.out.println("---------- Welcome to ScroogeCoin! ---------- \n" +
                    "1. Make new account.\n" +
                    "2. List accounts and balance.\n" +
                    "3. Pay recipients.\n" +
                    "4. Exit.\n");

            Scanner input = new Scanner(System.in);
            String option = input.nextLine();

            switch (option){
                case "1":
                    // Make new account.

                    System.out.println("\nEnter account name.");
                    String name = input.nextLine();

                    // Generate key pair for new account:
                    keyPairs.put(name, KeyPairGenerator.getInstance("RSA").generateKeyPair());
                    break;

                case "2":
                    // List accounts and balance

                    // Buffer variables
                    boolean exists;
                    UTXO buffer = null;

                    // Iterate every keypair
                    for (Map.Entry<String, KeyPair> entry : keyPairs.entrySet())
                     {
                         exists = false;

                         // Iterate utxoPool
                         for (UTXO ut : utxoPool.getAllUTXO()){
                             // Check if utxo.output.PK matches keypair.PK.
                             if (Objects.equals(utxoPool.getTxOutput(ut).address, entry.getValue().getPublic())) {
                                 // If match, save buffers and break.
                                 buffer = ut;
                                 exists = true;
                                 break;
                             }
                         }

                         // If the account is linked with an utxo, the account has unspent coins. Else, the account has 0 coins.
                         if (exists){
                             System.out.println("Account name: " + entry.getKey() + " | Balance: " + utxoPool.getTxOutput(buffer).value);
                         } else {
                             System.out.println("Account name: " + entry.getKey() + " | Balance: 0");
                         }
                    }
                    break;

                case "3":
                    // Pay recipient.

                    // Scan seller
                    System.out.println("\nEnter seller account.");
                    String seller = input.nextLine();

                    // Scan number of recipients
                    System.out.println("\nEnter number of recipients.");
                    String nrRecipients = input.nextLine();

                    // Stores recipients with the format HashMap<recipient, amount>
                    HashMap<String, String> recipients = new HashMap<String, String>();

                    // Scan recipients and corresponding amount.
                    for (int i = 0; i < Integer.parseInt(nrRecipients); i++){
                        System.out.println("\nEnter recipient account name.");
                        String acc = input.nextLine();
                        System.out.println("\nEnter amount.");
                        String amount = input.nextLine();
                        recipients.put(acc, amount);
                    }

                    // Create the tx using the scanned info.
                    // Iterate all utxo to find the sellers unspent coins.
                    for (UTXO ut : utxoPool.getAllUTXO()) {
                        // If utxo PK equals seller PK
                        if (Objects.equals(utxoPool.getTxOutput(ut).address, keyPairs.get(seller).getPublic())){

                            // Make new tx and use utxo's hash and index.
                            Transaction tx = new Transaction();
                            tx.addInput(ut.getTxHash(), ut.getIndex());

                            // Iterate through all recipients, and add one output for each.
                            for (Map.Entry<String, String> entry : recipients.entrySet()) {
                                tx.addOutput(Double.parseDouble(entry.getValue()), keyPairs.get(entry.getKey()).getPublic());
                            }

                            // Seller signs the tx, and the utxoPool is updated from txHandler.
                            tx.signTx(keyPairs.get(seller).getPrivate(), 0);
                            txHandler.handleTxs(new Transaction[] { tx });
                            utxoPool = txHandler._utxoPool;
                        }
                    }
                    break;

                case "4":
                    // Exit.

                    running = false;
                    break;

            }
        }
    }
}
