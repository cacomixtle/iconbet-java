package com.iconbet.score.daodice;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Payable;

public class DaoDice {

	public static final String TAG = "DICE";
	public static boolean DEBUG = Boolean.TRUE;
	public static BigInteger UPPER_LIMIT = BigInteger.valueOf(99);
	public static BigInteger LOWER_LIMIT = BigInteger.ZERO;
	public static Double MAIN_BET_MULTIPLIER = 98.5;
	public static Double SIDE_BET_MULTIPLIER = 95d;
	public static final BigInteger BET_MIN = new BigInteger("100000000000000000"); // 0.1 ICX = 10^18 * 0.1

	public static final Double FIVE = 5d;
	public static final BigInteger _1140 = BigInteger.valueOf(1140);
	public static final BigInteger _540 = BigInteger.valueOf(540);
	public static final BigInteger _12548 = BigInteger.valueOf(12548);
	public static final BigInteger _11 = BigInteger.valueOf(11);
	public static final BigInteger _99 = BigInteger.valueOf(99);
	public static final BigInteger _95 = BigInteger.valueOf(95);
	public static final BigInteger _100 = BigInteger.valueOf(100);
	public static final Double _100D = 100d;
	public static final Double _1_5D = 1.5;
	public static final Integer _68134 = 68134;
	public static final Double _681_34 = 681.34;

	
	
	
	public static List<String> SIDE_BET_TYPES = List.of("digits_match", "icon_logo1", "icon_logo2");
	public static Map<String, Double> SIDE_BET_MULTIPLIERS = Map.of("digits_match",MAIN_BET_MULTIPLIER, "icon_logo1", FIVE, "icon_logo2", SIDE_BET_MULTIPLIER );
	public static Map<String, BigInteger> BET_LIMIT_RATIOS_SIDE_BET = Map.of("digits_match", _1140, "icon_logo1", _540, "icon_logo2", _12548 );
	
	private final String _GAME_ON = "game_on";
	private final String _ROULETTE_SCORE = "roulette_score";
	
	
	private VarDB<Boolean> _game_on = Context.newVarDB(_GAME_ON, Boolean.class); 
	private VarDB<Address> _roulette_score = Context.newVarDB(_ROULETTE_SCORE, Address.class); 

	
	
	public DaoDice() {
		if (DEBUG) {
			Context.println("In __init__. "+ TAG);
			Address owner = Context.getOrigin();
			Context.println("owner is "+ owner.toString() +" "+TAG);		
		}
		this._game_on.set(Boolean.FALSE);
	}

	@EventLog(indexed=3) 
	public void BetPlaced(BigInteger amount, BigInteger upper, BigInteger lower) {}

	@EventLog(indexed=2)
	public void BetSource( Address _from, BigInteger timestamp) {}

	@EventLog(indexed=3)
	public void PayoutAmount(BigInteger payout, BigInteger main_bet_payout, BigInteger side_bet_payout) {}

	@EventLog(indexed=3)
	public void BetResult(String spin, BigInteger winningNumber, BigInteger payout) {}

	@EventLog(indexed=2)
	public void FundTransfer(Address recipient, BigInteger amount, String  note) {}

	/***
    def on_update(self) -> None:
        super().on_update()
	***/
	
	
	/***
    A function to return the owner of this score.
    :return: Owner address of this score
    :rtype: :class:`iconservice.base.address.Address`
    ***/
	@External(readonly = true)
	public Address get_score_owner() {
		return Context.getOrigin() ;
	}
	
	/***
    Sets the roulette score address. The function can only be invoked by score owner.
    :param _score: Score address of the roulette
    :type _score: :class:`iconservice.base.address.Address`
    ***/
	@External
	public void set_roulette_score( Address _score) {
		Address sender = Context.getCaller();
		Address owner = Context.getOrigin();
		if (sender.equals(owner)) {
			this._roulette_score.set(_score);
		}
	}
	
	/***
    Returns the roulette score address.
    :return: Address of the roulette score
    :rtype: :class:`iconservice.base.address.Address`
    ***/
	@External(readonly = true)
	public Address get_roulette_score() {
		return this._roulette_score.get();
	}

	
	/***
        Set the status of game as on. Only the owner of the game can call this method. Owner must have set the
        roulette score before changing the game status as on.
	***/
	@External
	public void game_on() {
		Address sender = Context.getCaller();
		Address owner = Context.getOrigin();
		if(!sender.equals(owner)) {
			Context.revert("Only the owner can call the game_on method");
		}
		if ( !this._game_on.get() && 
				this._roulette_score.get()!=null) {
			this._game_on.set(Boolean.TRUE);
		}
	}
	
	/****
        Set the status of game as off. Only the owner of the game can call this method.
	***/	
	@External
	public void game_off() {
		Address sender = Context.getCaller();
		Address owner = Context.getOrigin();
		if (!sender.equals(owner)) {
			Context.revert("Only the owner can call the game_on method");
		}
		if ( this._game_on.get()!= null && 
				this._game_on.get()) {
			this._game_on.set(Boolean.FALSE);
		}
	}
	
	
	/***
        Returns the current game status
        :return: Current game status
        :rtype: bool
	***/
	@External(readonly = true)
	public boolean get_game_on() {
		return this._game_on.get();
	}
	
	
	/***
        Returns the side bet multipliers. Side bets are matching digits, single icon logo and double icon logo.
        :return: Side bet multipliers
        :rtype: dict
	***/
	@External(readonly = true)
	public Map<String, Double> get_side_bet_multipliers() {
		return SIDE_BET_MULTIPLIERS;
	}
	
	/***
    A function to redefine the value of  self.owner once it is possible .
    To  be included through an update if it is added to ICONSERVICE
    Sets the value of self.owner to the score holding the game treasury
	 ***/
	@External
	public void untether() {

		//Context.getOrigin() - > txn.origin  - always wallet
		//Context.getCaller() - > sender
		//Context.getOrigin() - > owner
		if (!Context.getOrigin().equals(Context.getOwner()))
			Context.revert("Only the owner can call the untether method.");
	}

	
	/***	
	 Generates a random # from tx hash, block timestamp and user provided
	 seed. The block timestamp provides the source of unpredictability.
	 :param user_seed: 'Lucky phrase' provided by user, defaults to ""
	 :type user_seed: str,optional
	 :return: Number from [x / 100000.0 for x in range(100000)]
	 :rtype: float
	***/
	public double get_random(String user_seed) {
		Context.println("Entered get_random. " + TAG);
		Address sender = Context.getCaller();
		if (sender.isContract()) {
			Context.revert("ICONbet: SCORE cant play games");
		}
		
		String seed = encodeHexString(Context.getTransactionHash()) + String.valueOf(Context.getBlockTimestamp()) + user_seed;
		double spin = fromByteArray( Context.hash("sha3-256", seed.getBytes())) % 100000 / 100000.0;

		Context.println("Result of the spin was " + spin +" "+ TAG);
		return spin;
	}


	/***
        Main bet function. It takes the upper and lower number for bet. Upper and lower number must be in the range
        [0,99]. The difference between upper and lower can be in the range of [0,95].
        :param upper: Upper number user can bet. It must be in the range [0,99]
        :type upper: int
        :param lower: Lower number user can bet. It must be in the range [0,99]
        :type lower: int
        :param user_seed: 'Lucky phrase' provided by user, defaults to ""
        :type user_seed: str,optional
        :param side_bet_amount: Amount to be used for side bet from value sent to this function, defaults to 0
        :type side_bet_amount: int,optional
        :param side_bet_type: side bet types can be one of this ["digits_match", "icon_logo1","icon_logo2"], defaults to
         ""
        :type side_bet_type: str,optional
       ***/ 		
	@Payable    	
	@External
	public void call_bet(BigInteger upper, BigInteger lower, String user_seed, BigInteger side_bet_amount, String side_bet_type) {
		//question ?? return?  return self.__bet(upper, lower, user_seed, side_bet_amount, side_bet_type)
		__bet(upper, lower, user_seed, side_bet_amount, side_bet_type);
	}
	
	private void __bet(BigInteger upper, BigInteger lower, String user_seed, BigInteger side_bet_amount, String side_bet_type) {
		/***
		 * TODO 
		 */
		Boolean side_bet_win = Boolean.FALSE;
		Boolean side_bet_set = Boolean.FALSE;
		BigInteger side_bet_payout = BigInteger.ZERO;
		BigInteger side_bet_limit = BigInteger.ZERO;
		BigInteger main_bet_amount = BigInteger.ZERO;
		Boolean main_bet_win = Boolean.FALSE;
		
		BetSource(get_roulette_score(), side_bet_amount);
		
		BigInteger _treasury_min = Context.call(BigInteger.class,this._roulette_score.get(), "get_treasury_min");
		Context.transfer(this._roulette_score.get(), Context.getValue());
		FundTransfer(this._roulette_score.get(),  Context.getValue(), "Sending icx to Roulette");
		Context.call(Context.getValue(),this._roulette_score.get(), "take_wager");
		
		
		if (!this._game_on.get()) {
			Context.println("Game not active yet. "+TAG);
			Context.revert("Game not active yet.");
		}
		if(!((upper.compareTo(BigInteger.ZERO)>= 0 && upper.compareTo(_99)<= 0) && 
				(lower.compareTo(BigInteger.ZERO)>= 0 && lower.compareTo(_99)<= 0))){
			Context.println("Numbers placed with out of range numbers "+TAG);
			Context.revert("Invalid bet. Choose a number between 0 to 99");
		}
		
		BigInteger gapResult = upper.subtract(upper);
		if(!(BigInteger.ZERO.compareTo(gapResult)== -1 &&
				gapResult.compareTo(_95)== -1 ) ) {
			Context.println("Bet placed with illegal gap "+TAG);
			Context.revert("Invalid gap. Choose upper and lower values such that gap is between 0 to 95");
		}
		
		if (("".equals(side_bet_type) &&  BigInteger.ZERO.compareTo(side_bet_amount)!=0 ) ||
				((!"".equals(side_bet_type)) &&   BigInteger.ZERO.compareTo(side_bet_amount)==0 )) {
			Context.println("should set both side bet type as well as side bet amount "+TAG);
			Context.revert("should set both side bet type as well as side bet amount");
		}

		if(BigInteger.ZERO.compareTo(side_bet_amount)== -1) {
			Context.revert("Bet amount cannot be negative'");
		}
		
		if ( !"".equals(side_bet_type) && BigInteger.ZERO.compareTo(side_bet_amount)!= 0 ) {
			side_bet_set = Boolean.TRUE;
			if (!SIDE_BET_TYPES.contains(side_bet_type) ) {
				Context.println("Invalid side bet type "+TAG);
				Context.revert("Invalid side bet type.");		
			}
			side_bet_limit = _treasury_min.divide( BET_LIMIT_RATIOS_SIDE_BET.get(side_bet_type) );
			if ( BET_MIN.compareTo(side_bet_amount) == 1 || side_bet_amount.compareTo(side_bet_limit)== 1) {
				Context.println("Betting amount " + side_bet_amount.toString() +" out of range. "+TAG);
				Context.revert("Betting amount "+side_bet_amount.toString() +" out of range ("+BET_MIN.toString() +" ,"+side_bet_limit.toString()+").");		
			}
			side_bet_payout =  BigInteger.valueOf( (int) (SIDE_BET_MULTIPLIERS.get(side_bet_type) * _100D) )
					.multiply(side_bet_amount).divide(_100);
		}
		
		main_bet_amount = Context.getValue().subtract(side_bet_amount);
		BetPlaced(main_bet_amount, upper, lower);
		BigInteger gap = upper.subtract(lower).add(BigInteger.ONE);
		
		if(BigInteger.ZERO.compareTo(main_bet_amount) == 0) {
			Context.println("No main bet amount provided "+TAG);
			Context.revert("No main bet amount provided");				
		}
		
        // l = ( t * 1.5 * g) / [68134 - (681.34 * g)]  = t * {(1.5 * g) / [68134 - (681.34 * g)]}
		BigInteger main_bet_limit = _treasury_min.multiply( BigInteger.valueOf((long) ( 
				(_1_5D * gap.intValue()) / 
				(_68134 - (_681_34 * gap.intValue() ))
				)));

		if ( BET_MIN.compareTo(main_bet_amount)== 1 || main_bet_amount.compareTo(main_bet_limit) == 1) {
			Context.println("Betting amount "+main_bet_amount.toString() +" out of range. "+TAG);
			Context.revert("Main Bet amount {"+main_bet_amount.toString() +"} out of range {"+BET_MIN.toString()+"},{"+main_bet_limit.toString()+"}");
		}

		BigInteger main_bet_payoutResult = BigInteger.valueOf( (long)(MAIN_BET_MULTIPLIER * _100D) ).multiply(main_bet_amount);
		BigInteger main_bet_payout = main_bet_payoutResult.divide(_100.multiply(gap));

		BigInteger payout = side_bet_payout.add(main_bet_payout);
		BigInteger balance = Context.getBalance(this._roulette_score.get());
		if (balance.compareTo(payout) == -1) {
			Context.println("Not enough in treasury to make the play. "+TAG);
			Context.revert("Not enough in treasury to make the play.");
		}
		double spin = get_random(user_seed);
		BigInteger winningNumber = BigInteger.valueOf( (long)(spin * _100D));
		Context.println("winningNumber was {"+winningNumber.toString()+"}. "+TAG);
		
		if (lower.compareTo(winningNumber) <= 0 && 
				upper.compareTo(winningNumber)>= 0) {
			main_bet_win = Boolean.TRUE;
		}else {
			main_bet_win = Boolean.FALSE;
		}
		
		if (side_bet_set) {
			side_bet_win = check_side_bet_win(side_bet_type,winningNumber);
			if (!side_bet_win) {
				side_bet_payout = BigInteger.ZERO;
			}
		}

		main_bet_payout = main_bet_payout.multiply(main_bet_win?BigInteger.ONE:BigInteger.ZERO);
		payout = main_bet_payout.add(side_bet_payout);
		BetResult(String.valueOf(spin), winningNumber, payout);
		PayoutAmount(payout, main_bet_payout, side_bet_payout);

		if (main_bet_win || side_bet_win) {
			Context.println("Amount owed to winner: {"+payout.toString()+"}. "+TAG);			
			try {
				Context.println("Trying to send to ({"+Context.getOrigin().toString()+"}): {"+payout.toString()+"}. "+TAG);
				Context.call(Context.getOrigin(), "wager_payout", payout);
				Context.println("Sent winner ({"+Context.getOrigin().toString()+"}): {"+payout.toString()+"}. "+TAG);
				
			}catch(Exception e) {
				Context.println("Send failed. Exception: " + e +" "+TAG);
				Context.revert("Network problem. Winnings not sent. Returning funds.");

			}
		}else {
			Context.println("Player lost. ICX retained in treasury. "+TAG);
		}

	}
	
	
	/***
    # check for bet limits and side limits
        Checks the conditions for side bets are matched or not.
        :param side_bet_type: side bet types can be one of this ["digits_match", "icon_logo1","icon_logo2"], defaults to
        :type side_bet_type: str,optional
        :param winning_number: winning number returned by random function
        :type winning_number: int
        :return: Returns true or false based on the side bet type and the winning number
        :rtype: bool
	***/
	public Boolean check_side_bet_win(String side_bet_type, BigInteger winning_number) {
		
		if (SIDE_BET_TYPES.get(0).equals(side_bet_type)) { //# digits_match
			BigInteger mod =  winning_number.mod(_11);
			return (mod.compareTo(BigInteger.ZERO)== 0);
		}else if(SIDE_BET_TYPES.get(1).equals(side_bet_type)) { //for icon logo1 ie for numbers having 1 zero in it
			return (winning_number.toString().contains("0") ||
					(winning_number.compareTo(BigInteger.ONE)>= 0  &&
					winning_number.compareTo(BigInteger.TEN )<= 0 ));			
		}else if(SIDE_BET_TYPES.get(2).equals(side_bet_type) ) { //or icon logo2 ie for 0
			return (winning_number.compareTo(BigInteger.ZERO) == 0);
		}else {
			return Boolean.FALSE;			
		}

	}
	
	@Payable
	public void fallback() {}

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

	int fromByteArray(byte[] bytes) {
	     return ((bytes[0] & 0xFF) << 24) | 
	            ((bytes[1] & 0xFF) << 16) | 
	            ((bytes[2] & 0xFF) << 8 ) | 
	            ((bytes[3] & 0xFF) << 0 );
	}
}
