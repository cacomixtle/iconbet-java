package com.iconbet.score.promotion;

import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

public class Promotion {
	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "Promotion";
	public static BigInteger TEN_18 = new BigInteger("1000000000000000000");
	public static int[] WAGER_WAR_PRIZE = new int[] {25, 20, 15, 10, 10, 6, 6, 3, 3, 2};

	private String _REWARDS_SCORE = "rewards_score";
	private String _DIVIDENDS_SCORE = "dividends_score";
	private String _TOTAL_PRIZES = "total_prizes";

	public VarDB<Address> _rewards_score = Context.newVarDB(this._REWARDS_SCORE, Address.class);
	public VarDB<Address> _dividends_score = Context.newVarDB(this._DIVIDENDS_SCORE, Address.class);
	public VarDB<BigInteger> _total_prizes = Context.newVarDB(this._TOTAL_PRIZES, BigInteger.class);

	public Promotion(){
		this._total_prizes.set(ZERO);
	}

	@EventLog(indexed=2)
	public void FundTransfer(String to, BigInteger amount, String note) {}

	@External(readonly=true)
	public String name() {
		return "ICONbet Promotion";
	}

	/*
    Sets the rewards core address. Only owner can set the address
    :param _score: Address of the rewards score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_rewards_score(Address _score) {
		if ( Context.getCaller().equals(Context.getOwner()) ){
			this._rewards_score.set(_score);
		}
	}

	/*
    Returns the Rewards score address
    :return: Address of the rewards score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_rewards_score() {
		return this._rewards_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Sets the dividends score address. Only owner can set the address
    :param _score: Address of the dividends score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_dividends_score(Address _score) {
		if ( Context.getCaller().equals(Context.getOwner()) ){
			this._dividends_score.set(_score);
		}
	}

	/*
    Returns the dividends score address
    :return: Address of the dividends score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_dividends_score() {
		return this._dividends_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Distributes the prizes it receive to the top 10 wagerers
    :return:
	 */
	@SuppressWarnings("unchecked")
	public void _distribute_prizes() {
		String json = Context.call(String.class, this._rewards_score.get(),  "get_daily_wager_totals");

		JsonObject wagerTotals = Json.parse(json).asObject();
		Iterator<Member> it = wagerTotals.get("yesterday").asObject().iterator();

		Map.Entry<String, BigInteger>[] wagers = new Map.Entry[wagerTotals.get("yesterday").asObject().size()];

		int j = 0;
		while(it.hasNext()) {
			Member t = it.next();
			wagers[j] = Map.entry(t.getName(), new BigInteger(t.getValue().asString())); 
			j++;
		}

		Comparator<Map.Entry<String, BigInteger>> c = new Comparator<>() {
			@Override
			public int compare(Entry<String, BigInteger> a, Entry<String, BigInteger> b) {
				return a.getValue().compareTo(b.getValue());
			}
		};

		ArrayUtils.quickSort(wagers, 0, wagers.length, c);

		Map.Entry<String, BigInteger>[] topWagers = (Map.Entry<String, BigInteger>[])ArrayUtils.top(wagers, 10, true);

		int totalPercent = 0;
		for(int i = topWagers.length-1 ; i >= 0 ; i-- ) {
			totalPercent = totalPercent + WAGER_WAR_PRIZE[i]; 
		}

		int i = 0;
		//TODO: test this logic in depth
		BigInteger totalPrizes = this._total_prizes.get();
		for (Map.Entry<String, BigInteger> es: topWagers) {
			String address =  es.getKey();
			BigInteger prize = BigInteger.valueOf(WAGER_WAR_PRIZE[i]).multiply(totalPrizes).divide(BigInteger.valueOf(totalPercent));
			totalPercent -= WAGER_WAR_PRIZE[i];
			totalPrizes = totalPrizes.subtract(prize);
			try {
				Context.transfer(Address.fromString(address), prize);
				this.FundTransfer(address, prize, "Wager Wars prize distribution");
			}catch (Exception e) {
				Context.revert("Network problem. Prize not sent. Will try again later. Exception:"+ e.getMessage());
			}
			i++;
		}
		this._total_prizes.set(ZERO);
	}

	@Payable
	public void fallback() {
		if ( this._dividends_score.get()!= null && Context.getCaller().equals(this._dividends_score.get())){
			this._total_prizes.set(Context.getValue());
			this._distribute_prizes();
		}else {
			Context.revert("Funds can only be accepted from the dividends distribution contract");
		}
	}

}
