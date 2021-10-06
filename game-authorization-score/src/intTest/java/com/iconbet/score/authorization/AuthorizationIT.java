package com.iconbet.score.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.data.Address;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import foundation.icon.test.Env;
import foundation.icon.test.ResultTimeoutException;
import foundation.icon.test.TestBase;
import foundation.icon.test.TransactionHandler;
import foundation.icon.test.score.Score;

class AuthorizationIT extends TestBase {

	private static Score authorization;
	
    private static TransactionHandler txHandler;
    private static KeyWallet[] wallets;
    private static KeyWallet ownerWallet;

    @BeforeAll
    static void init() throws Exception {
        Env.Chain chain = Env.getDefaultChain();
        IconService iconService = new IconService(new HttpProvider(chain.getEndpointURL(3)));
        txHandler = new TransactionHandler(iconService, chain);

        // init wallets
        wallets = new KeyWallet[1];
        BigInteger amount = ICX.multiply(BigInteger.valueOf(30));
        for (int i = 0; i < wallets.length; i++) {
            wallets[i] = KeyWallet.create();
            txHandler.transfer(wallets[i].getAddress(), amount);
        }
        for (KeyWallet wallet : wallets) {
            ensureIcxBalance(txHandler, wallet.getAddress(), BigInteger.ZERO, amount);
        }
        ownerWallet = wallets[0];

        // deploy game auth SCORE
        authorization = txHandler.deploy(ownerWallet, Score.getFilePath("game-authorization-score"), null);

    }

    @AfterAll
    static void shutdown() throws Exception {
        for (KeyWallet wallet : wallets) {
            txHandler.refundAll(wallet);
        }
    }

	@BeforeEach
	public void setup() throws Exception {
	}

	@Test
	void testGetRouletteScore() throws IOException, ResultTimeoutException {
		assertNotNull(authorization);
		Address rouletteScore = new Address("cxd1a7d8e201c9a45881d9a946858f126d9b82a6f3");
		authorization.invokeAndWaitResult(ownerWallet, "set_roulette_score", 
				new RpcObject.Builder()
				.put("_scoreAddress", new RpcValue(rouletteScore))
				.build()
				);
		RpcItem item = authorization.call("get_roulette_score", new RpcObject.Builder().build());
		Address a = item.asAddress();
		assertNotNull(a);
		assertEquals(rouletteScore.toString(), a.toString());
	}

}
