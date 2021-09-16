package com.iconbet.score.daolette;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private static final BigInteger TX_MIN_BATCH_SIZE = BigInteger.valueOf(10);
	private static final BigInteger TX_MAX_BATCH_SIZE = BigInteger.valueOf(500);
	private static final BigInteger DIST_DURATION_PARAM = BigInteger.valueOf(50);  // Units of 1/Days

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

	private DictDB<String, String> _vote = Context.newDictDB(this._VOTE, String.class);
	private ArrayDB<Address> _voted = Context.newArrayDB(this._VOTED, Address.class);
	private VarDB<BigInteger> _yes_votes = Context.newVarDB(this._YES_VOTES, BigInteger.class);
	private VarDB<BigInteger> _no_votes = Context.newVarDB(this._NO_VOTES, BigInteger.class);
	private VarDB<Boolean> _open_treasury = Context.newVarDB(this._OPEN_TREASURY, Boolean.class);
	private VarDB<Address> _game_auth_score = Context.newVarDB(this._GAME_AUTH_SCORE, Address.class);

	/*TODO: not used*/
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
	public void BetSource(Address _from, BigInteger timestamp){}

	@EventLog(indexed=2)
	public void BetPlaced(BigInteger amount, String numbers){}

	@EventLog(indexed=2)
	public void BetResult(String spin, String winningNumber, BigInteger payout){}

	@EventLog(indexed=3)
	public void DayAdvance(BigInteger day, BigInteger skipped, BigInteger block_time, String note){}

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
		this._day.set(BigInteger.valueOf(Context.getTransactionTimestamp()).divide(U_SECONDS_DAY));
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
		for(int i = 0; i < BET_LIMIT_RATIOS.length; i++) {
			this._bet_limits.set(BigInteger.valueOf(i), this._treasury_min.get().divide( BigInteger.valueOf(BET_LIMIT_RATIOS[i]) ));
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
			this._day.set(BigInteger.valueOf(Context.getBlockTimestamp()).divide(U_SECONDS_DAY));
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

	/*
    Returns the reward pool of the ICONbet platform
    :return: Reward pool of the ICONbet platform
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_excess() {
		//TODO: this could be negative looks like, is it ok?
		BigInteger excessToMinTreasury = this._treasury_balance.getOrDefault(BigInteger.ZERO).subtract(this._treasury_min.get());

		if (! this._excess_smoothing_live.get()) {
			return excessToMinTreasury.subtract( Context.call(BigInteger.class, this._game_auth_score.get(),  "get_excess"));
		}else {
			BigInteger thirdPartyGamesExcess = BigInteger.ZERO;
			@SuppressWarnings("unchecked")
			Map<String, String> gamesExcess = (Map<String, String>)Context.call(this._game_auth_score.get(),"get_todays_games_excess");

			for (Map.Entry<String,String> gameExcess :gamesExcess.entrySet()) {
				thirdPartyGamesExcess = thirdPartyGamesExcess.add(
						BigInteger.ZERO.max( new BigInteger(gameExcess.getValue()) )
						);
			}
			return excessToMinTreasury.subtract( thirdPartyGamesExcess.multiply(BigInteger.valueOf(20)) ).divide(BigInteger.valueOf(100));
		}
	}

	/*
    Returns the total distributed amount from the platform
    :return: Total distributed excess amount
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_total_distributed() {
		return this._total_distributed.get();
	}

	/*
    Returns the total bets made till date
    :return: Total bets made till date
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_total_bets() {
		return this._total_bet_count.get().add(this._daily_bet_count.get());
	}

	/*
    Returns the total bets of current day
    :return: Total bets of current day
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_todays_bet_total() {
		return this._daily_bet_count.get();
	}

	/*
    Returns the treasury minimum value
    :return: Treasury minimum value
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_treasury_min() {
		return this._treasury_min.get();
	}

	/*
    Returns the bet limit for the number of selected numbers
    :param n: No. of selected numbers
    :type n: int
    :return: Bet limit in loop
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_bet_limit(BigInteger n) {
		return this._bet_limits.get(n);
	}

	/*
    Returns the vote results of dissolving the treasury.
    :return: Vote result for treasury to be dissolved e.g. [0,0]
    :rtype: str
	 */
	@External(readonly=true)
	public String get_vote_results() {
		return "["+this._yes_votes.get()+","+ this._no_votes.get()+"]";
	}

	/*
    A function to return the owner of this score.
    :return: Owner address of this score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_score_owner() {
		return Context.getOwner();
	}

	/*
    Returns the number of skipped days. Days are skipped if the distribution is not completed in any previous day.
    :return: Number of skipped days
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_skipped_days() {
		return this._skipped_days.get();
	}

	@External(readonly=true)
	public BigInteger get_yesterdays_excess() {
		return this._yesterdays_excess.getOrDefault(BigInteger.ZERO);
	}

	@External(readonly=true)
	public Address get_daofund_score() {
		return this._daofund_score.getOrDefault(ZERO_ADDRESS);
	}

	@External
	public void set_daofund_score(Address _score) {
		if ( ! Context.getCaller().equals(Context.getOwner())) {
			Context.revert("TREASURY: DAOfund address can only be set by owner");
		}
		if ( ! _score.isContract()) {
			Context.revert("TREASURY: Only contract address is accepted for DAOfund");
		}
		this._daofund_score.set(_score);
	}

	@External
	@Payable
	public void send_wager(BigInteger _amount) {
		if ( Context.getValue().compareTo(_amount) != 0) {
			Context.revert("ICX sent and the amount in the parameters are not same");
		}
		this._take_wager(Context.getCaller(),_amount);
	}

	@External
	@Payable
	public void send_rake(BigInteger _wager, BigInteger _payout) {
		if  ( Context.getValue().compareTo(_wager.subtract(_payout)) != 0 ) {
			Context.revert("ICX sent and the amount in the parameters are not same");
		}
		this.take_rake(_wager, _payout);
	}

	/*
    Takes wager amount from approved games. The wager amounts are recorded in game authorization score. Checks if
    the day has been advanced. If the day has advanced the excess amount is transferred to distribution contract.
    :param _amount: Wager amount to be recorded for excess calculation
    :return:
	 */
	@External
	public void take_wager(BigInteger _amount) {
		this._take_wager(Context.getCaller(), _amount);
	}

	/*
    Takes wager amount from approved games.
    :param _game_address: Address of the game
    :type _game_address: :class:`iconservice.base.address.Address`
    :param _amount: Wager amount
    :type _amount: int
    :return:
	 */
	public void _take_wager(Address _game_address, BigInteger _amount) {
		if (_amount.compareTo(BigInteger.ZERO) <= 0) {
			Context.revert("Invalid bet amount "+_amount);
		}

		String gameStatus = Context.call(String.class, this._game_auth_score.get(),  "get_game_status", _game_address);

		if ( !gameStatus.equals("gameApproved")){
			Context.revert("Bet only accepted through approved games.");
		}

		if (this.__day_advanced()) {
			this.__check_for_dividends();
		}
		this._daily_bet_count.set(this._daily_bet_count.get().add(BigInteger.ONE));

		Context.call(this._game_auth_score.get(),  "accumulate_daily_wagers", _game_address, _amount);

		Context.println("Sending wager data to rewards score."+ TAG);

		BigInteger days = this._day.get().subtract(this._skipped_days.get()).mod(BigInteger.TWO);
		Context.call(this._rewards_score.get(),  "accumulate_wagers", Context.getOrigin().toString(), _amount, days);

		this._treasury_balance.set(Context.getBalance(Context.getAddress()));
	}

	/*
    Takes wager amount and payout amount data from games which have their own treasury.
    :param _wager: Wager you want to record in GAS
    :param _payout: Payout you want to record
    :return:
	 */
	@External
	public void take_rake(BigInteger _wager, BigInteger _payout) {

		if (_payout.compareTo(BigInteger.ZERO) <= 0) {
			Context.revert("Payout can't be zero");
		}
		this._take_wager(Context.getCaller(), _wager);

		// dry run of wager_payout i.e. make payout without sending ICX
		String gameStatus = Context.call(String.class, this._game_auth_score.get(),  "get_game_status", Context.getCaller());

		if (! gameStatus.equals("gameApproved")) {
			Context.revert("Payouts can only be invoked by approved games.");
		}
		Context.call(this._game_auth_score.get(), "accumulate_daily_payouts", Context.getCaller(), _payout);

		this._treasury_balance.set(Context.getBalance(Context.getAddress()));
	}

	/*
    Makes payout to the player of the approved games. Only the approved games can request payout.
    :param _payout: Payout to be made to the player
    :return:
	 */
	@External
	public void wager_payout(BigInteger _payout) {
		this._wager_payout(Context.getCaller(), _payout);
	}

	/*
    Makes payout to the player of the approved games.
    :param _game_address: Address of the game requesting payout
    :type _game_address: :class:`iconservice.base.address.Address`
    :param _payout: Payout to be made to the player
    :type _payout: int
    :return:
	 */
	public void _wager_payout(Address _game_address, BigInteger _payout) {

		if (_payout.compareTo(BigInteger.ZERO) <= 0) {
			Context.revert("Invalid payout amount requested "+_payout);
		}

		String gameStatus = Context.call(String.class, this._game_auth_score.get(),  "get_game_status", _game_address);

		if ( !gameStatus.equals("gameApproved")){
			Context.revert("Payouts can only be invoked by approved games.");
		}

		boolean accumulated = Context.call(Boolean.class, this._game_auth_score.get(),  "accumulate_daily_payouts", _game_address, _payout);

		if (accumulated) {
			try {
				Context.println("Trying to send to ("+Context.getOrigin()+"): "+_payout+" . "+ TAG);
				Context.transfer(Context.getOrigin(), _payout);
				this.FundTransfer(Context.getOrigin(), _payout, "Player Winnings from "+ Context.getCaller()+".");
				Context.println("Sent winner ("+ Context.getOrigin()+") "+_payout+"."+ TAG);
			}catch(Exception  e) {
				Context.println("Send failed. Exception: "+e.getMessage()+ " "+ TAG);
				Context.revert("Network problem. Winnings not sent. Returning funds. Exception: "+ e.getMessage());
			}
			this._treasury_balance.set( Context.getBalance(Context.getAddress()) );
		}
	}

	/*
    Takes a list of numbers in the form of a comma separated string. e.g. "1,2,3,4" and user seed
    :param numbers: Numbers selected
    :type numbers: strnumset
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
	 */
	@External
	@Payable
	public void bet_on_numbers(String numbers, String user_seed) {

		//TODO: validate well-formed string
		List<Integer> list = Stream.of(numbers.split(",")).mapToInt(n -> Integer.valueOf(n)).boxed().collect(Collectors.toList());
		Set<Integer> numSet = Set.of(list.toArray(new Integer[list.size()]));

		if (numSet.equals(SET_RED) || numSet.equals(SET_BLACK)) {
			this._bet_type.set(BET_TYPES[2]);
		}else if (numSet.equals(SET_ODD) || numSet.equals(SET_EVEN)){
			this._bet_type.set(BET_TYPES[3]);
		}else {
			this._bet_type.set(BET_TYPES[1]);
		}
		this.__bet(numSet, user_seed);
	}

	/*
    The bet is set on either red color or black color.
    :param color: Red Color is chosen if true. Black if false
    :type color: blue
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
	 */
	@External
	@Payable
	public void bet_on_color(Boolean color, String user_seed) {
		this._bet_type.set(BET_TYPES[2]);
		Set<Integer> numbers;
		if (color) {
			numbers = WHEEL_RED;
		}else {
			numbers = WHEEL_BLACK;
		}
		this.__bet(numbers, user_seed);
	}

	/*
    The bet is set on either odd or even numbers.
    :param even_odd: Odd numbers is chosen if true. Even if false.
    :type even_odd: bool
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
	 */
	@External
	@Payable
	public void bet_on_even_odd(Boolean even_odd, String user_seed) {
		this._bet_type.set(BET_TYPES[3]);
		Set<Integer> numbers;
		if (even_odd) {
			numbers = WHEEL_ODD;
		}else {
			numbers = WHEEL_EVEN;
		}
		this.__bet(numbers, user_seed);
	}

	@External
	public void untether() {
		/*
        A function to redefine the value of self.owner once it is possible.
        To be included through an update if it is added to IconService.
        Sets the value of self.owner to the score holding the game treasury.
		 */
		if ( !Context.getCaller().equals(Context.getOwner())) {
			Context.revert("Only the owner can call the untether method.");
		}
	}

	/*
    Vote takes the votes from TAP holders to dissolve the treasury.
    :param option: Option to select for dissolving the treasury ("yes" | "no")
    :type option: str
    :return:
	 */
	@External
	public void vote(String option) {

		List<String> op = Arrays.asList("yes", "no");

		if (!op.contains(option)) {
			Context.revert("Option must be one of either \"yes\" or \"no\".");
		}

		Address address = Context.getOrigin();
		BigInteger balanceOwner = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", address);

		if ( !containsInArrayDb(address, this._voted)
				&& balanceOwner.equals(BigInteger.ZERO)) {
			Context.revert("You must either own or be a previous owner of TAP tokens in order to cast a vote.");
		}
		this._vote.set(address.toString(), option);
		if ( !containsInArrayDb(address, this._voted)) {
			this._voted.add(address);
			String message = "Recorded vote of "+ address.toString();
			this.Vote(Context.getCaller(), option, message);
		}else {
			String message = address.toString() + " updated vote to "+ option;
			this.Vote(address, option, message);
		}
		if ( !this.vote_result()) {
			String vote_msg = "Overall Vote remains a 'No'.";
			this.Vote(address, option, vote_msg);
		}else {
			// In case the votes is passed, treasury is dissolved by sending all the balance to distribution contract.
			// Distribution contract will then distribute 80% to tap holders and 20% to founders.
			this._open_treasury.set(true);
			this._excess_to_distribute.set( Context.getBalance(Context.getAddress()));
			this.__check_for_dividends();
			String vote_msg = "Vote passed! Treasury balance forwarded to distribution contract.";
			this.Vote(address, option, vote_msg);
			this._treasury_min.set(BigInteger.ZERO);
		}
	}

	/*
    Returns the vote result of vote on dissolving the treasury
    :return: True if majority of votes are yes
    :rtype: bool
	 */
	public boolean vote_result() {

		BigInteger yes = BigInteger.ONE;
		BigInteger no = BigInteger.ZERO;
		for (int i=0; i< this._voted.size(); i++){
			Address address = this._voted.get(i);
			String vote = this._vote.get( address.toString() );
			BigInteger balance = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", address);
			if (vote.equals("yes")){
				yes = yes.add(balance);
			}else {
				no = no.add(balance);
			}
		}
		this._yes_votes.set(yes);
		this._no_votes.set(no);
		BigInteger totalSupply = Context.call(BigInteger.class, this._token_score.get(), "totalSupply");
		BigInteger rewardsBalance = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", this._rewards_score.get());

		return this._yes_votes.get().compareTo(
				totalSupply.subtract(rewardsBalance).divide(BigInteger.TWO)
				) > 0;
	}

	/*
    Returns the batch size to be used for distribution according to the number of recipients. Minimum batch size is
    10 and maximum is 500.
    :param recip_count: Number of recipients
    :type recip_count: int
    :return: Batch size
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_batch_size(BigInteger recip_count) {
		Context.println("In get_batch_size."+ TAG);
		BigInteger yesterdaysCount = this._yesterdays_bet_count.get();
		if (yesterdaysCount.compareTo(BigInteger.ONE) < 0) {
			yesterdaysCount = BigInteger.ONE;
		}
		BigInteger size = DIST_DURATION_PARAM.multiply(recip_count).divide(yesterdaysCount);
		if (size.compareTo(TX_MIN_BATCH_SIZE) < 0) {
			size = TX_MIN_BATCH_SIZE;
		}
		if (size.compareTo(TX_MAX_BATCH_SIZE) > 0) {
			size = TX_MAX_BATCH_SIZE;
		}
		Context.println("Returning batch size of "+ size + " - "+ TAG);
		return size;
	}

	/*
    Generates a random # from tx hash, block timestamp and user provided
    seed. The block timestamp provides the source of unpredictability.
    :param user_seed: 'Lucky phrase' provided by user.
    :type user_seed: str
    :return: number from [x / 100000.0 for x in range(100000)] i.e. [0,0.99999]
    :rtype: float
	 */
	public double get_random(String userSeed) {
		Context.println("Entered get_random. "+ TAG);
		double spin = 0.0;
		try {
			String seed = encodeHexString(Context.getTransactionHash()) + String.valueOf(Context.getTransactionTimestamp()) + userSeed;
			spin = ( ByteBuffer.wrap(sha3_256(seed)).order(ByteOrder.BIG_ENDIAN).getInt() % 100000) / 100000.0;
		}catch (NoSuchAlgorithmException e) {
			Context.revert(e.getMessage());
			return spin;
		}
		Context.println("Result of the spin was "+ spin + "-"+ TAG);
		return spin;
	}

	/*
    Checks if day has been advanced nad the TAP distribution as well as dividends distribution has been completed.
    If the day has advanced and the distribution has completed then the current day is updated, excess is recorded
    from game authorization score, total bet count is updated and the daily bet count is reset.
    :return: True if day has advanced and distribution has been completed for previous day
    :rtype: bool
	 */
	@SuppressWarnings("unchecked")
	private boolean __day_advanced() {
		Context.println("In __day_advanced method." + TAG);
		BigInteger currentDay = BigInteger.valueOf(Context.getBlockTimestamp()).divide(U_SECONDS_DAY);
		BigInteger advance = currentDay.subtract(this._day.get());
		if (advance.compareTo(BigInteger.ONE) < 0) {
			return false;
		}else {

			Boolean rewardsComplete = Context.call(Boolean.class, this._rewards_score.get(), "rewards_dist_complete");
			Boolean dividendsComplete  = Context.call(Boolean.class, this._dividends_score.get(), "dividends_dist_complete");
			if (  !rewardsComplete || !dividendsComplete) {
				String rew = "";
				String div = "";
				if (!rewardsComplete) {
					rew = " Rewards dist is not complete";
				}
				if (!dividendsComplete) {
					div = " Dividends dist is not complete";
				}
				this._day.set(currentDay);
				this._skipped_days.set(this._skipped_days.get().add(advance));
				this.DayAdvance(this._day.get(), this._skipped_days.get(), BigInteger.valueOf(Context.getBlockTimestamp()),
						"Skipping a day since "+rew+ " " +div);
				return false;
			}
			// Set excess to distribute
			BigInteger excessToMinTreasury = this._treasury_balance.getOrDefault(BigInteger.ZERO).subtract(this._treasury_min.get());

			BigInteger developersExcess  = Context.call(BigInteger.class, this._game_auth_score.get(), "record_excess");
			this._excess_to_distribute.set(developersExcess.add(
					BigInteger.ZERO.max(
							excessToMinTreasury.subtract(developersExcess)
							)
					));

			if (this._excess_smoothing_live.get()) {
				BigInteger thirdPartyGamesExcess = BigInteger.ZERO;
				Map<String, String> gamesExcess = Context.call(Map.class, this._game_auth_score.get(), "get_yesterdays_games_excess");
				for (Map.Entry<String, String> game : gamesExcess.entrySet()) {
					thirdPartyGamesExcess = thirdPartyGamesExcess.add(
							BigInteger.ZERO.max( new BigInteger(game.getValue()) )
							);
				}
				BigInteger partnerDeveloper = thirdPartyGamesExcess.multiply( BigInteger.valueOf(20)).divide(BigInteger.valueOf(100));
				BigInteger rewardPool = BigInteger.ZERO.max(
						excessToMinTreasury.subtract(partnerDeveloper).multiply( BigInteger.valueOf(90) )
						).divide(BigInteger.valueOf(100));
				BigInteger daofund = BigInteger.ZERO.max(
						excessToMinTreasury.subtract(partnerDeveloper).multiply( BigInteger.valueOf(5))
						).divide(BigInteger.valueOf(100));
				this._excess_to_distribute.set(partnerDeveloper.add(rewardPool));
				this._yesterdays_excess.set(excessToMinTreasury.subtract(partnerDeveloper));
				this._daofund_to_distirbute.set(daofund);
			}

			if (advance.compareTo(BigInteger.ONE) > 0) {
				this._skipped_days.set(this._skipped_days.get().add(advance).subtract(BigInteger.ONE));
			}

			this._day.set(currentDay);
			this._total_bet_count.set(this._total_bet_count.get().add(this._daily_bet_count.get()));
			this._yesterdays_bet_count.set(this._daily_bet_count.get());
			this._daily_bet_count.set(BigInteger.ZERO);
			this.DayAdvance(this._day.get(), this._skipped_days.get(), BigInteger.valueOf(Context.getBlockTimestamp()), "Day advanced. Counts reset.");
			return true;
		}
	}

	/*
    If there is excess in the treasury, transfers to the distribution contract.
    :return:
	 */
	public void __check_for_dividends() {
		BigInteger excess = this._excess_to_distribute.getOrDefault(BigInteger.ZERO);
		BigInteger daofund = this._daofund_to_distirbute.getOrDefault(BigInteger.ZERO);

		Context.println("Found treasury excess of "+excess + ". "+ TAG);
		if (excess.compareTo(BigInteger.ZERO) > 0) {
			try {
				Context.println("Trying to send to ("+this._dividends_score.get()+"): "+excess+". "+ TAG);
				Context.transfer(this._dividends_score.get(), excess);
				this.FundTransfer(this._dividends_score.get(), excess, "Excess made by games");
				Context.println("Sent div score ("+this._dividends_score.get()+") "+excess +". "+ TAG);
				this._total_distributed.set(this._total_distributed.get().add(excess));
				this._excess_to_distribute.set(BigInteger.ZERO);
			}catch (Exception e) {
				Context.println("Send failed. Exception: "+e.getMessage()+ " "+ TAG);
				Context.revert("Network problem. Excess not sent. Exception:" +e.getMessage());
			}
		}

		if (daofund.compareTo(BigInteger.ZERO) > 0) {
			try {
				this._daofund_to_distirbute.set(BigInteger.ZERO);
				Context.transfer(this._daofund_score.get(), daofund);
				this.FundTransfer(this._daofund_score.get(), daofund, "Excess transerred to daofund");
			}catch (Exception e) {
				Context.revert("Network problem. DAOfund not sent. Exception: "+e.getMessage());
			}
		}

	}

	/*
    Takes a list of numbers in the form of a comma separated string and the user seed
    :param numbers: The numbers which are selected for the bet
    :type numbers: str
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
	 */
	public void __bet(Set<Integer> numbers, String user_seed) {
		this.BetSource(Context.getOrigin(), BigInteger.valueOf(Context.getBlockTimestamp()));
		if (!this._game_on.get()) {
			Context.println("Game not active yet. "+ TAG);
			Context.revert("Game not active yet.");
		}
		BigInteger amount = Context.getValue();
		Context.println("Betting "+amount+" loop on "+numbers+". " +TAG);
		this.BetPlaced(amount, setToStringEnumerated(numbers));
		this._take_wager(Context.getAddress(), amount);

		if (numbers.size() == 0) {
			Context.println("Bet placed without numbers. "+ TAG);
			Context.revert("Invalid bet. No numbers submitted. Zero win chance. Returning funds.");
		}else if (numbers.size() > 20) {
			Context.println("Bet placed with too many numbers. Max numbers = 20. "+ TAG);
			Context.revert("Invalid bet. Too many numbers submitted. Returning funds.");
		}

		Set<Integer> numset = new HashSet<>(WHEEL_ORDER);
		numset.remove(0);
		for (Integer num :numbers) {
			if  ( !numset.contains(num) ) {
				Context.println("Invalid number submitted. "+ TAG);
				Context.revert("Please check your bet. Numbers must be between 0 and 20, submitted as a comma separated string. Returning funds.");
			}
		}
		String betType = this._bet_type.get();
		this._bet_type.set(BET_TYPES[0]);
		BigInteger betLimit;
		if (betType.equals(BET_TYPES[2]) ||
				betType.equals(BET_TYPES[3])) {
			betLimit = this._bet_limits.get(BigInteger.ZERO);
		}else {
			betLimit = this._bet_limits.get(BigInteger.valueOf(numbers.size()));
		}
		if (amount.compareTo(BET_MIN) < 0
				|| amount.compareTo(betLimit) > 0) {
			Context.println("Betting amount "+amount +" out of range. "+ TAG);
			Context.revert("Betting amount "+amount+" out of range ("+BET_MIN+" -> "+betLimit+" loop).");
		}
		if (numbers.size() == 1) {
			betType = BET_TYPES[4];
		}
		BigInteger payout;
		if (betType.equals(BET_TYPES[1])){
			payout = BigInteger.valueOf( MULTIPLIERS.get(BET_TYPES[5]).longValue() * 1000 ).multiply(amount).divide(BigInteger.valueOf(100));
		}else {
			payout = BigInteger.valueOf( MULTIPLIERS.get(betType).longValue()).multiply(amount);
		}

		if ( Context.getBalance(Context.getAddress()).compareTo(payout) < 0) {
			Context.println("Not enough in treasury to make the play. "+ TAG);
			Context.revert("Not enough in treasury to make the play.");
		}

		double spin = this.get_random(user_seed);
		Integer winningNumber = WHEEL_ORDER.stream().filter(i-> i == (int)(spin * 21) ).findFirst().orElse(0);
		Context.println("winningNumber was "+winningNumber+". "+ TAG);
		int win = numbers.stream().filter(i-> i.equals(winningNumber)).findFirst().orElse(0);
		Context.println("win value was "+win +". "+ TAG);
		payout = payout.multiply(BigInteger.valueOf(win));
		this.BetResult(String.valueOf(spin), String.valueOf(winningNumber), payout);

		if (win == 1) {
			this._wager_payout(Context.getAddress(), payout);
		}else {
			Context.println("Player lost. ICX retained in treasury. "+ TAG);
		}
	}

	/*
    Users can add to excess, excess added by this method will be only shared to tap holders and wager wars
    :return:
	 */
	@Payable
	@External
	public void add_to_excess() {
		if (Context.getValue().compareTo(BigInteger.ZERO) <= 0) {
			Context.revert("No amount added to excess");
		}
		this._treasury_balance.set( Context.getBalance(Context.getAddress()) );
		this.FundReceived(Context.getCaller(), Context.getValue(), Context.getValue()+ " added to excess");
	}

	@Payable
	public void fallback() {

		String gameStatus = Context.call(String.class, this._game_auth_score.get(),  "get_game_status", Context.getCaller());
		if ( !gameStatus.equals("gameApproved")) {
			Context.revert(
					"This score accepts plain ICX through approved games and through set_treasury, add_to_excess method.");
		}
	}

	@Payable
	@External
	public void transfer_to_dividends() {
		if ( !Context.getCaller().equals(Context.getOwner())) {
			Context.revert(TAG + ": Only owner can transfer the amount to dividends contract.");
		}
		Context.transfer(this._dividends_score.get(), Context.getValue());
	}

	public boolean getNewDivLive() {
		return this._new_div_live.getOrDefault(false);
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

	private <T> boolean containsInArrayDb(T value, ArrayDB<T> arraydb) {
		boolean found = false;
		if(arraydb == null || value == null) {
			return found;
		}

		for(int i = 0; i< arraydb.size(); i++) {
			if(arraydb.get(i) != null
					&& arraydb.get(i).equals(value)) {
				found = true;
				break;
			}
		}
		return found;
	}

	public byte[] sha3_256(String value) throws NoSuchAlgorithmException {

		final MessageDigest digest = MessageDigest.getInstance("SHA3-256");
		return  digest.digest(
				value.getBytes(StandardCharsets.UTF_8));

	}

	public String encodeHexString(byte[] byteArray) {
		StringBuffer hexStringBuffer = new StringBuffer();
		for (int i = 0; i < byteArray.length; i++) {
			hexStringBuffer.append(byteToHex(byteArray[i]));
		}
		return hexStringBuffer.toString();
	}

	public String byteToHex(byte num) {
		char[] hexDigits = new char[2];
		hexDigits[0] = Character.forDigit((num >> 4) & 0xF, 16);
		hexDigits[1] = Character.forDigit((num & 0xF), 16);
		return new String(hexDigits);
	}

	public <T> String setToStringEnumerated(Set<T> set) {
		if(set == null || set.size() == 0) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		for (T entry : set) {
			sb.append(entry+",");
		}
		char c = sb.charAt(sb.length()-1);
		if(c == ',') {
			sb.deleteCharAt(sb.length()-1);
		}
		String list = sb.toString();
		Context.println(list);
		return list;
	}
}
