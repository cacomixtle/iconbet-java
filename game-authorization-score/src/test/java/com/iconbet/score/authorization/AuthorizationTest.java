package com.iconbet.score.authorization;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

public class AuthorizationTest extends TestBase {

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static Score authorization;
	
	@BeforeAll
	public static void init() {
	
		
	}
	
	
	@BeforeEach
	public void setup() throws Exception {
		authorization = sm.deploy(owner, Authorization.class);
	}
	
}
