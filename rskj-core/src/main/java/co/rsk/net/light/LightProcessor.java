package co.rsk.net.light;

import co.rsk.net.MessageChannel;
import co.rsk.net.light.messages.TransactionIdMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightProcessor {
    private static final Logger logger = LoggerFactory.getLogger("light");

    public LightProcessor() {
    }

    public void processTransactionIdMessage(MessageChannel sender, TransactionIdMessage message) {
        logger.debug("transactionID Message Recieved");
    }
}
