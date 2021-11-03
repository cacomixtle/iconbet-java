package com.iconbet.score.daolette.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.math.BigInteger;
import java.util.Map;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iconloop.score.test.Account;
import com.iconloop.score.test.Score;
import com.iconloop.score.test.ServiceManager;
import com.iconloop.score.test.TestBase;

class DaoletteGameTest extends TestBase{

	private static final ServiceManager sm = getServiceManager();
	private static final Account owner = sm.createAccount();
	private static final BigInteger totalSupply = BigInteger.valueOf(50000000000L);

	private static Score daolettGame;
	private ObjectMapper mapper = new ObjectMapper();

	@BeforeAll
	public static void init() {
		owner.addBalance(DaoletteGame.TAG, totalSupply);
	}

	@BeforeEach
	public void setup() throws Exception {
		daolettGame = sm.deploy(owner, DaoletteGame.class);
	}

	@SuppressWarnings("unchecked")
	@Test
	void testGetMultipliers() throws JsonParseException, JsonMappingException, IOException {
		String jsonString = (String) daolettGame.call("get_multipliers");
		Map<String, Float> multipliers = mapper.readValue(jsonString, Map.class);
		String expectedJsonString = "{\"bet_on_color\": 2, \"bet_on_even_odd\": 2, \"bet_on_number\": 20, \"number_factor\": 20.685}";
		Map<String, Float> expectedMultipliers = mapper.readValue(expectedJsonString, Map.class);

		assertEquals(expectedMultipliers, multipliers);

	}

}
