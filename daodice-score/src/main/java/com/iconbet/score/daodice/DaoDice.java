package com.iconbet.score.daodice;

import java.math.BigDecimal;
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
	public static BigDecimal MAIN_BET_MULTIPLIER = BigDecimal.valueOf(98.5);
	public static BigDecimal SIDE_BET_MULTIPLIER = BigDecimal.valueOf(95);
	public static final BigInteger BET_MIN = new BigInteger("100000000000000000"); // 0.1 ICX = 10^18 * 0.1
	public static final BigDecimal FIVE = BigDecimal.valueOf(5);
	public static final BigInteger _1140 = BigInteger.valueOf(1140);
	public static final BigInteger _540 = BigInteger.valueOf(540);
	public static final BigInteger _12548 = BigInteger.valueOf(12548);
	public static final BigInteger _11 = BigInteger.valueOf(11);
	
	public static List<String> SIDE_BET_TYPES = List.of("digits_match", "icon_logo1", "icon_logo2");
	public static Map<String, BigDecimal> SIDE_BET_MULTIPLIERS = Map.of("digits_match",MAIN_BET_MULTIPLIER, "icon_logo1", FIVE, "icon_logo2", SIDE_BET_MULTIPLIER );
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
	public Boolean get_game_on() {
		return this._game_on.get();
	}
	
	
	/***
        Returns the side bet multipliers. Side bets are matching digits, single icon logo and double icon logo.
        :return: Side bet multipliers
        :rtype: dict
	***/
	@External(readonly = true)
	public Map<String, BigDecimal> get_side_bet_multipliers() {
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
	public BigDecimal get_random(String user_seed) {
		Context.println("Entered get_random. " + TAG);
		Address sender = Context.getCaller();
		if (sender.isContract()) {
			Context.revert("ICONbet: SCORE cant play games");
		}
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
		BigDecimal spin = BigDecimal.ZERO;
		
		//seed = (str(bytes.hex(self.tx.hash)) + str(self.now()) + user_seed)
		//spin = (int.from_bytes(sha3_256(seed.encode()), "big") % 100000) / 100000.0
		String msg = now.toString().concat(user_seed);
		byte[] bytes = Context.hash("", msg.getBytes());
		
		Context.println("Result of the spin was " + spin.toString() +" "+ TAG);
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

}
