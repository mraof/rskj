package co.rsk.net.light;

import co.rsk.net.MessageChannel;
import co.rsk.net.light.messages.TransactionIdResponseMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LightProcessor {
    private static final Logger logger = LoggerFactory.getLogger("light");

    public LightProcessor() {
    }

    public void processTransactionIdResponseMessage(MessageChannel sender, TransactionIdResponseMessage message) {
        logger.debug("transactionID response Message Recieved");
    }
}
