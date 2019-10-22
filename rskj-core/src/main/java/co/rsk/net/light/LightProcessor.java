package co.rsk.net.light;

import co.rsk.net.MessageChannel;
import co.rsk.net.light.messages.TransactionIndexRequestMessage;
import co.rsk.net.light.messages.TransactionIndexResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightProcessor {
    private static final Logger logger = LoggerFactory.getLogger("light");

    public LightProcessor() {
    }

    public void processTransactionIndexResponseMessage(MessageChannel sender, TransactionIndexResponseMessage message) {
        logger.debug("transactionID response Message Recieved");
    }

    public void processTransactionIndexRequestMessage(MessageChannel sender, TransactionIndexRequestMessage message) {
        logger.debug("transactionID request Message Recieved");
    }
}
