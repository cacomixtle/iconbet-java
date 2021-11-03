package com.iconbet.score.daolette.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;

class NumTest {
	private static final List<Integer> numbers = List.of(2,4,6,8,10,12,14,16,18,20);

	private static final Map<String, Number> MULTIPLIERS = Map.of(
			"bet_on_color", 2,
			"bet_on_even_odd", 2,
			"bet_on_number", 20,
			"number_factor", 20.685f);
	private static final String[] BET_TYPES = new String[] {"none", "bet_on_numbers", "bet_on_color", "bet_on_even_odd", "bet_on_number", "number_factor"};

	@Test
	void testMultipliers() {
		//just testing a specific part of the code here, to see the result
		BigInteger amount = new BigInteger("200000000000000000");

		BigInteger payout = BigInteger.valueOf( Float.valueOf(MULTIPLIERS.get(BET_TYPES[5]).floatValue() * 1000).intValue() ).multiply(amount).divide(BigInteger.valueOf(1000*numbers.size()));

		System.out.println(payout);
		//413700000000000000
		//200000000000000000
		//payout = int(MULTIPLIERS[BET_TYPES[5]] * 1000) * amount // (1000 * n)
		int part = (int)(20.685f * 1000);
		assertEquals(part , Float.valueOf(MULTIPLIERS.get(BET_TYPES[5]).floatValue() * 1000).intValue());
		assertEquals(BigInteger.valueOf(part) , BigInteger.valueOf( Float.valueOf(MULTIPLIERS.get(BET_TYPES[5]).floatValue() * 1000).intValue()) );
		assertEquals(BigInteger.valueOf(part) , BigInteger.valueOf( (int)(MULTIPLIERS.get(BET_TYPES[5]).floatValue() * 1000) ) );

	}

}
