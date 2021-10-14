package com.iconbet.score.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

class RewardDistributionTest extends TestBase{

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static final BigInteger totalSupply = BigInteger.valueOf(50000000000L);

	private static final String symbol = "TAP";
	private static Score rewardDistribution;

	@BeforeAll
	public static void init() {
		owner.addBalance(symbol, totalSupply);
	}

	@BeforeEach
	public void setup() throws Exception {
		rewardDistribution = sm.deploy(owner, RewardDistribution.class);
	}

	@Test
	void testGetTodaysTotalWagers() {
		BigInteger result = (BigInteger)rewardDistribution.call("get_todays_total_wagers");
		assertEquals(BigInteger.ZERO, result);
	}
	
}
