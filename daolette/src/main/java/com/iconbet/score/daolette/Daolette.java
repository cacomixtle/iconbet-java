package com.iconbet.score.daolette;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

public class Daolette{
	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);
	private static final String TAG = "ICONbet Treasury";

	// Treasury minimum 2.5E+23, or 250,000 ICX.
	private static final BigInteger TREASURY_MINIMUM = new BigInteger("250000000000000000000000");

	//TODO: verify this datatype
	private static final int[] BET_LIMIT_RATIOS = new int[] {147, 2675, 4315, 2725, 1930, 1454, 1136, 908, 738, 606,
			500, 413, 341, 280, 227, 182, 142, 107, 76, 48, 23};
	private static final BigInteger BET_MIN = new BigInteger("100000000000000000"); // 1.0E+17, .1 ICX
	private static final BigInteger	U_SECONDS_DAY = BigInteger.valueOf(86400000000L); // Microseconds in a day.

	private static final int TX_MIN_BATCH_SIZE = 10;
	private static final int TX_MAX_BATCH_SIZE = 500;
	private static final int DIST_DURATION_PARAM = 50;  // Units of 1/Days

	private static final String[] BET_TYPES = new String[] {"none", "bet_on_numbers", "bet_on_color", "bet_on_even_odd", "bet_on_number", "number_factor"};
	private static final Set<Integer> WHEEL_ORDER = new HashSet<>(Arrays.asList(2, 20, 3, 17, 6, 16, 7, 13, 10, 12,
			11, 9, 14, 8, 15, 5, 18, 4, 19, 1, 0));
	private static final Set<Integer> WHEEL_BLACK = new HashSet<>(Arrays.asList(2,3,6,7,10,11,14,15,18,19));
	private static final Set<Integer> SET_BLACK = new HashSet<>(Arrays.asList(2, 3, 6, 7, 10, 11, 14, 15, 18, 19));
	private static final Set<Integer> WHEEL_RED = new HashSet<>( Arrays.asList(1,4,5,8,9,12,13,16,17,20));
	private static final Set<Integer> SET_RED = new HashSet<>(Arrays.asList(1, 4, 5, 8, 9, 12, 13, 16, 17, 20));
	private static final Set<Integer> WHEEL_ODD = new HashSet<>( Arrays.asList(1,3,5,7,9,11,13,15,17,19));
	private static final Set<Integer> SET_ODD = new HashSet<>( Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15, 17, 19));
	private static final Set<Integer> WHEEL_EVEN = new HashSet<>( Arrays.asList(2,4,6,8,10,12,14,16,18,20));
	private static final Set<Integer> SET_EVEN = new HashSet<>( Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20));
	private static final Map<String, Float> MULTIPLIERS = Map.of("bet_on_color", 2f,
			"bet_on_even_odd", 2f,
			"bet_on_number", 20f,
			"number_factor", 20.685f);

	private String _EXCESS = "house_excess";
	private String _EXCESS_TO_DISTRIBUTE = "excess_to_distribute";

	private String _TOTAL_DISTRIBUTED = "total_distributed";
	private String _GAME_ON = "game_on";

	private String _BET_TYPE = "bet_type";
	private String _TREASURY_MIN = "treasury_min";
	private String _BET_LIMITS = "bet_limits";
	private String _DAY = "day";
	private String _SKIPPED_DAYS = "skipped_days";
	private String _DAILY_BET_COUNT = "daily_bet_count";
	private String _TOTAL_BET_COUNT = "total_bet_count";
	private String _YESTERDAYS_BET_COUNT = "yesterdays_bet_count";
	private String _TOKEN_SCORE = "token_score";
	private String _REWARDS_SCORE = "rewards_score";
	private String _DIVIDENDS_SCORE = "dividends_score";

	private String _VOTE = "vote";
	private String _VOTED = "voted";
	private String _YES_VOTES = "yes_votes";
	private String _NO_VOTES = "no_votes";
	private String _OPEN_TREASURY = "open_treasury";
	private String _GAME_AUTH_SCORE = "game_auth_score";

	private String _NEW_DIV_LIVE = "new_div_live";
	private String _TREASURY_BALANCE = "treasury_balance";

	private String _EXCESS_SMOOTHING_LIVE = "excess_smoothing_live";

	private String _DAOFUND_SCORE = "daofund_score";
	private String _YESTERDAYS_EXCESS = "yesterdays_excess";
	private String _DAOFUND_TO_DISTRIBUTE = "daofund_to_distribute";

	private VarDB<BigInteger> _excess = Context.newVarDB(this._EXCESS, BigInteger.class);
	private VarDB<BigInteger> _total_distributed = Context.newVarDB(this._TOTAL_DISTRIBUTED, BigInteger.class);
	private VarDB<Boolean> _game_on = Context.newVarDB(this._GAME_ON, Boolean.class);
	private VarDB<String> _bet_type = Context.newVarDB(this._BET_TYPE, String.class);
	private VarDB<BigInteger> _treasury_min = Context.newVarDB(this._TREASURY_MIN, BigInteger.class);
	private DictDB<BigInteger, BigInteger> _bet_limits = Context.newDictDB(this._BET_LIMITS, BigInteger.class);
	private VarDB<BigInteger> _day = Context.newVarDB(this._DAY, BigInteger.class);
	private VarDB<BigInteger> _skipped_days = Context.newVarDB(this._SKIPPED_DAYS, BigInteger.class);
	private VarDB<BigInteger> _total_bet_count = Context.newVarDB(this._TOTAL_BET_COUNT, BigInteger.class);
	private VarDB<BigInteger> _daily_bet_count = Context.newVarDB(this._DAILY_BET_COUNT, BigInteger.class);
	private VarDB<BigInteger> _yesterdays_bet_count = Context.newVarDB(this._YESTERDAYS_BET_COUNT, BigInteger.class);
	private VarDB<Address> _token_score = Context.newVarDB(this._TOKEN_SCORE, Address.class);
	private VarDB<Address> _rewards_score = Context.newVarDB(this._REWARDS_SCORE, Address.class);
	private VarDB<Address> _dividends_score = Context.newVarDB(this._DIVIDENDS_SCORE, Address.class);

	private DictDB<Address, String> _vote = Context.newDictDB(this._VOTE, String.class);
	private ArrayDB<Address> _voted = Context.newArrayDB(this._VOTED, Address.class);
	private VarDB<BigInteger> _yes_votes = Context.newVarDB(this._YES_VOTES, BigInteger.class);
	private VarDB<BigInteger> _no_votes = Context.newVarDB(this._NO_VOTES, BigInteger.class);
	private VarDB<Boolean> _open_treasury = Context.newVarDB(this._OPEN_TREASURY, Boolean.class);
	private VarDB<Address> _game_auth_score = Context.newVarDB(this._GAME_AUTH_SCORE, Address.class);

	private VarDB<Boolean> _new_div_live = Context.newVarDB(this._NEW_DIV_LIVE, Boolean.class);
	private VarDB<BigInteger> _excess_to_distribute = Context.newVarDB(this._EXCESS_TO_DISTRIBUTE, BigInteger.class);
	private VarDB<BigInteger> _treasury_balance = Context.newVarDB(this._TREASURY_BALANCE, BigInteger.class);

	private VarDB<Boolean> _excess_smoothing_live = Context.newVarDB(this._EXCESS_SMOOTHING_LIVE, Boolean.class);

	private VarDB<Address> _daofund_score = Context.newVarDB(this._DAOFUND_SCORE, Address.class);
	private VarDB<BigInteger> _yesterdays_excess = Context.newVarDB(this._YESTERDAYS_EXCESS, BigInteger.class);
	private VarDB<BigInteger> _daofund_to_distirbute = Context.newVarDB(this._DAOFUND_TO_DISTRIBUTE, BigInteger.class);

	@EventLog(indexed=2)
	public void FundTransfer(Address recipient, BigInteger amount, String note) {}

	@EventLog(indexed=2)
	public void FundReceived(Address sender, BigInteger amount, String note) {}

	@EventLog(indexed=2)
	public void BetSource(Address _from, int timestamp){}

	@EventLog(indexed=2)
	public void BetPlaced(int amount, String numbers){}

	@EventLog(indexed=2)
	public void BetResult(String spin, String winningNumber, BigInteger payout){}

	@EventLog(indexed=3)
	public void DayAdvance(int day, int skipped, int block_time, String note){}

	@EventLog(indexed=2)
	public void Vote(Address _from, String _vote, String note){}

	public Daolette() {
		Context.println("In __init__. "+ TAG);
		Context.println("owner is "+Context.getOwner()+". "+TAG);

		this._excess.set(BigInteger.ZERO);
		this._total_distributed.set(BigInteger.ZERO);
		this._game_on.set(false);
		this._bet_type.set(BET_TYPES[0]);
		this._treasury_min.set(TREASURY_MINIMUM);
		this._set_bet_limit();
		this._day.set(BigInteger.valueOf(Context.getTransactionTimestamp())); // U_SECONDS_DAY)
		this._skipped_days.set(BigInteger.ZERO);
		this._total_bet_count.set(BigInteger.ZERO);
		this._daily_bet_count.set(BigInteger.ZERO);
		this._yesterdays_bet_count.set(BigInteger.ZERO);
		this._yes_votes.set(BigInteger.ZERO);
		this._no_votes.set(BigInteger.ZERO);
		this._open_treasury.set(false);
		this._game_auth_score.set(ZERO_ADDRESS);
		this._excess_smoothing_live.set(false);
	}

	@External
	public void toggle_excess_smoothing() {
		/*
        Toggles the status of excess smoothing between true and false. If its true, it keeps the 10% of excess to be
        distributed to tap holders and wager war in the treasury itself making a positive start for next day. If false,
        the feature is disabled
        :return:
		 */
		if ( !Context.getCaller().equals(Context.getOwner())) {
			Context.revert("This method can only be invoked by the score owner. You are trying for unauthorized access");
		}
		this._excess_smoothing_live.set(!this._excess_smoothing_live.get());
	}

	@External(readonly=true)
	public Boolean get_excess_smoothing_status() {
		/*
        Status of excess smoothing.
        :return: Returns the boolean value representing the status of excess smoothing
		 */
		return this._excess_smoothing_live.get();
	}

	@External
	public void set_token_score(Address _score) {
		/*
        Sets the token score address. Only owner can set the address.
        :param _score: Address of the token score
        :type _score: :class:`iconservice.base.address.Address`
        :return:
		 */
		if ( Context.getCaller().equals(Context.getOwner())){
			this._token_score.set(_score);
		}
	}

	@External(readonly=true)
	public Address get_token_score() {
		/*
        Returns the token score address
        :return: TAP token score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
		return this._token_score.getOrDefault(ZERO_ADDRESS);
	}

	@External
	public void set_rewards_score(Address _score) {
		/*
        Sets the rewards score address. Only owner can set the address.
        :param _score: Address of the rewards score
        :type _score: :class:`iconservice.base.address.Address`
        :return:
		 */
		if ( Context.getCaller().equals(Context.getOwner())){
			this._rewards_score.set(_score);
		}
	}

	@External(readonly=true)
	public Address get_rewards_score() {
		/*
        Returns the rewards score address
        :return: Rewards score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
		return this._rewards_score.getOrDefault(ZERO_ADDRESS);
	}

	@External
	public void set_dividends_score(Address _score) {
		/*
        Sets the dividends score address. Only owner can set the address.
        :param _score: Address of the dividends score address
        :type _score: :class:`iconservice.base.address.Address`
        :return:
		 */
		if ( Context.getCaller().equals(Context.getOwner())){
			this._dividends_score.set(_score);
		}
	}

	@External(readonly=true)
	public Address get_dividends_score() {
		/*
        Returns the dividends score address
        :return: Dividends score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
		return this._dividends_score.getOrDefault(ZERO_ADDRESS);
	}

	@External
	public void set_game_auth_score(Address _score) {
		/*
        Sets the game authorization score address. Only owner can set this address
        :param _score: Address of the game authorization score
        :type _score: :class:`iconservice.base.address.Address`
        :return:
		 */
		if ( Context.getCaller().equals(Context.getOwner())){
			this._game_auth_score.set(_score);
		}
	}

	@External(readonly=true)
	public Address get_game_auth_score() {
		/*
        Returns the game authorization score address
        :return: Game authorization score address
        :rtype: :class:`iconservice.base.address.Address`
		 */
		return this._game_auth_score.getOrDefault(ZERO_ADDRESS);
	}

	@External(readonly=true)
	public Boolean get_treasury_status() {
		/*
        Returns the status of treasury. If the treasury is to be dissolved it returns True
        :return: True if treasury is to be dissolved
        :rtype: bool
		 */
		return this._open_treasury.getOrDefault(false);
	}

	@External
	@Payable
	public void set_treasury() {
		/*
        Anyone can add amount to the treasury and increase the treasury minimum
        Receives the amount and updates the treasury minimum value.
        Can increase treasury minimum with multiples of 10,000 ICX
        :return:
		 */
		if (Context.getValue().compareTo(BigInteger.TEN.pow(22)) <  0) {
			Context.revert(TAG + " : set_treasury method doesnt accept ICX less than 10000 ICX");
		}
		if ( Context.getValue().mod(BigInteger.TEN.pow(22)).compareTo(BigInteger.ZERO)  != 0) {
			Context.revert(TAG +" : Set treasury error, Please send amount in multiples of 10,000 ICX");
		}

		this._treasury_min.set(this._treasury_min.get().add(Context.getValue() ));
		Context.println("Increasing treasury minimum by " + Context.getValue() + " to " + this._treasury_min.get());
		this._set_bet_limit();
		this._open_treasury.set(false);
		this.FundReceived(Context.getCaller(), Context.getValue(), "Treasury minimum increased by " + Context.getValue());
		Context.println(Context.getValue() + " was added to the treasury from address "+ Context.getCaller() + " "+ TAG);
	}

	/*
    Sets the bet limits for the new treasury minimum
    :return:
	 */
	private void _set_bet_limit() {
		for(int i: BET_LIMIT_RATIOS) {
			this._bet_limits.set(BigInteger.valueOf(i), this._treasury_min.get()); // ratio
		}
	}

	/*
    Turns on the game. Only owner can turn on the game
    :return:
	 */
	@External
	public void game_on() {
		if ( !Context.getCaller().equals(Context.getOwner())) { 
			Context.revert("Only the game owner can turn it on.");
		}

		if (!this._game_on.get()) {
			this._game_on.set(true);
			this._day.set(BigInteger.valueOf(Context.getBlockTimestamp())); // U_SECONDS_DAY)
		}
	}

	/*
    Turns off the game. Only owner can turn off the game
    :return:
	 */
	@External
	public void game_off() {
		if ( !Context.getCaller().equals(Context.getOwner())) {
			Context.revert("Only the score owner can turn it off");
		}
		if (this._game_on.get()) {
			this._game_on.set(false);
		}
	}

	/*
    Returns the status of the game.
    :return: Status of game
    :rtype: bool
	 */
	@External(readonly=true)
	public Boolean get_game_on_status() {
		return this._game_on.get();
	}

	/*
    Returns the multipliers of different bet types
    :return: Multipliers of different bet types
    :rtype: str
	 */
	@External(readonly=true)
	public String get_multipliers() {
		return mapToJsonString(MULTIPLIERS);
	}

	public static <K,V> String mapToJsonString(Map<K, V > map) {
		StringBuilder sb = new StringBuilder("{");
		for (Map.Entry<K, V> entry : map.entrySet()) {
			sb.append("\""+entry.getKey()+"\":\""+entry.getValue()+"\",");
		}
		char c = sb.charAt(sb.length()-1);
		if(c == ',') {
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("}");
		String json = sb.toString();
		Context.println(json);
		return json;
	}
}
