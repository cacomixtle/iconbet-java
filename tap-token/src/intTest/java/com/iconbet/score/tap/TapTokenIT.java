package com.iconbet.score.tap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
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
import foundation.icon.test.TransactionFailureException;
import foundation.icon.test.TransactionHandler;
import foundation.icon.test.score.Score;

class TapTokenIT extends TestBase{	
	private static Score tapToken;
	private static TransactionHandler txHandler;
	private static KeyWallet ownerWallet;
	private static final BigInteger decimals = BigInteger.valueOf(10);
	private static final BigInteger initialSupply = BigInteger.valueOf(10);
	private static Env.Chain chain = Env.getDefaultChain();

	@BeforeAll
	static void init() throws Exception {
		IconService iconService = new IconService(new HttpProvider(chain.getEndpointURL(3)));
		txHandler = new TransactionHandler(iconService, chain);

		BigInteger amount = ICX.multiply(BigInteger.valueOf(2000));
		ownerWallet = KeyWallet.create();
		txHandler.transfer(ownerWallet.getAddress(), amount);
		ensureIcxBalance(txHandler, ownerWallet.getAddress(), BigInteger.ZERO, amount);

		// deploy token score
		tapToken = txHandler.deploy(ownerWallet, Score.getFilePath("tap-token"), 
				new RpcObject.Builder()
				.put("_decimals", new RpcValue(decimals))
				.put("_initialSupply", new RpcValue(initialSupply))
				.build()
				);
	}

	@AfterAll
	static void shutdown() throws Exception {
		txHandler.refundAll(ownerWallet);
	}

	@Test
	void updateScore() throws IOException, ResultTimeoutException, TransactionFailureException {

		Address expectedAddress = tapToken.getAddress();
		assertNotNull(expectedAddress);
		BigInteger ownerBalance = txHandler.getBalance(ownerWallet.getAddress());

		System.out.println("balance after deploying: "+ ownerBalance);
		System.out.println("cost of deployment: " + ICX.multiply(BigInteger.valueOf(2000)).subtract(ownerBalance));

		//step limit 
		//      1 849 944 600
		//limit=1 248 380 600
		//		      494 470
		//          8 325 200
		//
		//opimized jar size = 8273
		//uber jar size = 30110
		//2000,000000000000000000 ICX init
		//1984,348576625000000000 ICX after create
		//  15,651423375000000000 ICX cost of create aprox 16 ICX?
		//1961,197153250000000000 ICX after update
		//  38,802846750000000000 ICX cost of update
		RpcItem ts = tapToken.call("totalSupply", new RpcObject.Builder().build());
		System.out.println("total supply:"+ts);
		
		BigInteger steps = BigInteger.valueOf(100_000).add( BigInteger.valueOf(30110 * 200 )).add(BigInteger.valueOf(30110 * 30_000)).add(BigInteger.valueOf(1_600_000_000));
		System.out.println("calculated steps for updating score: "+steps);
		tapToken = txHandler.deploy(ownerWallet, Score.getFilePath("tap-token"),
				tapToken.getAddress(),
				new RpcObject.Builder()
				.put("_decimals", new RpcValue(BigInteger.ZERO))
				.put("_initialSupply", new RpcValue(BigInteger.ZERO))
				.build(),
				steps
				);

		assertEquals(expectedAddress,tapToken.getAddress());
		ownerBalance = txHandler.getBalance(ownerWallet.getAddress());

		System.out.println("balance after updating: "+ ownerBalance);
		System.out.println("cost of update: " + ICX.multiply(BigInteger.valueOf(2000)).subtract(ownerBalance));

		ts = tapToken.call("totalSupply", new RpcObject.Builder().build());
		assertNotNull(ts);
		//assertNotEquals(BigInteger.ZERO, ts.asInteger());
		System.out.println("total supply:"+ts);
	}
}
