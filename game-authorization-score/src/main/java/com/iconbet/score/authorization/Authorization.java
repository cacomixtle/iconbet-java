package com.iconbet.score.authorization;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonArray;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonObject.Member;
import com.eclipsesource.json.JsonValue;

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
import score.annotation.Payable;

import com.iconloop.score.token.irc2.IRC2;


public class Authorization implements IRC2{
	
	
	public Authorization() {
		
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
		day.set(now.divide(U_SECONDS_DAY));
	}
	
	
	private static final String TAG = "AUTHORIZATION";
	private static final Boolean DEBUG = Boolean.FALSE;
	private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");
	private static final BigInteger U_SECONDS_DAY = new BigInteger("86400000000"); // Microseconds in a day.
			
	private static final BigInteger _1_ICX = new BigInteger("100000000000000000"); // 0.1 ICX = 10^18 * 0.1
			
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
	//  question=?  why adrress
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
			this.new_div_changing_time.set(timestamp);
			ArrayDB<Address> approved_games_list = get_approved_games();
			for (int i= 0; i< approved_games_list.size(); i++ ) {
				Address address = approved_games_list.get(i);
				//question=? how to remove?
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
		 for (int i = 0; i< this.proposal_list.size();i++ ) {
			 Address address = this.proposal_list.get(i);
			 if (this.status_data.get(address).equals("gameApproved") ) {
				 _proposal_list.add(address);
			 }
		 }
		 return _proposal_list;
	}

	@External
	public void set_roulette_score(Address scoreAddress) {
		/**
        Sets the address of roulette/game score
        :param _scoreAddress: Address of roulette
        :type _scoreAddress: :class:`iconservice.base.address.Address`
        :return:		 
		 * **/
		if (!Context.getCaller().equals(Context.getOwner())) {
			Context.revert("This function can only be called from the GAS owner.");
		}
		this.roulette_score.set(scoreAddress);
	}
	
	@External(readonly = true)
	public Address get_roulette_score() {
		/**
		Returns the roulette score address
        :return: Address of the roulette score
        :rtype: :class:`iconservice.base.address.Address
        
		 ***/
		return this.roulette_score.get();
	}
	
	@External
	public void set_game_developers_share(BigInteger share) {
		/**
		 Sets the sum of game developers as well as platform share
        :param _share: Sum of game_devs as well as platform share
        :type _share: int
        :return:
		 * */
		
		if (!Context.getCaller().equals(Context.getOwner())) {
			Context.revert("This function can only be called by GAS owner");
		}
		this.game_developers_share.set(share);
	}
	
	@External(readonly = true)
	public BigInteger get_game_developers_share() {
		/**
		 Returns the sum of game developers and platform share.
        :return: Sum of game developers share as well as platform share
        :rtype: int
		 ***/
		
		return this.game_developers_share.get(); 
	}
	
	
	@External
	public void set_super_admin(Address super_admin) {
		/***
		Sets super admin. Super admin is also added in admins list. Only allowed
        by the contract owner.
        :param _super_admin: Address of super admin
        :type _super_admin: :class:`iconservice.base.address.Address` 
		 ***/
		if (Context.getCaller().equals(Context.getOwner())) {
			this.super_admin.set(super_admin);
			this.admin_list.add(super_admin);
		}
		
	}
	
	@External(readonly = true)
	public Address get_super_admin() {
		/**
        Return the super admin address
        :return: Super admin wallet address
        :rtype: :class:`iconservice.base.address.Address		  
		 **/
		if (DEBUG) {
			Context.println( Context.getOrigin().toString() + " is getting super admin address." + TAG);
		}
		return this.super_admin.get();
	}
	
	@External
	public void set_admin(Address admin) {
		/**
		Sets admin. Only allowed by the super admin.
        :param _admin: Wallet address of admin
        :type _admin: :class:`iconservice.base.address.Address`
        :return:
		 ***/
		
		if (Context.getCaller().equals(this.super_admin.get())) {
			this.admin_list.add(admin);
		}		
	}
	
	@External(readonly = true)
	public ArrayDB<Address> get_admin(){
		/**
        Returns all the admin list
        :return: List of admins
        :rtype: list	  
		 ***/
		
		if (DEBUG) {
			Context.println( Context.getOrigin().toString() + " is getting admin addresses." + TAG);
		}
		ArrayDB<Address> admin_list = Context.newArrayDB(ADMIN_LIST, Address.class);
		for(int i= 0; i< this.admin_list.size(); i++) {
			Address address = admin_list.get(i);
			this.admin_list.add(address);
		}
		return admin_list;
	}
	
	@External
	public void remove_admin(Address admin) {
		/***
        Removes admin from the admin arrayDB. Only called by the super admin
        :param _admin: Address of admin to be removed
        :type _admin: :class:`iconservice.base.address.Address`
        :return:
		***/
		if (Context.getCaller().equals(this.get_super_admin())) {
			if (!containsInArrayDb(admin, this.admin_list) ) {
				Context.revert("Invalid address: not in list");
			}

			Address top = this.admin_list.pop();
			if (!top.equals(admin)) {
				for (int i= 0; i<this.admin_list.size(); i++) {
					Address address =  this.admin_list.get(i);
					if (address.equals(admin)) {
						this.admin_list.set(i, address);
					}
				}
			}
			if (DEBUG) {
				Context.println( admin.toString() + " has been removed from admin list." + TAG);
			}
		
		}
		
	}
	
	@Payable
	@External
	public void submit_game_proposal(String gamedata) {
		/***
        Takes the proposal from new games who want to register in the ICONbet
        platform. The games need to submit game with a fee of 50 ICX as well as
        the game data needs to be a JSON string in the following format:
        {
            "name": ""(Name of the game, str),
            "scoreAddress": "", (User must submit a score address, the game can
                                be completed or else the score can contain the
                                boilerplate score required for ICONbet platform,
                                Address)
            "minBet": , (minBet must be greater than 100000000000000000(0.1 ICX), int)
            "maxBet": , (maxBet in the game in loop, int)
            "houseEdge": "", (house edge of the game in percentage, str)
            "gameType": "", (Type of game, type should be either "Per wager
                            settlement" or "Game defined interval settlement", str)
            "revShareMetadata": "" ,(data about how would you share your revenue)
            "revShareWalletAddress": "", (Wallet address in which you want to
                                         receive your percentage of the excess
                                         made by game)
            "linkProofPage": "" , (link of the page showing the game statistics)
            "gameUrlMainnet": "", (IP of the game in mainnet)
            "gameUrlTestnet": "", (IP of the game in testnet)
        }
        :param _gamedata: JSON object containing the data of game in above format
        :type _gamedata: str
        :return:
        ***/	
		Address sender = Context.getCaller();
		BigInteger fee = MULTIPLIER.multiply(new BigInteger("50"));
		
		if (fee.compareTo(Context.getValue()) != 0 ) {
			Context.revert("50 ICX is required for submitting game proposal");
		}
		
        JsonValue json = Json.parse(gamedata);
        if (!json.isArray()) {
            throw new IllegalArgumentException("Not json array");
        }
       
        JsonArray array = json.asArray();
        
		check_game_metadata(gamedata);
				
		Address score_at_address = Context.call(Address.class, Address.fromString(getValueFromItem(array,"scoreAddress")),"get_score_owner");
		if (!sender.equals(score_at_address)) {
			Context.revert("Owner not matched");
		}
		ProposalSubmitted(sender,score_at_address);
		if (containsInArrayDb(score_at_address,this.proposal_list) ) {
			Context.revert("Already listed scoreAddress in the proposal list.");
		}
		this.proposal_list.add(score_at_address);
		
		/// question =?   self._owner_data[Address.from_string(metadata['scoreAddress'])] = self.msg.sender
		this.owner_data.set(score_at_address, sender.toString());
		
		
		this.status_data.set(score_at_address, "waiting");
		this.proposal_data.set(score_at_address, gamedata); 
		
		if ( this.apply_watch_dog_method.get()) {
			BigInteger maxPayout = new BigInteger(getValueFromItem(array,"maxPayout"));
			this.maximum_payouts.set(score_at_address, maxPayout);
		}
 
	}
	
	
	/// question=? Python  def _check_game_metadata(self, _metadata: dict) ?dict?
	
	public void check_game_metadata(String metadata ) {
		/***
        Sanity checks for the game metadata
        :param _metadata: JSON metadata of the game
        :type _metadata: dict
        :return:
        ***/	
		//All fields should be provided
        JsonValue json = Json.parse(metadata);
        if (!json.isArray()) {
            throw new IllegalArgumentException("Not json array");
        }
       
        JsonArray array = json.asArray();       
        
        for (JsonValue item : array) {
            for (Member member : item.asObject()) {
            	 String field = member.getName();
            	 if (!METADATA_FIELDS.contains(field)) {
       				Context.revert("There is no "+field+" for the game");
            	 }
            }		
        }
        
		if ( this.apply_watch_dog_method.get()) {
			if (metadata.contains("maxPayout")) {
				BigInteger maxPayout = new BigInteger(getValueFromItem(array,"maxPayout"));
            	if (maxPayout.compareTo(_1_ICX) == -1) {
            		Context.revert(maxPayout.toString()+" is less than 0.1 ICX");
            	}		
			}else {
				Context.revert("There is no maxPayout for the game");
			}
		}
		
		// Check if name is empty
		if (!metadata.contains("name")) {
			Context.revert("Game name cant be empty");
		}
		
		// check if scoreAddress is a valid contract address
		if (metadata.contains("scoreAddress")) {
			Address scoreAddress = Address.fromString(getValueFromItem(array,"scoreAddress"));
			if (!scoreAddress.isContract()) {
				Context.revert(scoreAddress.toString() +" is not a valid contract address");
			}
		}
		
		// Check if minbet is within defined limit of 0.1 ICX
		BigInteger minBet = new BigInteger(getValueFromItem(array,"minBet"));
		if (minBet.compareTo(_1_ICX) == -1 ) {
			Context.revert(minBet.toString() +" is less than 0.1 ICX");
		}
		
	    // Check if proper game type is provided		
		String gameType = getValueFromItem(array,"gameType");
		if (!GAME_TYPE.contains(gameType)) {
			Context.revert("Not a valid game type");
		}
		
		// Check for revenue share wallet address
		String revwallet = getValueFromItem(array,"revwallet");
		try {
			Address revWalletAddress = Address.fromString(revwallet);
			if (!revWalletAddress.isContract() ) {
				Context.revert("Not a wallet address");
			}
		}catch(Exception e) {
			Context.revert("Invalid address");
		}
	}
	
	
	private String getValueFromItem(JsonArray array, String nameItem) {
        for (JsonValue item : array) {
            JsonObject member = item.asObject();
            String value = member.getString(nameItem,null);
            if( value!=null) {
            	return value;
            }
        }
        return "";
	}

	private <T> Boolean containsInArrayDb(T value, ArrayDB<T> arraydb) {
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
        
	
}