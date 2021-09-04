package com.iconbet.score.authorization;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;

import com.iconloop.score.token.irc2.IRC2;


public class Authorization implements IRC2{
	
	
	public Authorization() {
		BigInteger now = new BigInteger(String.valueOf(System.currentTimeMillis()));		
		day.set(now.divide(U_SECONDS_DAY));
	}
	
	
	private static final String TAG = "AUTHORIZATION";
	private static final Boolean DEBUG = Boolean.FALSE;
	private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");
	private static final BigInteger U_SECONDS_DAY = new BigInteger("86400000000"); // Microseconds in a day.
			
			
	private static List<String> METADATA_FIELDS = Arrays.asList("name", "scoreAddress", "minBet", "maxBet", "houseEdge",
	                                                      "gameType", "revShareMetadata", "revShareWalletAddress",
	                                                      "linkProofPage", "gameUrlMainnet", "gameUrlTestnet");
	
	private static List<String> GAME_TYPE = Arrays.asList("Per wager settlement", "Game defined interval settlement");
	
	private static List<String> STATUS_TYPE = Arrays.asList("waiting", "proposalApproved", "proposalRejected", "gameReady",
                   "gameApproved", "gameRejected", "gameSuspended", "gameDeleted");

    private static final String ADMIN_LIST = "admin_list";
    private static final String SUPER_ADMIN = "super_admin";
    private static final String PROPOSAL_DATA = "proposal_data";
    private static final String PROPOSAL_LIST = "proposal_list";
    private static final String STATUS_DATA = "status_data";
    private static final String OWNER_DATA = "owner_data";
    private static final String ROULETTE_SCORE = "roulette_score";
    private static final String DAY = "day";
    private static final String PAYOUTS = "payouts";
    private static final String WAGERS = "wagers";
    private static final String NEW_DIV_CHANGING_TIME = "new_div_changing_time";
    private static final String GAME_DEVELOPERS_SHARE = "game_developers_share";
    
    private static final String TODAYS_GAMES_EXCESS = "todays_games_excess";
    // dividends paid according to this excess
    private static final String GAMES_EXCESS_HISTORY = "games_excess_history";
    		

    private static final String APPLY_WATCH_DOG_METHOD = "apply_watch_dog_method";
    private static final String MAXIMUM_PAYOUTS = "maximum_payouts";
    private static final String MAXIMUM_LOSS = "maximum_loss";
        

	private final VarDB<Address> roulette_score = Context.newVarDB(ROULETTE_SCORE, Address.class);
	private final ArrayDB<Address> admin_list = Context.newArrayDB(ADMIN_LIST, Address.class);
	private final VarDB<Address> super_admin = Context.newVarDB(SUPER_ADMIN, Address.class);
	//// why Address? ////
	private final DictDB<Address,String> proposal_data = Context.newDictDB(PROPOSAL_DATA, String.class);
	private final DictDB<Address,String> status_data = Context.newDictDB(STATUS_DATA, String.class);
	private final DictDB<Address,String> owner_data = Context.newDictDB(OWNER_DATA, String.class);
	private final ArrayDB<Address> proposal_list = Context.newArrayDB(PROPOSAL_LIST, Address.class);
	private final VarDB<BigInteger> day = Context.newVarDB(DAY, BigInteger.class);
	private final DictDB<Address, BigInteger[]> wagers = Context.newDictDB(WAGERS, BigInteger[].class);
	private final DictDB<Address, BigInteger[]> payouts = Context.newDictDB(PAYOUTS, BigInteger[].class);
	
	private final VarDB<BigInteger> game_developers_share = Context.newVarDB(GAME_DEVELOPERS_SHARE, BigInteger.class);
	private final DictDB<Address,BigInteger> todays_games_excess = Context.newDictDB(TODAYS_GAMES_EXCESS, BigInteger.class);
	
	private final VarDB<BigInteger> new_div_changing_time = Context.newVarDB(NEW_DIV_CHANGING_TIME, BigInteger.class);
	private final DictDB<Address, BigInteger[]> games_excess_history = Context.newDictDB(TODAYS_GAMES_EXCESS, BigInteger[].class);

	private final VarDB<Boolean> apply_watch_dog_method = Context.newVarDB(APPLY_WATCH_DOG_METHOD, Boolean.class);
	private final DictDB<Address,BigInteger> maximum_payouts = Context.newDictDB(MAXIMUM_PAYOUTS, BigInteger.class);
	private final VarDB<BigInteger> maximum_loss = Context.newVarDB(MAXIMUM_LOSS, BigInteger.class);

	  

	@Override
	public String name() {
		return null;
	}
	@Override
	public String symbol() {
		return null;
	}
	@Override
	public int decimals() {
		return 0;
	}
	@Override
	public BigInteger totalSupply() {
		return null;
	}
	@Override
	public BigInteger balanceOf(Address _owner) {
		return null;
	}
	
	@Override
	@EventLog(indexed=2)
	public void transfer(Address to, BigInteger value, byte[] data) {}
	
	@Override
	@EventLog(indexed=2)
	public void Transfer(Address from, Address to, BigInteger value, byte[] data) {}
	
	@EventLog(indexed=2)
	public void ProposalSubmitted(Address sender, Address scoreAddress) {}
	
	@EventLog(indexed=1)
	public void GameSuspended(Address scoreAddress, String note) {}

	
	@External
	public void untether() {
		/*
        A function to redefine the value of self.owner once it is possible.
        To be included through an update if it is added to IconService.

        Sets the value of self.owner to the score holding the game treasury.
		 */
		//Context.getOrigin() - > txn.origin  - always wallet
		//Context.getCaller() - > sender
		//Context.getOrigin() - > owner
		if (!Context.getOrigin().equals(Context.getOwner()))
			Context.revert("Only the owner can call the untether method.");
	}
	
	
	@External
	public void set_new_div_changing_time(BigInteger timestamp) {
	/**
	  Sets the equivalent time of 00:00 UTC of dividend structure changing
	        date in microseconds timestamp.
	        :param _timestamp: Timestamp of 00:00 UTC of dividend structure changing
	                           date in microseconds timestamp
	        :type _timestamp: int
	        :return:  
	 **/
		
		if (Context.getCaller().equals(Context.getOwner())) {
			new_div_changing_time.set(timestamp);
			ArrayDB<Address> approved_games_list = get_approved_games();
			for (int i= 0; i< approved_games_list.size(); i++ ) {
				Address address = approved_games_list.get(i);
				//how to remove?
				//todays_games_excess			
			}
		}
	}
	
	@External
	public ArrayDB<Address> get_approved_games() {
	/**
	    Returns all the approved games' Address
        :return: List of approved games
        :rtype: list	
	 **/
		
		 ArrayDB<Address> _proposal_list = Context.newArrayDB(PROPOSAL_LIST, Address.class);
		 for (int i = 0; i< proposal_list.size();i++ ) {
			 Address address = proposal_list.get(i);
			 if (status_data.get(address).equals("gameApproved") ) {
				 _proposal_list.add(address);
			 }
		 }
		 return _proposal_list;
	}

}