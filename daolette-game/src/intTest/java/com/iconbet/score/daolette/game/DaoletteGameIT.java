package com.iconbet.score.daolette.game;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Optional;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;

import foundation.icon.icx.IconService;
import foundation.icon.icx.KeyWallet;
import foundation.icon.icx.SignedTransaction;
import foundation.icon.icx.Transaction;
import foundation.icon.icx.TransactionBuilder;
import foundation.icon.icx.data.TransactionResult;
import foundation.icon.icx.data.TransactionResult.EventLog;
import foundation.icon.icx.transport.http.HttpProvider;
import foundation.icon.icx.transport.jsonrpc.RpcItem;
import foundation.icon.icx.transport.jsonrpc.RpcObject;
import foundation.icon.icx.transport.jsonrpc.RpcValue;
import foundation.icon.test.Env;
import foundation.icon.test.ResultTimeoutException;
import foundation.icon.test.TestBase;
import foundation.icon.test.TransactionHandler;
import foundation.icon.test.score.Score;

class DaoletteGameIT extends TestBase{
	private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");

	private static Score daoletteGame;
	private static Score daolette;
	private static Score authorization;
	private static Score rewardDistribution;
	private static Score tapToken;
	private static TransactionHandler txHandler;
	private static KeyWallet[] wallets;
	// TOOD: once we have dividens score set, we can use it instead of a EOA 
	private static KeyWallet ownerWallet;
	private static final BigInteger decimals = BigInteger.valueOf(10);
	private static final BigInteger initialSupply = BigInteger.valueOf(10);
	private static Env.Chain chain = Env.getDefaultChain();
	private static IconService iconService; 
	private ObjectMapper mapper = new ObjectMapper();

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
		ownerWallet = wallets[0];

		daoletteGame = txHandler.deploy(chain.godWallet, Score.getFilePath("daolette-game"), null);
		daolette = txHandler.deploy(chain.godWallet, Score.getFilePath("daolette"), null);
        authorization = txHandler.deploy(chain.godWallet, Score.getFilePath("game-authorization-score"), null);
		rewardDistribution = txHandler.deploy(chain.godWallet, Score.getFilePath("reward-distribution"), null);

		tapToken = txHandler.deploy(chain.godWallet, Score.getFilePath("tap-token"), new RpcObject.Builder()
                .put("_decimals", new RpcValue(decimals))
                .put("_initialSupply", new RpcValue(initialSupply))
                .build()
                );

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
	void testBetOnEvenOdd() throws IOException, ResultTimeoutException {
		assertNotNull(daoletteGame);

		//------------- starting games
		daoletteGame.invokeAndWaitResult(chain.godWallet, "set_treasury_score", 
				new RpcObject.Builder()
				.put("_score", new RpcValue(daolette.getAddress()))
				.build());

		daoletteGame.invokeAndWaitResult(chain.godWallet, "game_on", new RpcObject.Builder().build());


		daolette.invokeAndWaitResult(chain.godWallet, "set_game_auth_score", 
				new RpcObject.Builder()
				.put("_score", new RpcValue(authorization.getAddress()))
				.build());

		authorization.invokeAndWaitResult(chain.godWallet, "set_super_admin", 
				new RpcObject.Builder()
				.put("_super_admin", new RpcValue(chain.godWallet.getAddress()))
				.build());

		//---------------  activating games
		//activate daolette game
		GameMetadata gmd = new GameMetadata();
		gmd.setMaxPayout("5900000000000000000");
		gmd.setName("test-daolette-game");
		gmd.setScoreAddress(daoletteGame.getAddress().toString());
		gmd.setMinBet("200000000000000000");
		gmd.setGameType("Per wager settlement");
		gmd.setRevShareWalletAddress(daolette.getAddress().toString());
		String jsonGameMetadata = mapper.writeValueAsString(gmd);
		authorization.invokeAndWaitResult(chain.godWallet, "submit_game_proposal", 
				new RpcObject.Builder()
				.put("_gamedata", new RpcValue(jsonGameMetadata))
				.build(),
				BigInteger.valueOf(50).multiply(MULTIPLIER)
				,null);

		authorization.invokeAndWaitResult(chain.godWallet, "set_game_ready", 
				new RpcObject.Builder()
				.put("_scoreAddress", new RpcValue(daoletteGame.getAddress()))
				.build());

		authorization.invokeAndWaitResult(chain.godWallet, "set_game_status", 
				new RpcObject.Builder()
				.put("_status", new RpcValue("gameApproved"))
				.put("_scoreAddress", new RpcValue(daoletteGame.getAddress()))
				.build());

		//authorize tresury (daolette)
		daolette.invokeAndWaitResult(chain.godWallet, "game_on", new RpcObject.Builder().build());

		gmd = new GameMetadata();
		gmd.setMaxPayout("5900000000000000000");
		gmd.setName("test-daolette");
		gmd.setScoreAddress(daolette.getAddress().toString());
		gmd.setMinBet("200000000000000000");
		gmd.setGameType("Per wager settlement");
		gmd.setRevShareWalletAddress(daolette.getAddress().toString());
		jsonGameMetadata = mapper.writeValueAsString(gmd);
		authorization.invokeAndWaitResult(chain.godWallet, "submit_game_proposal", 
				new RpcObject.Builder()
				.put("_gamedata", new RpcValue(jsonGameMetadata))
				.build(),
				BigInteger.valueOf(50).multiply(MULTIPLIER)
				,null);

		authorization.invokeAndWaitResult(chain.godWallet, "set_game_ready", 
				new RpcObject.Builder()
				.put("_scoreAddress", new RpcValue(daolette.getAddress()))
				.build());

		authorization.invokeAndWaitResult(chain.godWallet, "set_game_status", 
				new RpcObject.Builder()
				.put("_status", new RpcValue("gameApproved"))
				.put("_scoreAddress", new RpcValue(daolette.getAddress()))
				.build());


		//--------------  configuring games
		daolette.invokeAndWaitResult(chain.godWallet, "set_rewards_score", 
				new RpcObject.Builder()
				.put("_score", new RpcValue(rewardDistribution.getAddress()))
				.build());

		rewardDistribution.invokeAndWaitResult(chain.godWallet, "set_game_score", 
				new RpcObject.Builder()
				.put("_score", new RpcValue(daolette.getAddress()))
				.build());

		authorization.invokeAndWaitResult(chain.godWallet, "set_roulette_score", 
				new RpcObject.Builder()
				.put("_scoreAddress", new RpcValue(daolette.getAddress()))
				.build());

		//send some ICX to treasury, so it have some funds for the first game ever.
		daolette.invokeAndWaitResult(chain.godWallet, "add_to_excess", 
				new RpcObject.Builder()
				.build(),
				BigInteger.valueOf(100).multiply(MULTIPLIER)
				,null);

		//bet on daolette game
		TransactionResult txn = daoletteGame.invokeAndWaitResult(chain.godWallet, "bet_on_even_odd", 
				new RpcObject.Builder()
				.put("even_odd", new RpcValue(false))
				.put("user_seed", new RpcValue("3,17,6"))
				.build(),
				new BigInteger("200000000000000000"),
				new BigInteger("100000000"));

		assertEquals(BigInteger.ONE, txn.getStatus());
		Optional<EventLog> daoletteGameEvent = txn.getEventLogs().stream().filter(e-> e.getScoreAddress().equals(daoletteGame.getAddress().toString())).findFirst();
		assertTrue(daoletteGameEvent.isPresent());
		System.out.println(txn);

	}

}

class GameMetadata{
	private String name;
	private String scoreAddress;
	private String minBet;
	private String maxBet;
	private String houseEdge;
	private String gameType;
	private String revShareMetadata;
	private String revShareWalletAddress;
	private String linkProofPage;
	private String gameUrlMainnet;
	private String gameUrlTestnet;
	private String maxPayout;

	public String getMaxPayout() {
		return maxPayout;
	}
	public void setMaxPayout(String maxPayout) {
		this.maxPayout = maxPayout;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getScoreAddress() {
		return scoreAddress;
	}
	public void setScoreAddress(String scoreAddress) {
		this.scoreAddress = scoreAddress;
	}
	public String getMinBet() {
		return minBet;
	}
	public void setMinBet(String minBet) {
		this.minBet = minBet;
	}
	public String getMaxBet() {
		return maxBet;
	}
	public void setMaxBet(String maxBet) {
		this.maxBet = maxBet;
	}
	public String getHouseEdge() {
		return houseEdge;
	}
	public void setHouseEdge(String houseEdge) {
		this.houseEdge = houseEdge;
	}
	public String getGameType() {
		return gameType;
	}
	public void setGameType(String gameType) {
		this.gameType = gameType;
	}
	public String getRevShareMetadata() {
		return revShareMetadata;
	}
	public void setRevShareMetadata(String revShareMetadata) {
		this.revShareMetadata = revShareMetadata;
	}
	public String getRevShareWalletAddress() {
		return revShareWalletAddress;
	}
	public void setRevShareWalletAddress(String revShareWalletAddress) {
		this.revShareWalletAddress = revShareWalletAddress;
	}
	public String getLinkProofPage() {
		return linkProofPage;
	}
	public void setLinkProofPage(String linkProofPage) {
		this.linkProofPage = linkProofPage;
	}
	public String getGameUrlMainnet() {
		return gameUrlMainnet;
	}
	public void setGameUrlMainnet(String gameUrlMainnet) {
		this.gameUrlMainnet = gameUrlMainnet;
	}
	public String getGameUrlTestnet() {
		return gameUrlTestnet;
	}
	public void setGameUrlTestnet(String gameUrlTestnet) {
		this.gameUrlTestnet = gameUrlTestnet;
	}
}