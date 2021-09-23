package com.iconbet.score.daolette.game;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

public class DaoletteGame {

	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "DAOLETTE";

	private static final int[] BET_LIMIT_RATIOS = new int[] {147, 2675, 4315, 2725, 1930, 1454, 1136, 908, 738, 606,
			500, 413, 341, 280, 227, 182, 142, 107, 76, 48, 23};

	private static final BigInteger BET_MIN = new BigInteger("100000000000000000");  // 1.0E+17, .1 ICX

	private static final String[] BET_TYPES = new String[] {"none", "bet_on_numbers", "bet_on_color", "bet_on_even_odd", "bet_on_number", "number_factor"};

	private static final Set<Integer> WHEEL_ORDER = new HashSet<>(Arrays.asList(2, 20, 3, 17, 6, 16, 7, 13, 10, 12,
			11, 9, 14, 8, 15, 5, 18, 4, 19, 1, 0));

	private static final Set<Integer> WHEEL_BLACK = new HashSet<>(Arrays.asList(2,3,6,7,10,11,14,15,18,19));

	private static final Set<Integer> SET_BLACK = new HashSet<>(Arrays.asList( 2, 3, 6, 7, 10, 11, 14, 15, 18, 19));

	private static final Set<Integer> WHEEL_RED  = new HashSet<>( Arrays.asList(1,4,5,8,9,12,13,16,17,20));

	private static final Set<Integer> SET_RED = new HashSet<>(Arrays.asList( 1, 4, 5, 8, 9, 12, 13, 16, 17, 20));

	private static final Set<Integer> WHEEL_ODD = new HashSet<>( Arrays.asList(1,3,5,7,9,11,13,15,17,19));

	private static final Set<Integer> SET_ODD = new HashSet<>( Arrays.asList( 1, 3, 5, 7, 9, 11, 13, 15, 17, 19));

	private static final Set<Integer> WHEEL_EVEN = new HashSet<>( Arrays.asList(2,4,6,8,10,12,14,16,18,20));

	private static final Set<Integer> SET_EVEN = new HashSet<>( Arrays.asList( 2, 4, 6, 8, 10, 12, 14, 16, 18, 20));

	private static final Map<String, Float> MULTIPLIERS = Map.of(
			"bet_on_color", 2f,
			"bet_on_even_odd", 2f,
			"bet_on_number", 20f,
			"number_factor", 20.685f);

	private String _GAME_ON = "game_on";
	private String _TREASURY_SCORE="treasury_score";

	private  VarDB<Boolean> _game_on = Context.newVarDB(this._GAME_ON, Boolean.class);
	private VarDB<Address> _treasury_score = Context.newVarDB(this._TREASURY_SCORE, Address.class);

	public DaoletteGame() {
		Context.println("In __init__."+ TAG);
		Context.println("owner is "+ Context.getOwner() + ". "+ TAG);
		this._game_on.set(false);
	}

	@EventLog(indexed=2)
	public void BetSource(Address v, BigInteger timestamp) {}

	@EventLog(indexed=2)
	public void BetPlaced(BigInteger amount, String numbers) {}

	@EventLog(indexed=2)
	public void BetResult(String spin, String winningNumber, BigInteger payout) {}

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
    Sets the treasury score address. The function can only be invoked by score owner.
    :param _score: Score address of the treasury
    :type _score: :class:`iconservice.base.address.Address`
	 */
	@External
	public void set_treasury_score(Address _score) {
		if ( Context.getCaller().equals(Context.getOwner())) {
			this._treasury_score.set(_score);
		}
	}

	/*
    Returns the treasury score address.
    :return: Address of the treasury score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_treasury_score() {
		return this._treasury_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Set the status of game as on. Only the owner of the game can call this method. Owner must have set the
    treasury score before changing the game status as on.
	 */
	@External
	public void game_on() {

		if ( !Context.getCaller().equals(Context.getOwner())){ 
			Context.revert("Only the owner can call the game_on method");
		}
		if (!this._game_on.get() && this._treasury_score.get() != null){
			this._game_on.set(true);
		}
	}

	/*
    Set the status of game as off. Only the owner of the game can call this method.
	 */
	@External
	public void game_off() {
		if ( !Context.getCaller().equals(Context.getOwner())){ 
			Context.revert("Only the owner can call the game_off method");
		}
		if (this._game_on.get()) {
			this._game_on.set(false);
		}
	}

	/*
    Returns the current game status
    :return: Current game status
    :rtype: bool
	 */
	@External(readonly=true)
	public boolean get_game_on() {
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
    Returns the bet limit for the number of selected numbers
    :param n: No. of selected numbers
    :return: Bet limit in loop
	 */
	@External(readonly=true)
	public BigInteger get_bet_limit(BigInteger n) {

		BigInteger treasuryMin = Context.call(BigInteger.class, this._treasury_score.get(),  "get_treasury_min");

		return treasuryMin.divide(BigInteger.valueOf(BET_LIMIT_RATIOS[n.intValue()]));
	}

	/*
    Takes a list of numbers in the form of a comma separated string. e.g. "1,2,3,4" and user seed
    :param numbers: Numbers selected
    :type numbers: str
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
	 */
	@External
	@Payable
	public void bet_on_numbers(String numbers, String user_seed) {
		List<Integer> list = Stream.of(numbers.split(",")).mapToInt(n -> Integer.valueOf(n)).boxed().collect(Collectors.toList());
		Set<Integer> numSet = Set.of(list.toArray(new Integer[list.size()]));

		if (numSet.equals(SET_RED) || numSet.equals(SET_BLACK)) {
			this.__bet(numSet, user_seed, BET_TYPES[2]);
		}else if (numSet.equals(SET_ODD) || numSet.equals(SET_EVEN)) {
			this.__bet(numSet, user_seed, BET_TYPES[3]);
		}else {
			this.__bet(numSet, user_seed, BET_TYPES[1]);
		}
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
	public void bet_on_color(boolean color, String user_seed) {
		Set<Integer> numbers;
		if (color) {
			numbers = WHEEL_RED;
		}else {
			numbers = WHEEL_BLACK;
		}
		this.__bet(numbers, user_seed, BET_TYPES[2]);
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
	public void bet_on_even_odd(boolean even_odd, String user_seed) {
		Set<Integer> numbers;
		if (even_odd) {
			numbers = WHEEL_ODD;
		}else {
			numbers = WHEEL_EVEN;
		}
		this.__bet(numbers, user_seed, BET_TYPES[3]);
	}

	/*
    A function to redefine the value of self.owner once it is possible.
    To be included through an update if it is added to IconService.
    Sets the value of self.owner to the score holding the game treasury.
	 */
	@External
	public void untether() {
		if ( !Context.getCaller().equals(Context.getOwner())){
			Context.revert("Only the owner can call the untether method.");
		}
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
		if ( Context.getCaller().isContract() ) {
			Context.revert("ICONbet: SCORE cant play games");
		}
		double spin;
		String seed = encodeHexString(Context.getTransactionHash()) + String.valueOf(Context.getBlockTimestamp()) + userSeed;
		spin = ( ByteBuffer.wrap(Context.hash("sha3-256", seed.getBytes())).order(ByteOrder.BIG_ENDIAN).getInt() % 100000) / 100000.0;
		Context.println("Result of the spin was "+ spin + " "+ TAG);
		return spin;
	}

	/*
    Takes a list of numbers in the form of a comma separated string and the user seed
    :param numbers: The numbers which are selected for the bet
    :type numbers: str
    :param user_seed: User seed/ Lucky phrase provided by user which is used in random number calculation
    :type user_seed: str
    :return:
	 */
	public void __bet(Set<Integer> numbers, String user_seed, String bet_type) {

		this.BetSource(Context.getOrigin(), BigInteger.valueOf(Context.getTransactionTimestamp()));

		BigInteger treasuryMin = Context.call(BigInteger.class, this._treasury_score.get(),  "get_treasury_min");

		String numberStr = numbers.stream().map(i-> i.toString()).collect(Collectors.joining(","));

		if (!this._game_on.get()) {
			Context.println("Game not active yet. "+ TAG);
			Context.revert("Game not active yet.");
		}
		BigInteger amount = Context.getValue();
		Context.println("Betting "+ amount +" loop on " + numberStr +". "+ TAG);
		this.BetPlaced(amount, numberStr);

		//TODO: investigate what does this chain call means
		//treasury_score.icx(self.msg.value).send_wager(amount)
		Context.call(this._treasury_score.get(),  "send_wager", amount);

		if (numbers.isEmpty()) {
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
		BigInteger betLimit;
		if (bet_type.equals(BET_TYPES[2]) || bet_type.equals(BET_TYPES[3])){
			betLimit = treasuryMin.divide(BigInteger.valueOf(BET_LIMIT_RATIOS[0]));
		}else {
			betLimit = treasuryMin.divide(BigInteger.valueOf(BET_LIMIT_RATIOS[numbers.size()]));
		}

		if (amount.compareTo(BET_MIN) < 0
				|| amount.compareTo(betLimit) > 0) {
			Context.println("Betting amount "+amount +" out of range. "+ TAG);
			Context.revert("Betting amount "+amount+" out of range ("+BET_MIN+" -> "+betLimit+" loop).");
		}

		if (numbers.size() == 1) {
			bet_type = BET_TYPES[4];
		}

		BigInteger payout;
		if (bet_type.equals(BET_TYPES[1])){
			payout = BigInteger.valueOf( MULTIPLIERS.get(BET_TYPES[5]).longValue() * 1000 ).multiply(amount).divide(BigInteger.valueOf(100*numbers.size()));
		}else {
			payout = BigInteger.valueOf( MULTIPLIERS.get(bet_type).longValue()).multiply(amount);
		}

		if ( Context.getBalance(this._treasury_score.get()).compareTo(payout) < 0) {
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
			Context.println("Won "+TAG);
			Context.call(this._treasury_score.get(),  "wager_payout", payout);
		}else {
			Context.println("Player lost. ICX retained in treasury. "+ TAG);
		}
	}

	@Payable
	public void fallback() {
		Context.revert( Context.getAddress() + " This contract can't receive plain ICX");
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

	public <K,V> String mapToJsonString(Map<K, V > map) {
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
