package com.iconbet.score.reward;

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
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import foundation.icon.test.Env;
import foundation.icon.test.ResultTimeoutException;
import foundation.icon.test.TestBase;
import foundation.icon.test.TransactionHandler;
import foundation.icon.test.score.Score;

class RewardDistributionIT extends TestBase{

	private static Score reward;
	private static Score tapToken;
	private static TransactionHandler txHandler;
	private static KeyWallet[] wallets;
	private static KeyWallet ownerWallet;
	private static final BigInteger decimals = BigInteger.valueOf(10);
	private static final BigInteger initialSupply = BigInteger.valueOf(10);
	private static Env.Chain chain = Env.getDefaultChain();

	@BeforeAll
	static void init() throws Exception {
		IconService iconService = new IconService(new HttpProvider(chain.getEndpointURL(3)));
		txHandler = new TransactionHandler(iconService, chain);

		// init wallets
		wallets = new KeyWallet[1];
		BigInteger amount = ICX.multiply(BigInteger.valueOf(5));
		for (int i = 0; i < wallets.length; i++) {
			wallets[i] = KeyWallet.create();
			txHandler.transfer(wallets[i].getAddress(), amount);
		}
		for (KeyWallet wallet : wallets) {
			ensureIcxBalance(txHandler, wallet.getAddress(), BigInteger.ZERO, amount);
		}
		ownerWallet = wallets[0];

		// deploy token score
		tapToken = txHandler.deploy(chain.godWallet, Score.getFilePath("tap-token"), new RpcObject.Builder()
                .put("_decimals", new RpcValue(decimals))
                .put("_initialSupply", new RpcValue(initialSupply))
                .build()
                );
		// deploy game auth SCORE
		reward = txHandler.deploy(chain.godWallet, Score.getFilePath("reward-distribution"), null);

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
	void testGetTodaysTapDistribution() throws IOException, ResultTimeoutException {
		assertNotNull(reward);
		assertNotNull(tapToken);

		reward.invokeAndWaitResult(chain.godWallet, "set_token_score",
				new RpcObject.Builder()
				.put("_score", new RpcValue(tapToken.getAddress()))
				.build()
				);

		BigInteger tokens = BigInteger.valueOf(1000);
		tapToken.invokeAndWaitResult(chain.godWallet, "transfer", new RpcObject.Builder()
				.put("_to", new RpcValue(reward.getAddress()))
				.put("_value", new RpcValue(tokens) )
				.put("_data", new RpcValue("init transfer".getBytes()))
				.build());

		RpcItem item = reward.call("get_todays_tap_distribution", new RpcObject.Builder().build());
		BigInteger result = item.asInteger();
		assertNotNull(result);
		assertEquals(tokens, result);

	}

}
