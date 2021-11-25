package com.iconbet.score.tap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import score.Context;

class TapTokenStakeTest extends TestBase{

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static final BigInteger initialSupply = BigInteger.valueOf(5);
	private static final BigInteger decimals = BigInteger.valueOf(10);
	private static final BigInteger totalSupply = BigInteger.valueOf(50000000000L);

	private static final String symbol = "TAP";
	private static Score tapToken;

	@BeforeAll
	public static void init() {
		owner.addBalance(symbol, totalSupply);
	}

	@BeforeEach
	public void setup() throws Exception {
		tapToken = sm.deploy(owner, TapToken.class, initialSupply, decimals, false);
	}
	@SuppressWarnings("unchecked")
	@Test
	void testStake() {
		Account alice = sm.createAccount();
		BigInteger value = BigInteger.TEN.pow(decimals.intValue());

		Boolean paused = (Boolean)tapToken.call("getPaused");
		if(paused) {
			tapToken.invoke(owner, "togglePaused");
		}

		tapToken.invoke(owner, "transfer", alice.getAddress(), value, "to alice".getBytes());
		owner.subtractBalance(symbol, value);

		Map<String, BigInteger> details = (Map<String, BigInteger>)tapToken.call("details_balanceOf", alice.getAddress());
		assertNotNull(details);
		Context.println(details.toString());

		tapToken.invoke(owner, "toggle_staking_enabled");
		assertTrue((Boolean)tapToken.call("staking_enabled"));

		BigInteger valueToStake = BigInteger.ONE.multiply(BigInteger.TEN.pow(decimals.intValue())).divide(BigInteger.TWO);
		tapToken.invoke(alice, "stake", valueToStake);

		details = (Map<String, BigInteger>)tapToken.call("details_balanceOf", alice.getAddress());
		assertNotNull(details);
		Context.println(details.toString());
		assertEquals(value, details.get("Total balance"));
		assertEquals(value.subtract(valueToStake), details.get("Available balance"));
		assertEquals(valueToStake, details.get("Staked balance"));

	}
}
