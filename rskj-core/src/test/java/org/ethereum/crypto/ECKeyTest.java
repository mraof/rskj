/*
 * This file is part of RskJ
 * Copyright (C) 2017 RSK Labs Ltd.
 * (derived from ethereumJ library, Copyright (c) 2016 <ether.camp>)
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

package org.ethereum.crypto;

import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import org.ethereum.core.ImmutableTransaction;
import org.ethereum.core.Transaction;
import org.ethereum.crypto.ECKey.ECDSASignature;
import org.ethereum.util.ByteUtil;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.bouncycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SignatureException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

import static com.google.common.base.Preconditions.checkNotNull;
import static org.junit.Assert.*;

public class ECKeyTest {
    private static final Logger log = LoggerFactory.getLogger(ECKeyTest.class);

    private String privString = "3ecb44df2159c26e0f995712d4f39b6f6e499b40749b1cf1246c37f9516cb6a4";
    private BigInteger privateKey = new BigInteger(Hex.decode(privString));

    private String pubString = "0497466f2b32bc3bb76d4741ae51cd1d8578b48d3f1e68da206d47321aec267ce78549b514e4453d74ef11b0cd5e4e4c364effddac8b51bcfc8de80682f952896f";
    private String compressedPubString = "0397466f2b32bc3bb76d4741ae51cd1d8578b48d3f1e68da206d47321aec267ce7";
    private byte[] pubKey = Hex.decode(pubString);
    private byte[] compressedPubKey = Hex.decode(compressedPubString);
    private String address = "8a40bfaa73256b60764c1bf40675a99083efb075";

    private String exampleMessage = "This is an example of a signed message.";

    // Signature components
    private final BigInteger r = new BigInteger("28157690258821599598544026901946453245423343069728565040002908283498585537001");
    private final BigInteger s = new BigInteger("30212485197630673222315826773656074299979444367665131281281249560925428307087");
    byte v = 28;

    @Test
    public void testHashCode() {
        Assert.assertEquals(1866897155, ECKey.fromPrivate(privateKey).hashCode());
    }

    @Test
    public void testECKey() {
        ECKey key = new ECKey();
        assertTrue(key.isPubKeyCanonical());
        assertNotNull(key.getPubKey());
        assertNotNull(key.getPrivKeyBytes());
        log.debug(Hex.toHexString(key.getPrivKeyBytes()) + " :Generated privkey");
        log.debug(Hex.toHexString(key.getPubKey()) + " :Generated pubkey");
    }

    @Test
    public void testFromPrivateKey() {
        ECKey key = ECKey.fromPrivate(privateKey).decompress();
        assertTrue(key.isPubKeyCanonical());
        assertTrue(key.hasPrivKey());
        assertArrayEquals(pubKey, key.getPubKey());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrivatePublicKeyBytesNoArg() {
        new ECKey(null, null);
        fail("Expecting an IllegalArgumentException for using only null-parameters");
    }

    @Test
    public void testIsPubKeyOnly() {
        ECKey key = ECKey.fromPublicOnly(pubKey);
        assertTrue(key.isPubKeyCanonical());
        assertTrue(key.isPubKeyOnly());
        assertArrayEquals(key.getPubKey(), pubKey);
    }


    @Test
    public void testPublicKeyFromPrivate() {
        byte[] pubFromPriv = ECKey.publicKeyFromPrivate(privateKey, false);
        assertArrayEquals(pubKey, pubFromPriv);
    }

    @Test
    public void testPublicKeyFromPrivateCompressed() {
        byte[] pubFromPriv = ECKey.publicKeyFromPrivate(privateKey, true);
        assertArrayEquals(compressedPubKey, pubFromPriv);
    }

    @Test
    public void testGetAddress() {
        ECKey key = ECKey.fromPublicOnly(pubKey);
        assertArrayEquals(Hex.decode(address), key.getAddress());
    }

    @Test
    public void testToString() {
        ECKey key = ECKey.fromPrivate(BigInteger.TEN); // An example private key.
        assertEquals("pub:04a0434d9e47f3c86235477c7b1ae6ae5d3442d49b1943c2b752a68e2a47e247c7893aba425419bc27a3b6c7e693a24c696f794c2ed877a1593cbee53b037368d7", key.toString());
    }

    @Test
    public void testEthereumSign() throws IOException {
        // TODO: Understand why key must be decompressed for this to work
        ECKey key = ECKey.fromPrivate(privateKey).decompress();
        System.out.println("Secret\t: " + Hex.toHexString(key.getPrivKeyBytes()));
        System.out.println("Pubkey\t: " + Hex.toHexString(key.getPubKey()));
        System.out.println("Data\t: " + exampleMessage);
        byte[] messageHash = HashUtil.keccak256(exampleMessage.getBytes());
        ECDSASignature signature = key.sign(messageHash);

        assertEquals(r, signature.r);
        assertEquals(s, signature.s);
        assertEquals(v, signature.v);
    }

    @Test
    public void testVerifySignature1() {
        ECKey key = ECKey.fromPublicOnly(pubKey);
        BigInteger r = new BigInteger("28157690258821599598544026901946453245423343069728565040002908283498585537001");
        BigInteger s = new BigInteger("30212485197630673222315826773656074299979444367665131281281249560925428307087");
        ECDSASignature sig = ECDSASignature.fromComponents(r.toByteArray(), s.toByteArray(), (byte) 28);
        key.verify(HashUtil.keccak256(exampleMessage.getBytes()), sig);
    }

    @Test
    public void testVerifySignature2() {
        BigInteger r = new BigInteger("c52c114d4f5a3ba904a9b3036e5e118fe0dbb987fe3955da20f2cd8f6c21ab9c", 16);
        BigInteger s = new BigInteger("6ba4c2874299a55ad947dbc98a25ee895aabf6b625c26c435e84bfd70edf2f69", 16);
        ECDSASignature sig = ECDSASignature.fromComponents(r.toByteArray(), s.toByteArray(), (byte) 0x1b);
        byte[] rawtx = Hex.decode("f82804881bc16d674ec8000094cd2a3d9f938e13cd947ec05abc7fe734df8dd8268609184e72a0006480");
        try {
            ECKey key = ECKey.signatureToKey(HashUtil.keccak256(rawtx), sig);
            System.out.println("Signature public key\t: " + Hex.toHexString(key.getPubKey()));
            System.out.println("Sender is\t\t: " + Hex.toHexString(key.getAddress()));
            assertEquals("cd2a3d9f938e13cd947ec05abc7fe734df8dd826", Hex.toHexString(key.getAddress()));
            key.verify(HashUtil.keccak256(rawtx), sig);
        } catch (SignatureException e) {
            fail();
        }
    }

    @Test
    @Ignore("The TX sender is not 20-byte long")
    public void testVerifySignature3() throws SignatureException {

        byte[] rawtx = Hex.decode("f86e80893635c9adc5dea000008609184e72a00082109f9479b08ad8787060333663d19704909ee7b1903e58801ba0899b92d0c76cbf18df24394996beef19c050baa9823b4a9828cd9b260c97112ea0c9e62eb4cf0a9d95ca35c8830afac567619d6b3ebee841a3c8be61d35acd8049");

        Transaction tx = new ImmutableTransaction(rawtx);
        ECKey key = ECKey.signatureToKey(HashUtil.keccak256(rawtx), tx.getSingleSignature());

        System.out.println("Signature public key\t: " + Hex.toHexString(key.getPubKey()));
        System.out.println("Sender is\t\t: " + Hex.toHexString(key.getAddress()));

        //  sender: CD2A3D9F938E13CD947EC05ABC7FE734DF8DD826
        // todo: add test assertion when the sign/verify part actually works.
    }


    @Test
    public void testSValue() throws Exception {
        // Check that we never generate an S value that is larger than half the curve order. This avoids a malleability
        // issue that can allow someone to change a transaction [hash] without invalidating the signature.
        final int ITERATIONS = 10;
        ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newFixedThreadPool(ITERATIONS));
        List<ListenableFuture<ECKey.ECDSASignature>> sigFutures = Lists.newArrayList();
        final ECKey key = new ECKey();
        for (byte i = 0; i < ITERATIONS; i++) {
            final byte[] hash = HashUtil.keccak256(new byte[]{i});
            sigFutures.add(executor.submit(new Callable<ECDSASignature>() {
                @Override
                public ECKey.ECDSASignature call() throws Exception {
                    return key.doSign(hash);
                }
            }));
        }
        List<ECKey.ECDSASignature> sigs = Futures.allAsList(sigFutures).get();
        for (ECKey.ECDSASignature signature : sigs) {
            assertTrue(signature.s.compareTo(ECKey.HALF_CURVE_ORDER) <= 0);
        }
        final ECKey.ECDSASignature duplicate = new ECKey.ECDSASignature(sigs.get(0).r, sigs.get(0).s);
        assertEquals(sigs.get(0), duplicate);
        assertEquals(sigs.get(0).hashCode(), duplicate.hashCode());
    }

    @Test
    public void testSignVerify() {
        ECKey key = ECKey.fromPrivate(privateKey);
        String message = "This is an example of a signed message.";
        ECDSASignature output = key.doSign(message.getBytes());
        assertTrue(key.verify(message.getBytes(), output));
    }

    @Test
    public void testIsPubKeyCanonicalCorect() {
        // Test correct prefix 4, right length 65
        byte[] canonicalPubkey1 = new byte[65];
        canonicalPubkey1[0] = 0x04;
        assertTrue(ECKey.isPubKeyCanonical(canonicalPubkey1));
        // Test correct prefix 2, right length 33
        byte[] canonicalPubkey2 = new byte[33];
        canonicalPubkey2[0] = 0x02;
        assertTrue(ECKey.isPubKeyCanonical(canonicalPubkey2));
        // Test correct prefix 3, right length 33
        byte[] canonicalPubkey3 = new byte[33];
        canonicalPubkey3[0] = 0x03;
        assertTrue(ECKey.isPubKeyCanonical(canonicalPubkey3));
    }

    @Test
    public void testIsPubKeyCanonicalWrongLength() {
        // Test correct prefix 4, but wrong length !65
        byte[] nonCanonicalPubkey1 = new byte[64];
        nonCanonicalPubkey1[0] = 0x04;
        assertFalse(ECKey.isPubKeyCanonical(nonCanonicalPubkey1));
        // Test correct prefix 2, but wrong length !33
        byte[] nonCanonicalPubkey2 = new byte[32];
        nonCanonicalPubkey2[0] = 0x02;
        assertFalse(ECKey.isPubKeyCanonical(nonCanonicalPubkey2));
        // Test correct prefix 3, but wrong length !33
        byte[] nonCanonicalPubkey3 = new byte[32];
        nonCanonicalPubkey3[0] = 0x03;
        assertFalse(ECKey.isPubKeyCanonical(nonCanonicalPubkey3));
    }

    @Test
    public void testIsPubKeyCanonicalWrongPrefix() {
        // Test wrong prefix 4, right length 65
        byte[] nonCanonicalPubkey4 = new byte[65];
        assertFalse(ECKey.isPubKeyCanonical(nonCanonicalPubkey4));
        // Test wrong prefix 2, right length 33
        byte[] nonCanonicalPubkey5 = new byte[33];
        assertFalse(ECKey.isPubKeyCanonical(nonCanonicalPubkey5));
        // Test wrong prefix 3, right length 33
        byte[] nonCanonicalPubkey6 = new byte[33];
        assertFalse(ECKey.isPubKeyCanonical(nonCanonicalPubkey6));
    }

    @Test
    public void keyRecovery() throws Exception {
        ECKey key = new ECKey();
        String message = "Hello World!";
        byte[] hash = HashUtil.sha256(message.getBytes());
        ECKey.ECDSASignature sig = key.doSign(hash);
        key = ECKey.fromPublicOnly(key.getPubKeyPoint());
        boolean found = false;
        for (int i = 0; i < 4; i++) {
            ECKey key2 = ECKey.recoverFromSignature(i, sig, hash, true);
            checkNotNull(key2);
            if (key.equals(key2)) {
                found = true;
                break;
            }
        }
        assertTrue(found);
    }

    @Test
    public void testSignedMessageToKey() throws SignatureException {
        byte[] messageHash = HashUtil.keccak256(exampleMessage.getBytes());

        ECKey key = ECKey.signatureToKey(messageHash, ECDSASignature.fromComponents(ByteUtil.bigIntegerToBytes(r, 32),
                ByteUtil.bigIntegerToBytes(s, 32), v));

        assertNotNull(key);
        assertArrayEquals(pubKey, key.getPubKey());
    }

    @Test
    public void testGetPrivKeyBytes() {
        ECKey key = new ECKey();
        assertNotNull(key.getPrivKeyBytes());
        assertEquals(32, key.getPrivKeyBytes().length);
    }

    @Test
    public void testEqualsObject() {
        ECKey key0 = new ECKey();
        ECKey key1 = ECKey.fromPrivate(privateKey);
        ECKey key2 = ECKey.fromPrivate(privateKey);

        assertFalse(key0.equals(key1));
        assertTrue(key1.equals(key1));
        assertTrue(key1.equals(key2));
    }


    @Test
    public void decryptAECSIC(){
        ECKey key = ECKey.fromPrivate(Hex.decode("abb51256c1324a1350598653f46aa3ad693ac3cf5d05f36eba3f495a1f51590f"));
        byte[] payload = key.decryptAES(Hex.decode("84a727bc81fa4b13947dc9728b88fd08"));
        System.out.println(Hex.toHexString(payload));
    }
}
