package com.iconbet.score.daolette.game;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

public class DaoletteGame {

	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "DAOLETTE";

	private static final int[] BET_LIMIT_RATIOS = new int[] {147, 2675, 4315, 2725, 1930, 1454, 1136, 908, 738, 606,
			500, 413, 341, 280, 227, 182, 142, 107, 76, 48, 23};

	private static final BigInteger BET_MIN = new BigInteger("100000000000000000");  // 1.0E+17, .1 ICX

	private static final String[] BET_TYPES = new String[] {"none", "bet_on_numbers", "bet_on_color", "bet_on_even_odd", "bet_on_number", "number_factor"};

	private static final List<Integer> WHEEL_ORDER = List.of(2, 20, 3, 17, 6, 16, 7, 13, 10, 12,
			11, 9, 14, 8, 15, 5, 18, 4, 19, 1, 0);

	private static final List<Integer> WHEEL_BLACK = List.of(2,3,6,7,10,11,14,15,18,19);

	private static final List<Integer> SET_BLACK = List.of(2, 3, 6, 7, 10, 11, 14, 15, 18, 19);

	private static final List<Integer> WHEEL_RED  = List.of(1,4,5,8,9,12,13,16,17,20);

	private static final List<Integer> SET_RED = List.of( 1, 4, 5, 8, 9, 12, 13, 16, 17, 20);

	private static final List<Integer> WHEEL_ODD = List.of(1,3,5,7,9,11,13,15,17,19);

	private static final List<Integer> SET_ODD = List.of( 1, 3, 5, 7, 9, 11, 13, 15, 17, 19);

	private static final List<Integer> WHEEL_EVEN = List.of(2,4,6,8,10,12,14,16,18,20);

	private static final List<Integer> SET_EVEN = List.of( 2, 4, 6, 8, 10, 12, 14, 16, 18, 20);

	private static final Map<String, Number> MULTIPLIERS = Map.of(
			"bet_on_color", 2,
			"bet_on_even_odd", 2,
			"bet_on_number", 20,
			"number_factor", 20.685f);

	private String _GAME_ON = "game_on";
	private String _TREASURY_SCORE="treasury_score";

	private VarDB<Boolean> _game_on = Context.newVarDB(this._GAME_ON, Boolean.class);
	private VarDB<Address> _treasury_score = Context.newVarDB(this._TREASURY_SCORE, Address.class);

	public DaoletteGame(@Optional boolean _on_update_var) {
		if(_on_update_var) {
			Context.println("updating contract only");
			onUpdate();
			return;
		}
		Context.println("In __init__."+ TAG);
		Context.println("owner is "+ Context.getOwner() + ". "+ TAG);
		this._game_on.set(false);

	}

	public void onUpdate() {
		Context.println("calling on update. "+TAG);
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
			Context.println("setting treasury score address");
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
			Context.println("setting tresury game as on");
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
	public void bet_on_numbers(String numbers, @Optional String user_seed) {

		if(user_seed == null) {
			user_seed = "";
		}

		String[] array = StringUtils.split(numbers, ',');
		List<Integer> numList = List.of(mapToInt(array));

		if (numList.equals(SET_RED) || numList.equals(SET_BLACK)) {
			this.__bet(numList, user_seed, BET_TYPES[2]);
		}else if (numList.equals(SET_ODD) || numList.equals(SET_EVEN)) {
			this.__bet(numList, user_seed, BET_TYPES[3]);
		}else {
			this.__bet(numList, user_seed, BET_TYPES[1]);
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
	public void bet_on_color(boolean color, @Optional String user_seed) {

		if(user_seed == null) {
			user_seed = "";
		}

		List<Integer> numbers;
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
	public void bet_on_even_odd(boolean even_odd, @Optional String user_seed) {

		if(user_seed == null) {
			user_seed = "";
		}

		List<Integer> numbers;
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

		if(userSeed == null) {
			userSeed = "";
		}

		Context.println("Entered get_random. "+ TAG);
		if ( Context.getCaller().isContract() ) {
			Context.revert("ICONbet: SCORE cant play games");
		}
		double spin;
		String seed = encodeHexString(Context.getTransactionHash()) + String.valueOf(Context.getBlockTimestamp()) + userSeed;
		//TODO: we can not do this in java, there is no way to access to the memory address from the icon-jvm-jdk.
		//validate if the result is same as python
		//( ByteBuffer.wrap(Context.hash("sha3-256", seed.getBytes())).order(ByteOrder.BIG_ENDIAN).getInt() % 100000) / 100000.0;
		spin = fromByteArray( Context.hash("sha3-256", seed.getBytes())) % 100000 / 100000.0;
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
	@SuppressWarnings("rawtypes")
	public void __bet(List<Integer> numbers, String user_seed, String bet_type) {

		this.BetSource(Context.getOrigin(), BigInteger.valueOf(Context.getTransactionTimestamp()));

		String numberStr = listToListString(numbers);

		if (!this._game_on.get()) {
			Context.println("Game not active yet. "+ TAG);
			Context.revert("Game not active yet.");
		}
		BigInteger amount = Context.getValue();
		Context.println("Betting "+ amount +" loop on " + numberStr +". "+ TAG);
		this.BetPlaced(amount, numberStr);

		Context.call(Context.getValue(), this._treasury_score.get(),  "send_wager", amount);

		if (numbers.isEmpty()) {
			Context.println("Bet placed without numbers. "+ TAG);
			Context.revert("Invalid bet. No numbers submitted. Zero win chance. Returning funds.");
		}else if (numbers.size() > 20) {
			Context.println("Bet placed with too many numbers. Max numbers = 20. "+ TAG);
			Context.revert("Invalid bet. Too many numbers submitted. Returning funds.");
		}

		List numList = List.of(WHEEL_ORDER.toArray());
		numList = ArrayUtils.removeElement(numList,0);

		for (Integer num :numbers) {
			if  ( !numList.contains(num) ) {
				Context.println("Invalid number "+ num +"submitted. "+ TAG);
				Context.revert("Please check your bet. Numbers must be between 0 and 20, submitted as a comma separated string. Returning funds.");
			}
		}

		BigInteger treasuryMin = Context.call(BigInteger.class, this._treasury_score.get(),  "get_treasury_min");

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
			payout = BigInteger.valueOf( (int)(MULTIPLIERS.get(BET_TYPES[5]).floatValue() * 1000) ).multiply(amount).divide(BigInteger.valueOf(1000l * numbers.size()));
		}else {
			payout = BigInteger.valueOf( MULTIPLIERS.get(bet_type).longValue()).multiply(amount);
		}

		if ( Context.getBalance(this._treasury_score.get()).compareTo(payout) < 0) {
			Context.println("Not enough in treasury to make the play. " + payout+ TAG);
			Context.revert("Not enough in treasury to make the play.");
		}

		double spin = this.get_random(user_seed);
		Integer winningNumber = WHEEL_ORDER.get((int)(spin * 21));
		Context.println("winningNumber was "+winningNumber+". "+ TAG);
		int win = numbers.contains(winningNumber)? 1: 0;
		Context.println("winner number in selected numbers? "+win +". "+ TAG);
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
			if(entry.getValue() instanceof Number) {
				sb.append("\""+entry.getKey()+"\":"+entry.getValue()+",");
			}else {
				sb.append("\""+entry.getKey()+"\":\""+entry.getValue()+"\",");
			}
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

	public Integer[] mapToInt(String[] array) {
		Integer[] intArr = new Integer[array.length];

		for(int i = 0; i < array.length; i++) {
			intArr[i] = Integer.valueOf(array[i]);
		}
		return intArr;
	}

	public String listToListString(List<Integer> list) {
		StringBuilder sb = new StringBuilder();
		for(Integer i: list) {
			sb.append(i.toString());
			sb.append(",");
		}
		char c = sb.charAt(sb.length()-1);
		if(c == ',') {
			sb.deleteCharAt(sb.length()-1);
		}
		return sb.toString();

	}

	int fromByteArray(byte[] bytes) {
	     int order = ((bytes[0] & 0xFF) << 24) | 
	            ((bytes[1] & 0xFF) << 16) | 
	            ((bytes[2] & 0xFF) << 8 ) | 
	            ((bytes[3] & 0xFF) << 0 );
	     //TODO: this cand be negative, why???
	     if(order < 0) {
	    	 return order * -1;
	     }
	     return order;
	}
}
