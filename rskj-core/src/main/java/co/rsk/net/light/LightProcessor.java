package co.rsk.net.light;

import co.rsk.net.MessageChannel;
import co.rsk.net.light.messages.TransactionIndexRequestMessage;
import co.rsk.net.light.messages.TransactionIndexResponseMessage;
import org.bouncycastle.util.encoders.Hex;
import org.ethereum.core.Block;
import org.ethereum.core.Blockchain;
import org.ethereum.core.Transaction;
import org.ethereum.db.ReceiptStore;
import org.ethereum.db.TransactionInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.math.BigInteger;
import java.util.Arrays;

public class LightProcessor {
    private static final Logger logger = LoggerFactory.getLogger("light");

    private Blockchain blockchain;

    public LightProcessor(Blockchain blockchain) {
        this.blockchain = blockchain;
    }

    public void processTransactionIndexRequestMessage(MessageChannel sender, TransactionIndexRequestMessage message) {
        logger.debug("transactionID request Message Recieved");
        byte[] hash = message.getHash();

        byte[] blockHash;
        byte[] blockNumber;
        byte[] txIndex;

        TransactionInfo txinfo = blockchain.getTransactionInfo(hash);

        if (txinfo == null) {
            // Don't waste time sending an empty response.
            return;
        }

        blockHash = txinfo.getBlockHash();
        blockNumber = BigInteger.valueOf(blockchain.getBlockByHash(blockHash).getNumber()).toByteArray();
        txIndex = BigInteger.valueOf(txinfo.getIndex()).toByteArray();

        TransactionIndexResponseMessage response = new TransactionIndexResponseMessage(message.getId(),blockNumber,blockHash,txIndex);
        sender.sendMessage(response);
    }

    public void processTransactionIndexResponseMessage(MessageChannel sender, TransactionIndexResponseMessage message) {
        logger.debug("transactionIndex response Message Recieved");
        logger.debug("ID: "+message.getId());
        logger.debug("BlockHash: "+ Hex.toHexString(message.getBlockHash()));
        logger.debug("Blocknumber: "+Hex.toHexString(message.getBlockNumber()));
        logger.debug("TxIndex: "+Hex.toHexString(message.getTxIndex()));
        throw new NotImplementedException();
    }
}
