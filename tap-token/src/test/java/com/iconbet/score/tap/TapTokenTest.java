package com.iconbet.score.tap;

import static java.math.BigInteger.TEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

import score.Address;

class TapTokenTest extends TestBase {

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
		tapToken = sm.deploy(owner, TapToken.class, initialSupply, decimals);
	}

	@Test
	void name() {
		assertEquals(TapToken.TAG, tapToken.call("name"));
	}

	@Test
	void symbol() {
		assertEquals(symbol, tapToken.call("symbol"));
	}

	@Test
	void decimals() {
		assertEquals(decimals.intValue(), tapToken.call("decimals"));
	}

	@Test
	void totalSupply() {
		assertEquals(totalSupply, tapToken.call("totalSupply"));
	}

	@Test
	void balanceOf() {
		assertEquals(owner.getBalance(symbol),
				tapToken.call("balanceOf", tapToken.getOwner().getAddress()));
	}

	@Test
	void transfer() {

		Account alice = sm.createAccount();
		BigInteger value = TEN.pow(decimals.intValue());

		tapToken.invoke(owner, "transfer", alice.getAddress(), value, "to alice".getBytes());
		owner.subtractBalance(symbol, value);

		assertEquals(owner.getBalance(symbol),
				tapToken.call("balanceOf", tapToken.getOwner().getAddress()));
		assertEquals(value,
				tapToken.call("balanceOf", alice.getAddress()));

		// transfer self
		tapToken.invoke(alice, "transfer", alice.getAddress(), value, "self transfer".getBytes());
		assertEquals(value, tapToken.call("balanceOf", alice.getAddress()));
	}

	@Test
	void togglePaused() {
		assertFalse((Boolean)tapToken.call("getPaused"));
		tapToken.invoke(owner, "togglePaused");
		assertTrue((Boolean)tapToken.call("getPaused"));
	}

	@Test
	void transferPaused() {
		Account alice = sm.createAccount();
		BigInteger value = TEN.pow(decimals.intValue());

		tapToken.invoke(owner, "togglePaused");
		Address a = alice.getAddress();
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(owner, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): TAP token transfers are paused.", e.getMessage());

	}

	@Test
	void testWhitelist() {
		Account alice = sm.createAccount();
		tapToken.invoke(owner, "set_whitelist_address", alice.getAddress());

		@SuppressWarnings("unchecked")
		List<Address> addresses = (List<Address>)tapToken.call("get_whitelist_addresses");

		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals(alice.getAddress(), addresses.get(0));
	}

	@Test
	void testToggleStakingToEnable() {

		tapToken.invoke(owner, "toggle_staking_enabled");
		Boolean enabled = (Boolean)tapToken.call("staking_enabled");
		assertTrue(enabled);
	}

	@Test
	void testLockList() {
		Account alice = sm.createAccount();
		tapToken.invoke(owner, "toggle_staking_enabled");
		tapToken.invoke(owner, "set_locklist_address", alice.getAddress());

		@SuppressWarnings("unchecked")
		List<Address> addresses = (List<Address>)tapToken.call("get_locklist_addresses");

		assertNotNull(addresses);
		assertEquals(1, addresses.size());
		assertEquals(alice.getAddress(), addresses.get(0));
	}

	@Test
	void testTransferFailByLockList() {
		Account alice = sm.createAccount();
		Address a = alice.getAddress();
		tapToken.invoke(owner, "toggle_staking_enabled");
		tapToken.invoke(owner, "set_locklist_address", a);

		BigInteger value = TEN.pow(decimals.intValue());
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(alice, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): Transfer of TAP has been locked for this address.", e.getMessage());
	}

	@Test
	void testTransferNegative() {
		Account alice = sm.createAccount();
		Address a = alice.getAddress();

		BigInteger value = BigInteger.valueOf(-1);
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(alice, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): Transferring value cannot be less than zero", e.getMessage());
	}

	@Test
	void testTransferOutOfBalance() {
		Account alice = sm.createAccount();
		Address a = alice.getAddress();

		BigInteger value = TEN.pow(decimals.intValue());
		byte[] data = "to alice".getBytes();
		AssertionError e = Assertions.assertThrows(AssertionError.class, () -> {
			tapToken.invoke(alice, "transfer", a, value, data);
		});

		assertEquals("Reverted(0): Out of balance", e.getMessage());
	}

	@Test
	void testStakedBalance() {
		Account alice = sm.createAccount();
		BigInteger balance = (BigInteger)tapToken.call("staked_balance_of", alice.getAddress());

		assertNotNull(balance);
		assertEquals(BigInteger.ZERO, balance);
	}

	@Test
	void testUnStakedBalance() {
		Account alice = sm.createAccount();
		BigInteger balance = (BigInteger)tapToken.call("unstaked_balance_of", alice.getAddress());

		assertNotNull(balance);
		assertEquals(BigInteger.ZERO, balance);
	}

	@Test
	void testTotalStakedBalance() {
		BigInteger balance = (BigInteger)tapToken.call("total_staked_balance");

		assertNotNull(balance);
		assertEquals(BigInteger.ZERO, balance);
	}

	@Test
	void testSwitchDivsToStakedTapEnabled() {
		Boolean active = (Boolean)tapToken.call("switch_divs_to_staked_tap_enabled");

		assertNotNull(active);
		assertFalse(active);
	}

	@Test
	void testDetailsBalance() {
		Account alice = sm.createAccount();
		@SuppressWarnings("unchecked")
		Map<String, BigInteger> details = (Map<String, BigInteger>)tapToken.call("details_balanceOf", alice.getAddress());

		assertNotNull(details);
		assertEquals(BigInteger.ZERO, details.get("Total balance"));
		assertEquals(BigInteger.ZERO, details.get("Available balance"));
		assertEquals(BigInteger.ZERO, details.get("Staked balance"));
		assertEquals(BigInteger.ZERO, details.get("Unstaking balance"));
		assertEquals(BigInteger.ZERO, details.get("Unstaking time (in microseconds)"));
	}
}
