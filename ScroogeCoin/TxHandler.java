import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public UTXOPool _utxoPool; // Creating a variable called utxoPool that will be used as a copy for the methods bellow.

    public TxHandler(UTXOPool utxoPool) {
        _utxoPool = new UTXOPool(utxoPool);
    }  // creating the copy of the variable utxoPool

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool, 
     * (2) the signatures on each input of {@code tx} are valid, 
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     *     values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        double sumInput = 0;
        double sumOutput = 0;
        ArrayList<UTXO> usedUTXO = new ArrayList<>(); // Creates an arraylist that stores all the usedUTXO

        // Creates a for loop that will iterate through the different inputs in the transactions
        for (int i = 0; i < tx.numInputs(); i++){

            // Making a copy of the current transaction input.
            Transaction.Input input = tx.getInput(i);

            // Creating a new UTXO that uses the previous transaction hash H() and the current output index.
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // Check rule 1: All outputs claimed by {@code tx} are in the current UTXO pool

            // If statement that check the copy of utxoPool for already containing a utxo. if yes then return false.
            if (!_utxoPool.contains(utxo)) return false;

            // Check rule 2: The signatures on each input of {@code tx} are valid

            // Make a copy of the current output in tx?
            Transaction.Output output = _utxoPool.getTxOutput(utxo);

            // Get raw data to be used in verifySignature
            byte[] message = tx.getRawDataToSign(i); // Creates an byte array that stores the raw message data.
            if (!Crypto.verifySignature(output.address, message, input.signature)) return false; // If statement that checks the validity of the signature based on previous transactions

            // Check rule 3: No UTXO is claimed multiple times by {@code tx}
            if (usedUTXO.contains(utxo)) return false; // If statement that checks for rule nr 3. This prevents double spending
            usedUTXO.add(utxo); // If utxo is not previously used it is added to the usedUTXO ArrayList
            sumInput += output.value;
        }

        // Check rule 4: All of {@code tx}s output values are non-negative

        // Create a new for loop that iterates through all the outputs in a transaction
        for (int i = 0; i < tx.numOutputs(); i++){
            // Creates a copy of the current output handled in the transaction
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) return false; // If statement that checks if the output value of coin is below 0, is yes than something is wrong
            sumOutput += output.value; //Add the remaining value from the output to the sumOutput variable.
        }

        // Check rule 5: The sum of {@code tx}s input values is greater than or equal to the sum of its output values; and false otherwise.
        if (sumInput < sumOutput) return false;

        return true;
    }

    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // A buffer for accepted Txs, contains only valid Txs.
        ArrayList<Transaction> validTxs = new ArrayList<>();

        // // Create a for loop that will iterate through all transactions in the list.
        for (Transaction t : possibleTxs){
            if (isValidTx(t)) {
                validTxs.add(t);

                // If statement that checks if the possible transaction t is valid, if not then removes the transaction from the array list created above
                for (Transaction.Input input : t.getInputs()){
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    _utxoPool.removeUTXO(utxo);
                }

                // Create a new byte hashmap that will store the hash of the current possible transaction.
                byte[] hash = t.getHash();
                for (int i = 0; i < t.numOutputs(); i++){
                    UTXO utxo = new UTXO(hash, i);
                    _utxoPool.addUTXO(utxo, t.getOutput(i));
                }
            }
        }

        // Parse ArrayList to Transaction[], in order to return proper type
        Transaction[] validTxsArr = new Transaction[validTxs.size()];
        validTxsArr = validTxs.toArray(validTxsArr);
        return validTxsArr;
    }

}
