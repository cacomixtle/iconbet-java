package com.iconbet.score.promotion;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import foundation.icon.test.Env;
import foundation.icon.test.ResultTimeoutException;
import foundation.icon.test.TestBase;
import foundation.icon.test.TransactionHandler;
import foundation.icon.test.score.Score;

class PromotionIT extends TestBase{

	private static Score promotion;
	private static Score rewardDistribution;
	//private static Score tapToken;
	private static TransactionHandler txHandler;
	private static KeyWallet[] wallets;
	// TOOD: once we have dividens score set, we can use it instead of a EOA 
	private static KeyWallet dividendScoreOwner;
	private static final BigInteger decimals = BigInteger.valueOf(10);
	private static final BigInteger initialSupply = BigInteger.valueOf(10);
	private static Env.Chain chain = Env.getDefaultChain();
	private static IconService iconService; 

	@BeforeAll
	static void init() throws Exception {
		iconService = new IconService(new HttpProvider(chain.getEndpointURL(3)));
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
		dividendScoreOwner = wallets[0];

		promotion = txHandler.deploy(chain.godWallet, Score.getFilePath("promotion"), null);
		rewardDistribution = txHandler.deploy(chain.godWallet, Score.getFilePath("reward-distribution"), null);

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
	void testDistributePrizesByPayableFallback() throws IOException, ResultTimeoutException {
		assertNotNull(promotion);

		promotion.invokeAndWaitResult(chain.godWallet, "set_dividends_score", 
				new RpcObject.Builder()
				.put("_score", new RpcValue(dividendScoreOwner.getAddress()))
				.build());

		promotion.invokeAndWaitResult(chain.godWallet, "set_rewards_score", 
				new RpcObject.Builder()
				.put("_score", new RpcValue(rewardDistribution.getAddress()))
				.build());

        Transaction transaction = TransactionBuilder.newBuilder()
                .nid(BigInteger.valueOf(chain.networkId))
                .from(dividendScoreOwner.getAddress())
                .to(promotion.getAddress())
                .value(BigInteger.valueOf(1010))
                .build();

        BigInteger steps = iconService.estimateStep(transaction).execute().add(BigInteger.valueOf(10000));

        SignedTransaction signedTransaction = new SignedTransaction(transaction, dividendScoreOwner, steps);

        //method annotated with @Payable will be call when we transfer icx 
        iconService.sendTransaction(signedTransaction).execute();

	}

}
