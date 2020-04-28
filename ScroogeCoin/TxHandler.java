import java.util.ArrayList;

public class TxHandler {

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public UTXOPool _utxoPool;

    public TxHandler(UTXOPool utxoPool) {
        _utxoPool = new UTXOPool(utxoPool);
    }

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
        ArrayList<UTXO> usedUTXO = new ArrayList<>();

        // Iterate all inputs in tx
        for (int i = 0; i < tx.numInputs(); i++){

            // Make a copy of the current input in tx
            Transaction.Input input = tx.getInput(i);

            // make a new UTXO using the previous hash and the current output index.
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);

            // Check rule 1: All outputs claimed by {@code tx} are in the current UTXO pool

            // Check if the utxo pool copy contains the current utxo.
            if (!_utxoPool.contains(utxo)) return false;

            // Check rule 2: The signatures on each input of {@code tx} are valid

            // Make a copy of the current output in tx?
            Transaction.Output output = _utxoPool.getTxOutput(utxo);

            // Get raw data to be used in verifySignature
            byte[] message = tx.getRawDataToSign(i);
            if (!Crypto.verifySignature(output.address, message, input.signature)) return false;

            // Check rule 3: No UTXO is claimed multiple times by {@code tx}
            if (usedUTXO.contains(utxo)) return false;
            usedUTXO.add(utxo);
            sumInput += output.value;
        }

        // Check rule 4: All of {@code tx}s output values are non-negative

        // Iterate all outputs in tx
        for (int i = 0; i < tx.numOutputs(); i++){
            // Make a copy of the current output in tx
            Transaction.Output output = tx.getOutput(i);
            if (output.value < 0) return false;
            sumOutput += output.value;
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

        // Iterate through each tx
        for (Transaction t : possibleTxs){
            if (isValidTx(t)) {
                validTxs.add(t);

                // remove utxo's
                for (Transaction.Input input : t.getInputs()){
                    UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
                    _utxoPool.removeUTXO(utxo);
                }

                // add new utxo
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
