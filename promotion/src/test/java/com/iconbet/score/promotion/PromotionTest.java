package com.iconbet.score.promotion;

import static java.math.BigInteger.ZERO;

import java.math.BigInteger;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import score.Address;
import score.Context;
import score.VarDB;

@SuppressWarnings("unchecked")
class PromotionTest {

	Promotion promotion;

	VarDB<Address> _rewards_score = Mockito.mock(VarDB.class);
	VarDB<Address> _dividends_score = Mockito.mock(VarDB.class);
	VarDB<BigInteger> _total_prizes = Mockito.mock(VarDB.class); 

	@Test
	void testDistributePrizesYesterday() {
		String dailyWagerTotalsJson = "{\"yesterday\": {\"hxbe258ceb872e08851f1f59694dac2558708ece11\" :\"100\"}}";
		Address rewardsScore = Address.fromString("cx6274eb4b9db589a6b0721284d1521558f5dcf386");

		try(MockedStatic<Context> theMock  = Mockito.mockStatic(Context.class)){

			theMock
			.when(() -> Context.newVarDB("total_prizes", BigInteger.class))
			.thenReturn(_total_prizes);

			theMock
			.when(() -> Context.newVarDB("rewards_score", Address.class))
			.thenReturn(_rewards_score);

			theMock
			.when(() ->  Context.call(String.class, rewardsScore, "get_daily_wager_totals"))
			.thenReturn(dailyWagerTotalsJson);

			Mockito.when(_rewards_score.get()).thenReturn(rewardsScore);
			Mockito.when(_total_prizes.get()).thenReturn(BigInteger.valueOf(1000));

			promotion = new Promotion(false);
			promotion._distribute_prizes();
			Mockito.verify(_total_prizes, Mockito.times(2)).set(ZERO);

		}
	}

	@Test
	void testFallback() {
		Address dividendsScore = Address.fromString("cx8c80fea64cc28d5abfaf3259b08f81a265b559a8");
		Address rewardScore = Address.fromString("cx6274eb4b9db589a6b0721284d1521558f5dcf386");

		try(MockedStatic<Context> theMock  = Mockito.mockStatic(Context.class)){
			theMock
			.when(() -> Context.newVarDB("dividends_score", Address.class))
			.thenReturn(_dividends_score);

			theMock
			.when(() -> Context.getCaller())
			.thenReturn(rewardScore);

			Mockito.when(_dividends_score.get()).thenReturn(dividendsScore);
			Mockito.verifyNoInteractions(_total_prizes);
		}
	}
}
