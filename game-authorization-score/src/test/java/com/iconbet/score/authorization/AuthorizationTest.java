package com.iconbet.score.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonValue;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

class AuthorizationTest extends TestBase {

	private ObjectMapper mapper = new ObjectMapper();

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static Score authorization;

	@BeforeAll
	static void init() {
	}

	@BeforeEach
	void setup() throws Exception {
		authorization = sm.deploy(owner, Authorization.class);
	}

	@Test
	void testSetGameDevelopersShare() {
		BigInteger given = BigInteger.valueOf(10);
		authorization.invoke(owner, "set_game_developers_share", given);
		BigInteger shares = (BigInteger)authorization.call("get_game_developers_share");
		assertEquals(shares, given);

	}
	//get_excess
	//_game_auth_score
	//to test daolette.get_excess

	@Test
	void testJson() throws JsonProcessingException {
		GameMetadata gmd = new GameMetadata();
		String jsonGameMetadata = mapper.writeValueAsString(gmd);

		JsonValue json = Json.parse(jsonGameMetadata);
		assertFalse(json.isArray());
		assertTrue(json.isObject());

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