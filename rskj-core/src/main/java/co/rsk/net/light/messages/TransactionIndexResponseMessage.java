/*
 * This file is part of RskJ
 * Copyright (C) 2019 RSK Labs Ltd.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package co.rsk.net.light.messages;

import co.rsk.net.messages.Message;
import co.rsk.net.messages.MessageType;
import co.rsk.net.messages.MessageVisitor;
import co.rsk.net.messages.MessageWithId;

/**
 * Created by Sebastian Sicardi on 22/10/2019.
 */

public class TransactionIndexResponseMessage extends MessageWithId {
    private long id;
    private byte[] blockNumber;
    private byte[] blockHash;
    private byte[] txIndex;

    public TransactionIndexResponseMessage(long id, byte[] blockNumber, byte[] blockHash, byte[] txIndex) {
        this.blockNumber = blockNumber;
        this.blockHash = blockHash;
        this.txIndex = txIndex;
        this.id = id;
    }

    @Override
    public MessageType getMessageType() { return MessageType.TRANSACTION_INDEX_RESPONSE_MESSAGE; }

    @Override
    public byte[] getEncodedMessage() {
        return new byte[0];
    }

    @Override
    public long getId() {
        return this.id;
    }

    public byte[] getBlockNumber() {
        return blockNumber;
    }

    public byte[] getBlockHash() {
        return blockHash;
    }

    public byte[] getTxIndex() {
        return txIndex;
    }

    @Override
    protected byte[] getEncodedMessageWithoutId() {
        return new byte[0];
    }

    @Override
    public void accept(MessageVisitor v) {
        v.apply(this);
    }
}
