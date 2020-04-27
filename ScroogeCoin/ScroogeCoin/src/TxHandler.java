import javax.swing.*;
import javax.xml.crypto.dsig.TransformService;
import java.security.PublicKey;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TxHandler {
    private UTXOPool utxoPool;

    /**
     * Creates a public ledger whose current UTXOPool (collection of unspent transaction outputs) is
     * {@code utxoPool}. This should make a copy of utxoPool by using the UTXOPool(UTXOPool uPool)
     * constructor.
     */
    public TxHandler(UTXOPool utxoPool) {
        // IMPLEMENT THIS
        this.utxoPool = new UTXOPool(utxoPool); // Creates a new pool of unspent transaction outputs. This is used in the following methods.
    }

    /**
     * @return true if:
     * (1) all outputs claimed by {@code tx} are in the current UTXO pool,
     * (2) the signatures on each input of {@code tx} are valid,
     * (3) no UTXO is claimed multiple times by {@code tx},
     * (4) all of {@code tx}s output values are non-negative, and
     * (5) the sum of {@code tx}s input values is greater than or equal to the sum of its output
     * values; and false otherwise.
     */
    public boolean isValidTx(Transaction tx) {
        // IMPLEMENT THIS
        Set<UTXO> claimedUTXO = new HashSet<UTXO>(); // Creates a new hashset that classifies unspent transaction outputs. this is used to check if a claimed unspent coin has been used before or not.
        double inputSum = 0; // inputSum and outputSum are variables used in a transaction
        double outputSum = 0;

        List<Transaction.Input> inputs = tx.getInputs(); // This creates a list using transaction input type
        for (int i = 0; i < inputs.size(); i++) {
            Transaction.Input input = inputs.get(i);

            if (!isConsumedCoinAvailable(input)) {
                return false;
            }

            if (!verifySignatureOfConsumeCoin(tx, i, input)) {
                return false;
            }

            if (isCoinConsumedMultipleTimes(claimedUTXO, input)) {
                return false;
            }

            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            Transaction.Output correspondingOutput = utxoPool.getTxOutput(utxo);
            inputSum += correspondingOutput.value;

        }

        List<Transaction.Output> outputs = tx.getOutputs();
        for (int i = 0; i < outputs.size(); i++) {
            Transaction.Output output = outputs.get(i);
            if (output.value <= 0) {
                return false;
            }

            outputSum += output.value;
        }

        return !(outputSum > inputSum); // If the outputsum is higher than the inputsum it will return true but this is supposed to return false so the return statement returns using ! operator.
    }

    private boolean isCoinConsumedMultipleTimes(Set<UTXO> claimedUTXO, Transaction.Input input) {
        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        return !claimedUTXO.add(utxo); // This method checks if a claimed unspent transaction input is valid
    }

    private boolean verifySignatureOfConsumeCoin(Transaction tx, int index, Transaction.Input input) {
        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex); // This method checks if the signature of the coin in the transaction is valid by using the verifySignature method in the crypto java class.
        Transaction.Output correspondingOutput = utxoPool.getTxOutput(utxo);
        PublicKey pk = correspondingOutput.address;
        return Crypto.verifySignature(pk, tx.getRawDataToSign(index), input.signature);
    }

    private boolean isConsumedCoinAvailable(Transaction.Input input) {
        UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
        return utxoPool.contains(utxo);
    }



    /**
     * Handles each epoch by receiving an unordered array of proposed transactions, checking each
     * transaction for correctness, returning a mutually valid array of accepted transactions, and
     * updating the current UTXO pool as appropriate.
     */
    public Transaction[] handleTxs(Transaction[] possibleTxs) {
        // IMPLEMENT THIS
        List<Transaction> acceptedTx = new ArrayList<Transaction>();
        for (int i = 0; i < possibleTxs.length; i++){
            Transaction tx = possibleTxs[i];
            if (isValidTx(tx)){
                acceptedTx.add(tx);

                removeConsumedCoinsFromPool(tx);
                addCreatedCoinsToPool(tx);
            }
        }

        Transaction[] result = new Transaction[acceptedTx.size()];
        acceptedTx.toArray(result);
        return result;
    }

    private void addCreatedCoinsToPool(Transaction tx){
        List<Transaction.Output> outputs = tx.getOutputs();
        for (int j = 0; j < outputs.size(); j++) {
            Transaction.Output output = outputs.get(j);
            UTXO utxo = new UTXO(tx.getHash(), j);
            utxoPool.addUTXO(utxo, output);
        }

    }

    private void removeConsumedCoinsFromPool(Transaction tx){
        List<Transaction.Input> inputs = tx.getInputs();
        for (int j = 0; j < inputs.size(); j++){
            Transaction.Input input = inputs.get(j);
            UTXO utxo = new UTXO(input.prevTxHash, input.outputIndex);
            utxoPool.removeUTXO(utxo);
        }
    }
}