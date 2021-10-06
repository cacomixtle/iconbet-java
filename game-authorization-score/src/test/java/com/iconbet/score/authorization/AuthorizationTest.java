package com.iconbet.score.authorization;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

class AuthorizationTest extends TestBase {

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
}
