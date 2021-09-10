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
	

	//question ?? how to implement this?
		/***
	    def on_update(self) -> None:
	        super().on_update()
	        self._day.set(self.now() // U_SECONDS_DAY)
	        self._game_developers_share.set(20)
	        
	        ***/
	
	       /***
	        *  //question ?? instal == constructor?	    	
	  	  def on_install(self) -> None:
	  	        super().on_install()
	  	        self._day.set(self.now() // U_SECONDS_DAY)
	      ***/
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
	private final DictDB<Address,Address> owner_data = Context.newDictDB(OWNER_DATA, Address.class);
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
				this.todays_games_excess.set(address, null);
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
        
		check_game_metadata(array);

		Address scoreAddress =  Address.fromString(getValueFromItem(array,"scoreAddress"));
		Address score_at_address = Context.call(Address.class, Address.fromString(getValueFromItem(array,"scoreAddress")),"get_score_owner");
		
		if (!sender.equals(score_at_address)) {
			Context.revert("Owner not matched");
		}
		ProposalSubmitted(sender,scoreAddress);
		if (containsInArrayDb(scoreAddress,this.proposal_list) ) {
			Context.revert("Already listed scoreAddress in the proposal list.");
		}
		this.proposal_list.add(scoreAddress);
		
		/// question =?   self._owner_data[Address.from_string(metadata['scoreAddress'])] = self.msg.sender
		this.owner_data.set(scoreAddress, sender);
		
		
		this.status_data.set(scoreAddress, "waiting");
		this.proposal_data.set(scoreAddress, gamedata); 
		
		if ( this.apply_watch_dog_method.get()) {
			BigInteger maxPayout = new BigInteger(getValueFromItem(array,"maxPayout"));
			this.maximum_payouts.set(scoreAddress, maxPayout);
		}
 
	}
	
	
	/// question=? Python  def _check_game_metadata(self, _metadata: dict) ?dict?
	/// TODO Cambiar a Map o Json 
	public void check_game_metadata(JsonArray array) {
		/***
        Sanity checks for the game metadata
        :param _metadata: JSON metadata of the game
        :type _metadata: dict
        :return:
        ***/	
				
		//All fields should be provided       
        
        for (JsonValue item : array) {
            for (Member member : item.asObject()) {
            	 String field = member.getName();
            	 if (!METADATA_FIELDS.contains(field)) {
       				Context.revert("There is no "+field+" for the game");
            	 }
            }		
        }
        
		if ( this.apply_watch_dog_method.get()) {
			String maxPayoutStr = getValueFromItem(array,"maxPayout");
			if (!maxPayoutStr.isBlank()) {
				BigInteger maxPayout = new BigInteger(maxPayoutStr);
            	if (maxPayout.compareTo(_1_ICX) == -1) {
            		Context.revert(maxPayout.toString()+" is less than 0.1 ICX");
            	}		
			}else {
				Context.revert("There is no maxPayout for the game");
			}
		}
		
		// Check if name is empty
		String nameStr = getValueFromItem(array,"name");
		if (nameStr.isBlank()) {
			Context.revert("Game name cant be empty");
		}
		
		// check if scoreAddress is a valid contract address
		String scoreAddressStr = getValueFromItem(array,"name");
		if (!scoreAddressStr.isBlank()) {
			Address scoreAddress = Address.fromString(scoreAddressStr);
			if (!scoreAddress.isContract()) {
				Context.revert(scoreAddress.toString() +" is not a valid contract address");
			}
		}
		
		// Check if minbet is within defined limit of 0.1 ICX
		String minBetStr = getValueFromItem(array,"minBet");
		BigInteger minBet = new BigInteger(minBetStr);
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
	
	
	@External
	public void accumulate_daily_wagers(Address game, BigInteger wager ) {
		/***
        Accumulates daily wagers of the game. Updates the excess of the game.
        Only roulette score can call this function.
        :param game: Address of the game
        :type game: :class:`iconservice.base.address.Address`
        :param wager: Wager amount of the game
        :type wager: int
        :return:
		***/
		Address sender = Context.getCaller();
		
		if (!sender.equals(this.roulette_score.get()) ) {
			Context.revert("Only roulette score can invoke this method.");			
		}
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
		BigInteger day = now.divide(U_SECONDS_DAY);
		
		//Question ? day.intValue() number of the day
		BigInteger wagerValue = this.wagers.get(game)[day.intValue()];
		this.wagers.get(game)[day.intValue()] = wagerValue.add(wager);
		
		BigInteger newTime =this.new_div_changing_time.get();

		if ( newTime!= null && now.compareTo(newTime)>=1 ) {
			BigInteger excess = this.todays_games_excess.get(game);
			this.todays_games_excess.set(game, excess.add(wager));
		}
				
	}
	
	
	@External(readonly = true)
	public DictDB<Address, BigInteger[]> get_daily_wagers(BigInteger day) {
		/***
        Get daily wagers for a game in a particular day
        :param day: Index of the day for which wagers is to be returned,
                    index=timestamp//(seconds in a day)
        :type day: int
        :return: Wagers for all games on that particular day
        :rtype: dict
		***/
		if (day.compareTo(BigInteger.ONE) == -1) {
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			day.add(now.divide(U_SECONDS_DAY));			
		}
		
		 DictDB<Address, BigInteger[]> wagers = Context.newDictDB("wagers", BigInteger[].class);
		 
		for (int i= 0; i< this.get_approved_games().size(); i++ ) {
			Address game = this.get_approved_games().get(i);
			wagers.get(game)[i] = this.wagers.get(game)[day.intValue()];
		}
		return wagers;
	}
	
	@External
	public boolean accumulate_daily_payouts(Address game, BigInteger payout) {
		/***
	       Accumulates daily payouts of the game. Updates the excess of the game.
	        Only roulette score can call this function.
	        :param game: Address of the game
	        :type game: :class:`iconservice.base.address.Address`
	        :param payout: Payout amount of the game
	        :type payout: int
	        :return:
		***/
		Address roulette = this.roulette_score.get();
		if (!Context.getCaller().equals(roulette) ) {
			Context.revert("Only roulette score can invoke this method.");
		}
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
		BigInteger day = BigInteger.ZERO;
		day = day.add(now.divide(U_SECONDS_DAY));
		
		if (this.apply_watch_dog_method.get()!= null && 
				this.apply_watch_dog_method.get() ) {
			try {
				
				if ( payout.compareTo(this.maximum_payouts.get(game)) == 1 ) {
					Context.revert("Preventing Overpayment. Requested payout: " +payout.toString() +
							". MaxPayout for this game: "+this.maximum_payouts.get(game) +
							". "+ TAG);
				}
				
				BigInteger payOutDay = this.payouts.get(game)[day.intValue()].add(payout);
				BigInteger wagerDay = this.wagers.get(game)[day.intValue()];
				BigInteger incurred = payOutDay.subtract(wagerDay);
				if(incurred.compareTo(this.maximum_loss.get()) >= 1) {
					Context.revert("Limit loss. MaxLoss: " +this.maximum_loss.get()+". Loss Incurred if payout: "+
							incurred.intValue()+ " " +TAG);
				}
				
			}catch (Exception e) {
				this.status_data.set(game, "gameSuspended");
				this.GameSuspended(game, e.getMessage());
				return false;
			}
			
		}
		
		BigInteger newPayOut = this.payouts.get(game)[day.intValue()];
		this.payouts.get(game)[day.intValue()] = newPayOut.add(payout);

		if ( this.new_div_changing_time.get() != null && 
				this.new_div_changing_time.get().compareTo(BigInteger.ZERO) != 0 &&
				 day.compareTo(this.new_div_changing_time.get()) >= 1) {
			BigInteger accumulate = this.todays_games_excess.get(game);
			this.todays_games_excess.set(game, accumulate.subtract(payout));
		}
		return false;
	}
	
	
	//Question   def get_daily_payouts(self, day: int = 0 ?? initilize if null?
	@External(readonly = true)
	public DictDB<Address, BigInteger[]>  get_daily_payouts(BigInteger day) {
		/***
        Get daily payouts for a game in a particular day
        :param day: Index of the day for which payouts is to be returned
        :type day: int
        :return: Payouts of the game in that particular day
        :rtype: int
        ***/
		if (day.compareTo(BigInteger.ONE)== -1) {
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			day = day.add(now.divide(U_SECONDS_DAY));
		}
		
		DictDB<Address, BigInteger[]> payouts = Context.newDictDB("payouts", BigInteger[].class);

		for (int i=0; i<this.get_approved_games().size(); i++) {
			Address game = this.get_approved_games().get(i);
			payouts.get(game)[i] = this.payouts.get(game)[day.intValue()];
		}
		return payouts;
	}

	@External(readonly = true)
	public List<String> get_metadata_fields(){
		/***
        Returns the metadata fields which the games need to submit while
        submitting proposal.
        :return: List of metadata fields
        :rtype: list
        ***/
		
		return this.METADATA_FIELDS;
	}

	/// question def get_proposal_data(self, _scoreAddress: Address) -> str ? toString ?
	@External(readonly = true)
	public String get_proposal_data(Address scoreAddress) {
		/***
        Returns the proposal data of the game address
        :param _scoreAddress: Game address for which proposal data is to be fetched
        :type _scoreAddress: :class:`iconservice.base.address.Address`
        :return: JSON object of the proposal data of the game
        :rtype: str
        ***/
		
		return this.proposal_data.get(scoreAddress).toString();
	}
	
	//question ?   def get_score_list(self) -> list or rayDB<Address> ??
	@External(readonly = true)
	public ArrayDB<Address> get_score_list(){
		/***
        Returns all the games' Address regardless of their status.
        :return: List of games' Address
        :rtype: list
        ***/
		
		ArrayDB<Address> proposal_list = Context.newArrayDB("proposal_list", Address.class);

		for(int i= 0; i< this.proposal_list.size(); i++) {
			Address scoreAddress = this.proposal_list.get(i);
			proposal_list.add(scoreAddress);
		}		
		return proposal_list;
	}

	@External(readonly = true)
	public Address get_revshare_wallet_address(Address scoreAddress) {
		/**
        Returns the revshare wallet address of the game
        :param _scoreAddress: Address of the game for which revenue share wallet
                              address is to be fetched
        :type _scoreAddress: :class:`iconservice.base.address.Address`
        :return: Revenue share wallet address of the game
        :rtype: :class:`iconservice.base.address.Address`
        ***/
		
		String gamedata =  this.proposal_data.get(scoreAddress);
        JsonValue json = Json.parse(gamedata);
        if (!json.isArray()) {
            throw new IllegalArgumentException("Not json array");
        }       
        JsonArray array = json.asArray();        
        String revShareWalletAddressStr = getValueFromItem(array,"revShareWalletAddress");        
		
        return Address.fromString(revShareWalletAddressStr);
	}
	
	
	@External(readonly = true)
	public List<String> get_game_type(){
		/***
        Returns the available types of games.
        :return: List of types of games that the game owner can choose from
        :rtype: list
        ***/
		
		return this.GAME_TYPE;
	}
	
	@External(readonly = true)
	public String get_game_status(Address scoreAddress) {
		/***
        Returns the status of the game.
        :param _scoreAddress: Address of the game
        :type _scoreAddress: :class:`iconservice.base.address.Address`
        :return: Status of game
        :rtype: str
        ***/
		
		return status_data.get(scoreAddress);
		
	}
	

	@External(readonly = true)	
	public BigInteger get_excess() {
		/***
        Returns the excess share of game developers and founders
        :return: Game developers share
        :rtype: int
        ***/
		
		BigInteger positive_excess = BigInteger.ZERO;
		BigInteger game_developers_amount = BigInteger.ZERO;
		this.get_approved_games();
		for(int i= 0; i< this.get_approved_games().size(); i++) {
			Address game = this.get_approved_games().get(i);
			BigInteger game_excess =  this.todays_games_excess.get(game);
			if (game_excess!= null && 
					game_excess.compareTo(BigInteger.ZERO)>= 0) {
				positive_excess = positive_excess.add(game_excess);
			}			
		}
		
		game_developers_amount = this.game_developers_share.get().multiply(positive_excess);
		game_developers_amount = game_developers_amount.divide(BigInteger.valueOf(100L));
		
		return game_developers_amount;
	}
	
	@External
	public BigInteger record_excess() {
		/***
        Roulette score calls this function if the day has been advanced. This
        function takes the snapshot of the excess made by the game till the
        advancement of day.
        :return: Sum of game developers amount
        :rtype: int
        ***/
		Address sender = Context.getCaller();
		
		if (!sender.equals( this.roulette_score.get()) ) {
			Context.revert("This method can only be called by Roulette score");
		}
		BigInteger positive_excess = BigInteger.ZERO;
		BigInteger game_developers_amount = BigInteger.ZERO;
		BigInteger day = BigInteger.ZERO;
		
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
		day = day.add(now.divide(U_SECONDS_DAY));
		
		for (int i= 0; i< this.get_approved_games().size(); i++ ) {
			Address game = this.get_approved_games().get(i);
			BigInteger game_excess =  this.todays_games_excess.get(game);
			this.games_excess_history.get(game)[day.intValue() - 1 ] = game_excess;
			if (game_excess!= null &&
					game_excess.compareTo(BigInteger.ZERO)>= 0) {
				positive_excess = positive_excess.add(game_excess);
				this.todays_games_excess.set(game, BigInteger.ZERO);
			}
		}
		game_developers_amount = this.game_developers_share.get().multiply(positive_excess);
		game_developers_amount = game_developers_amount.divide(BigInteger.valueOf(100L));
		
		return game_developers_amount;
	}
        	
	
	
	// questions dict =  DictDB<Address, BigInteger[]>  ???
	@External(readonly = true)
	public DictDB<Address, BigInteger[]> get_todays_games_excess() {
		/***
        Returns the todays excess of the game. The excess is reset to 0 if it
        remains positive at the end of the day.
        :return: Returns the excess of games at current time
        ***/
		DictDB<Address, BigInteger[]> games_excess = Context.newDictDB("games_excess", BigInteger[].class);

		for (int i= 0; i<this.get_approved_games().size(); i++ ) {
			Address game = this.get_approved_games().get(i);
			games_excess.get(game)[i] = this.todays_games_excess.get(game);
		}
		
		return games_excess;		
	}

	
	// questions dict =  DictDB<Address, BigInteger[]>  ???
	@External(readonly = true)
	public DictDB<Address, BigInteger[]> get_games_excess(BigInteger day) {
		/***
        Returns a dictionary with game addresses as keys and the excess as the
        values for the specified day.
        :return: Dictionary of games' address and excess of the games
        :rtype: dict
        ***/
		if (day.compareTo(BigInteger.ZERO)== 0) {
			return this.get_todays_games_excess();
		}
		if (day.compareTo(BigInteger.ZERO)== -1) {
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			day = day.add(now.divide(U_SECONDS_DAY));
		}
		DictDB<Address, BigInteger[]> games_excess = Context.newDictDB("games_excess", BigInteger[].class);
		for(int i= 0; i< this.get_approved_games().size(); i++) {
			Address game = this.get_approved_games().get(i);
			games_excess.get(game)[i] = this.games_excess_history.get(game)[day.intValue()];
		}
		
		return games_excess;
	}
	
	// questions dict =  DictDB<Address, BigInteger[]>  ???
	@External(readonly = true)
	public DictDB<Address, BigInteger[]> get_yesterdays_games_excess(){
		/***
        Returns the dictionary containing keys as games address and value as
        excess of the game of yesterday
        :return: Dictionary of games' address and excess of the games
        :rtype: dict
        ***/
		
		return this.get_games_excess(BigInteger.ONE.negate());
	}
	
	
/***	
	// question ?? how to implement this?
	   @payable
	    def fallback(self):
	        pass
***/
	
	@External
	public void set_maximum_loss(BigInteger maxLoss) {
		Context.println("Setting maxLoss of "+ maxLoss.toString() );
		if ( maxLoss.compareTo(_1_ICX)== -1 ) { // 0.1 ICX = 10^18 * 0.1
			Context.revert("maxLoss is set to a value less than 0.1 ICX");
		}
		Address sender = Context.getCaller();
		if (!containsInArrayDb(sender, this.get_admin())) {
			Context.revert("Sender not an admin");
		}
		this.maximum_loss.set(maxLoss);
	}
	
	@External(readonly = true)
	public BigInteger get_maximum_loss() {
		
		return maximum_loss.get();
	}
	
	@External
	public void set_maximum_payout(Address game, BigInteger maxPayout ) {
		if ( maxPayout.compareTo(_1_ICX) == -1 ) { // 0.1 ICX = 10^18 * 0.1
			Context.revert(maxPayout.toString() + "is less than 0.1 ICX");
		}
		
		if (!containsInArrayDb(game, this.proposal_list)) {
			Context.revert("Game has not been submitted.");
		}
		Address sender = Context.getCaller();
		if (!containsInArrayDb(sender, this.get_admin())) {
			Context.revert("Sender not an admin");
		}
		this.maximum_payouts.set(game, maxPayout);
	}
	
	
	@External(readonly = true)
	public BigInteger get_maximum_payout(Address game) {
		
		if (!containsInArrayDb(game, this.proposal_list)) {
			Context.revert("Game has not been submitted.");
		}
		return this.maximum_payouts.get(game);
	}
	
	@External
	public void toggle_apply_watch_dog_method() {
		Address sender = Context.getCaller();
		if (!containsInArrayDb(sender, this.get_admin())) {
			Context.revert("Sender not an admin");
		}
		boolean old_watch_dog_status = this.apply_watch_dog_method.get();
		if (!old_watch_dog_status) {
			//# All approved games must have minimum_payouts set before applying watch dog methods.
			for (int i= 0; i< this.proposal_list.size(); i++) {
				Address scoreAddress = this.proposal_list.get(i);
				String scoreAddressValue = this.status_data.get(scoreAddress);
				if (scoreAddressValue.equals("gameApproved")) {
					BigInteger maximum_payouts = this.maximum_payouts.get(scoreAddress);
					if(maximum_payouts.compareTo(_1_ICX) == -1) {
						Context.revert("maxPayout of "+scoreAddress.toString() +" is less than 0.1 ICX");
					}
				}
			}
			BigInteger maximum_loss = this.maximum_loss.get();
			if (maximum_loss.compareTo(_1_ICX)== -1 ) {
				Context.revert("maxLoss is set to a value less than 0.1 ICX");
			}
		}
		
		this.apply_watch_dog_method.set(!old_watch_dog_status);
	}
	
	@External(readonly = true)
	public boolean get_apply_watch_dog_method() {
		return this.apply_watch_dog_method.get();
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