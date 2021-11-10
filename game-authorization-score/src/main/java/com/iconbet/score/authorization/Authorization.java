package com.iconbet.score.authorization;

import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.eclipsesource.json.Json;
import com.eclipsesource.json.JsonObject;
import com.eclipsesource.json.JsonValue;

import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;
import score.annotation.Optional;
import score.annotation.Payable;

public class Authorization{

	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "AUTHORIZATION";
	public static final boolean DEBUG = false;
	private static final BigInteger MULTIPLIER = new BigInteger("1000000000000000000");
	private static final BigInteger U_SECONDS_DAY = new BigInteger("86400000000"); // Microseconds in a day.

	private static final BigInteger _1_ICX = new BigInteger("100000000000000000"); // 0.1 ICX = 10^18 * 0.1

	private static List<String> METADATA_FIELDS = List.of("name", "scoreAddress", "minBet", "maxBet", "houseEdge",
			"gameType", "revShareMetadata", "revShareWalletAddress",
			"linkProofPage", "gameUrlMainnet", "gameUrlTestnet");

	private static List<String> GAME_TYPE = List.of("Per wager settlement", "Game defined interval settlement");

	private static List<String> STATUS_TYPE = List.of("waiting", "proposalApproved", "proposalRejected", "gameReady",
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
	private final BranchDB<BigInteger, DictDB<Address,BigInteger>> wagers = Context.newBranchDB(WAGERS, BigInteger.class);
	private final BranchDB<BigInteger, DictDB<Address, BigInteger>> payouts = Context.newBranchDB(PAYOUTS, BigInteger.class);

	private final VarDB<BigInteger> game_developers_share = Context.newVarDB(GAME_DEVELOPERS_SHARE, BigInteger.class);
	private final DictDB<Address,BigInteger> todays_games_excess = Context.newDictDB(TODAYS_GAMES_EXCESS, BigInteger.class);

	private final VarDB<BigInteger> new_div_changing_time = Context.newVarDB(NEW_DIV_CHANGING_TIME, BigInteger.class);
	private final BranchDB<BigInteger, DictDB<Address, BigInteger>> games_excess_history = Context.newBranchDB(GAMES_EXCESS_HISTORY, BigInteger.class);

	private final VarDB<Boolean> apply_watch_dog_method = Context.newVarDB(APPLY_WATCH_DOG_METHOD, Boolean.class);
	private final DictDB<Address,BigInteger> maximum_payouts = Context.newDictDB(MAXIMUM_PAYOUTS, BigInteger.class);
	private final VarDB<BigInteger> maximum_loss = Context.newVarDB(MAXIMUM_LOSS, BigInteger.class);

	private static final String PAUSED = "paused";
	private final VarDB<Boolean> onUpdate = Context.newVarDB(PAUSED, Boolean.class);

	public Authorization() {
		//we mimic on_update py feature, updating java score will call <init> (constructor) method 
		if (this.onUpdate.get() != null && this.onUpdate.get()) {
			onUpdate();
			return;
		}
		if(DEBUG) {
			Context.println("In __init__." +  TAG);
			Context.println("owner is " + Context.getOwner()+" "+ TAG);
		}
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
		day.set(now.divide(U_SECONDS_DAY));
		//TODO: should we define it as false by default?
		this.apply_watch_dog_method.set(false);

		this.onUpdate.set(true);
	}

	public void onUpdate() {
		Context.println("calling on update. "+TAG);
	}

	@EventLog(indexed=2) 
	public void FundTransfer(Address recipient, BigInteger amount, String note) {}     

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


	/**
	  Sets the equivalent time of 00:00 UTC of dividend structure changing
	        date in microseconds timestamp.
	        :param _timestamp: Timestamp of 00:00 UTC of dividend structure changing
	                           date in microseconds timestamp
	        :type _timestamp: int
	        :return:  
	 **/
	@External
	public void set_new_div_changing_time(BigInteger _timestamp) {

		if (Context.getCaller().equals(Context.getOwner())) {
			this.new_div_changing_time.set(_timestamp);
			List<Address> approved_games_list = get_approved_games();
			for (Address address: approved_games_list) {
				//TODO: verify if we are removing the value correctly here, should we set to zero?
				this.todays_games_excess.set(address, null);
			}
		}
	}

	/***
    Returns the new dividend changing time in microseconds timestamp.
    :return: New dividend changing time in timestamp
    :rtype: int
	 ***/
	@External(readonly = true)
	public BigInteger get_new_div_changing_time() {
		return new_div_changing_time.get();

	}


	/***
    Returns all the approved games' Address
    :return: List of approved games
    :rtype: list	
	 ***/
	@External(readonly = true)
	public List<Address> get_approved_games() {
		Address[] _proposal_list = new Address[this.proposal_list.size()];
		int j = 0;
		for (int i = 0; i< this.proposal_list.size();i++ ) {
			Address address = this.proposal_list.get(i);
			String gameApproved = this.status_data.get(address);
			if (gameApproved!=null &&  gameApproved.equals("gameApproved") ) {
				_proposal_list[j] = address;
				j++;
			}
		}
		Address[] tmp = new Address[j];
		System.arraycopy(_proposal_list, 0, tmp, 0, j);
		return List.of(tmp);
	}

	/**
    Sets the address of roulette/game score
    :param _scoreAddress: Address of roulette
    :type _scoreAddress: :class:`iconservice.base.address.Address`
    :return:
	 * **/
	@External
	public void set_roulette_score(Address _scoreAddress) {
		//TODO: should we call address isContract()?? contract means score?
		if (!Context.getCaller().equals(Context.getOwner())) {
			Context.revert("This function can only be called from the GAS owner.");
		}
		this.roulette_score.set(_scoreAddress);
	}

	/**
	Returns the roulette score address
    :return: Address of the roulette score
    :rtype: :class:`iconservice.base.address.Address
	 ***/
	@External(readonly = true)
	public Address get_roulette_score() {
		return this.roulette_score.getOrDefault(ZERO_ADDRESS);
	}

	/**
	 Sets the sum of game developers as well as platform share
   :param _share: Sum of game_devs as well as platform share
   :type _share: int
   :return:
	 * */
	@External
	public void set_game_developers_share(BigInteger _share) {
		if (!Context.getCaller().equals(Context.getOwner())) {
			Context.revert("This function can only be called by GAS owner");
		}
		this.game_developers_share.set(_share);
	}

	/**
	 Returns the sum of game developers and platform share.
   :return: Sum of game developers share as well as platform share
   :rtype: int
	 ***/
	@External(readonly = true)
	public BigInteger get_game_developers_share() {
		return this.game_developers_share.getOrDefault(ZERO);
	}


	/***
	Sets super admin. Super admin is also added in admins list. Only allowed
    by the contract owner.
    :param _super_admin: Address of super admin
    :type _super_admin: :class:`iconservice.base.address.Address` 
	 ***/
	@External
	public void set_super_admin(Address _super_admin) {
		if (Context.getCaller().equals(Context.getOwner())) {
			this.super_admin.set(_super_admin);
			this.admin_list.add(_super_admin);
		}

	}

	/**
    Return the super admin address
    :return: Super admin wallet address
    :rtype: :class:`iconservice.base.address.Address		  
	 **/
	@External(readonly = true)
	public Address get_super_admin() {
		if (DEBUG) {
			Context.println( Context.getOrigin().toString() + " is getting super admin address." + TAG);
		}
		return this.super_admin.get();
	}

	/**
	Sets admin. Only allowed by the super admin.
    :param _admin: Wallet address of admin
    :type _admin: :class:`iconservice.base.address.Address`
    :return:
	 ***/
	@External
	public void set_admin(Address _admin) {
		if (Context.getCaller().equals(this.super_admin.get())) {
			this.admin_list.add(_admin);
		}		
	}

	@External(readonly = true)
	public List<Address> get_admin(){
		/**
        Returns all the admin list
        :return: List of admins
        :rtype: list	  
		 ***/

		if (DEBUG) {
			Context.println( Context.getOrigin().toString() + " is getting admin addresses." + TAG);
		}
		Address[] admin_list = new Address[this.admin_list.size()];
		for(int i= 0; i< this.admin_list.size(); i++) {
			admin_list[i] = this.admin_list.get(i);
		}
		return List.of(admin_list);
	}

	/***
    Removes admin from the admin arrayDB. Only called by the super admin
    :param _admin: Address of admin to be removed
    :type _admin: :class:`iconservice.base.address.Address`
    :return:
	 ***/
	@External
	public void remove_admin(Address _admin) {
		if (Context.getCaller().equals(this.get_super_admin())) {
			if (!containsInArrayDb(_admin, this.admin_list) ) {
				Context.revert("Invalid address: not in list");
			}

			Address top = this.admin_list.pop();
			if (!top.equals(_admin)) {
				for (int i= 0; i<this.admin_list.size(); i++) {
					Address address =  this.admin_list.get(i);
					if (address.equals(_admin)) {
						this.admin_list.set(i, address);
					}
				}
			}
			if (DEBUG) {
				Context.println( _admin.toString() + " has been removed from admin list." + TAG);
			}

		}

	}

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
	@Payable
	@External
	public void submit_game_proposal(String _gamedata) {
		Address sender = Context.getCaller();
		BigInteger fee = MULTIPLIER.multiply(new BigInteger("50"));

		if (fee.compareTo(Context.getValue()) != 0 ) {
			Context.revert("50 ICX is required for submitting game proposal");
		}

		JsonValue json = Json.parse(_gamedata);
		if (!json.isObject()) {
			throw new IllegalArgumentException("_gamedata parameter is not a json object");
		}

		JsonObject jsonObject= json.asObject();

		_check_game_metadata(jsonObject);

		Address scoreAddress =  Address.fromString(jsonObject.get("scoreAddress").asString());
		Address score_at_address = Context.call(Address.class, scoreAddress,"get_score_owner");

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
		this.proposal_data.set(scoreAddress, _gamedata); 

		if ( this.apply_watch_dog_method.get()) {
			BigInteger maxPayout = new BigInteger(jsonObject.get("maxPayout").asString());
			this.maximum_payouts.set(scoreAddress, maxPayout);
		}

	}

	/***
	Admin can change the game status according to its previous status.
    :param _status: Status of the game.
    :type _status: str
    :param _scoreAddress: Score address of the game for which status is to be changed
    :type _scoreAddress: :class:`iconservice.base.address.Address`
    :return:
	 ***/
	@External
	public void set_game_status(String _status,Address _scoreAddress) {
		Address sender = Context.getCaller();
		if ( !this.get_admin().contains(sender)) {
			Context.revert("Sender not an admin");
		}
		if (!STATUS_TYPE.contains(_status)) {
			Context.revert("Invalid status");
		}
		String statusScoreAddress = this.status_data.get(_scoreAddress);
		if(_status.equals("gameRejected") && !statusScoreAddress.equals("gameReady") ) {
			Context.revert("This game cannot be rejected from state " +statusScoreAddress );
		}
		if(_status.equals("gameApproved") && !(
				statusScoreAddress.equals("gameReady") || statusScoreAddress.equals("gameSuspended")
				)) {
			Context.revert("This game cannot be approved from state " +statusScoreAddress );
		}
		if(_status.equals("gameSuspended") && !statusScoreAddress.equals("gameApproved")) {
			Context.revert("Only approved games may be suspended.");
		}
		if(_status.equals("gameDeleted") && !statusScoreAddress.equals("gameSuspended")) {
			Context.revert("Only suspended games may be deleted.");
		}

		this.status_data.set(_scoreAddress, _status);

	}

	/***
    When the game developer has completed the code for SCORE, can set the
    address of the game as ready.
    :param _scoreAddress: Address of the Game which is to be made ready
    :type _scoreAddress: :class:`iconservice.base.address.Address`
    :return:
	 ***/
	@External
	public void set_game_ready(Address _scoreAddress ) {
		Address sender = Context.getCaller();
		Address owner = this.owner_data.get(_scoreAddress);

		if (!sender.equals(owner)) {
			Context.revert("Sender not the owner of SCORE ");
		}
		this.status_data.set(_scoreAddress, "gameReady");
	}

	/***
    Sanity checks for the game metadata
    :param _metadata: JSON metadata of the game
    :type _metadata: dict
    :return:
	 ***/
	/// question=? Python  def _check_game_metadata(self, _metadata: dict) ?dict?
	/// TODO Cambiar a Map o Json 
	public void _check_game_metadata(JsonObject _metadata) {

		//All fields should be provided       
		for(String name: METADATA_FIELDS) {
			if(!_metadata.contains(name)) {
				Context.revert("There is no "+name+" for the game");
			}
		}

		Context.println("is apply_watch_dog_method ? "+ this.apply_watch_dog_method.get());

		if ( this.apply_watch_dog_method.get()) {
			String maxPayoutStr = _metadata.get("maxPayout").asString();
			if (!maxPayoutStr.isEmpty()) {
				BigInteger maxPayout = new BigInteger(maxPayoutStr);
				if (maxPayout.compareTo(_1_ICX) == -1) {
					Context.revert(maxPayout.toString()+" is less than 0.1 ICX");
				}
			}else {
				Context.revert("There is no maxPayout for the game");
			}
		}

		//TODO: use getString(name, default value) to prevent null pointer exceptions
		// Check if name is empty
		String nameStr = _metadata.get("name").asString();
		if (nameStr.isEmpty()) {
			Context.revert("Game name cant be empty");
		}

		// check if scoreAddress is a valid contract address
		String scoreAddressStr = _metadata.get("scoreAddress").asString();
		if (!scoreAddressStr.isEmpty()) {
			Address scoreAddress = Address.fromString(scoreAddressStr);
			if (!scoreAddress.isContract()) {
				Context.revert(scoreAddress.toString() +" is not a valid contract address");
			}
		}

		// Check if minbet is within defined limit of 0.1 ICX
		String minBetStr = _metadata.get("minBet").asString();
		BigInteger minBet = new BigInteger(minBetStr);
		if (minBet.compareTo(_1_ICX) == -1 ) {
			Context.revert(minBet.toString() +" is less than 0.1 ICX");
		}

		// Check if proper game type is provided		
		String gameType = _metadata.get("gameType").asString();
		if (!GAME_TYPE.contains(gameType)) {
			Context.revert("Not a valid game type");
		}

		// Check for revenue share wallet address
		String revwallet = _metadata.get("revShareWalletAddress").asString();
		try {
			Address revWalletAddress = Address.fromString(revwallet);
			if (!revWalletAddress.isContract() ) {
				Context.revert("Not a wallet address");
			}
		}catch(Exception e) {
			Context.revert("Invalid address");
		}
		Context.println("metadata json is valid");
	}

	/***
    Accumulates daily wagers of the game. Updates the excess of the game.
    Only roulette score can call this function.
    :param game: Address of the game
    :type game: :class:`iconservice.base.address.Address`
    :param wager: Wager amount of the game
    :type wager: int
    :return:
	 ***/
	@External
	public void accumulate_daily_wagers(Address game, BigInteger wager ) {
		Address sender = Context.getCaller();

		if (!sender.equals(this.roulette_score.get()) ) {
			Context.revert("Only roulette score can invoke this method.");			
		}
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());
		BigInteger day = now.divide(U_SECONDS_DAY);

		BigInteger wagerValue = this.wagers.at(day).getOrDefault(game, ZERO);
		this.wagers.at(day).set(game, wager.add(wagerValue));

		Context.println("acumulated wager at "+day+" for game "+ game + " is " + this.wagers.at(day).get(game));
		BigInteger newTime = this.new_div_changing_time.get();

		if ( newTime!= null && now.compareTo(newTime)>=1 ) {
			BigInteger excess = this.todays_games_excess.getOrDefault(game, ZERO);
			this.todays_games_excess.set(game, excess.add(wager));
		}

	}

	/***
    Get daily wagers for a game in a particular day
    :param day: Index of the day for which wagers is to be returned,
                index=timestamp//(seconds in a day)
    :type day: int
    :return: Wagers for all games on that particular day
    :rtype: dict
	 ***/
	@SuppressWarnings("unchecked")
	@External(readonly = true)
	public Map<String, String> get_daily_wagers(@Optional BigInteger day) {

		if (day == null) {
			day = BigInteger.ZERO;
		}

		if (day.compareTo(BigInteger.ONE) == -1) {
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			day.add(now.divide(U_SECONDS_DAY));			
		}

		Map.Entry<String, String>[] wagers = new Map.Entry[this.get_approved_games().size()];

		for (int i= 0; i< this.get_approved_games().size(); i++ ) {
			Address game = this.get_approved_games().get(i);
			wagers[i] = Map.entry(game.toString(), String.valueOf(this.wagers.at(day).get(game)) );
		}
		return Map.ofEntries(wagers);
	}

	/***
    Accumulates daily payouts of the game. Updates the excess of the game.
     Only roulette score can call this function.
     :param game: Address of the game
     :type game: :class:`iconservice.base.address.Address`
     :param payout: Payout amount of the game
     :type payout: int
     :return:
	 ***/
	@External
	public boolean accumulate_daily_payouts(Address game, BigInteger payout) {
		Address roulette = this.roulette_score.get();
		if (!Context.getCaller().equals(roulette) ) {
			Context.revert("Only roulette score can invoke this method.");
		}
		BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
		BigInteger day = now.divide(U_SECONDS_DAY);

		if (this.apply_watch_dog_method.getOrDefault(false) ) {
			try {
				Context.println("apply watch dog enabled . " + TAG);
				if ( payout.compareTo(this.maximum_payouts.getOrDefault(game, ZERO)) == 1 ) {
					Context.revert("Preventing Overpayment. Requested payout: " +payout.toString() +
							". MaxPayout for this game: "+this.maximum_payouts.get(game) +
							". "+ TAG);
				}

				BigInteger payOutDay = this.payouts.at(day).getOrDefault(game, ZERO);
				payOutDay = payOutDay.add(payout);
				BigInteger wagerDay = this.wagers.at(day).getOrDefault(game, ZERO);
				BigInteger incurred = payOutDay.subtract(wagerDay);
				Context.println("incurred payout: " + incurred);
				if(incurred.compareTo(this.maximum_loss.getOrDefault(ZERO) ) >= 1) {
					Context.revert("Limit loss. MaxLoss: " +this.maximum_loss.getOrDefault(ZERO)+". Loss Incurred if payout: "+
							incurred.intValue()+ " " +TAG);
				}

			}catch (Exception e) {
				Context.println("error thrown:" + e.getMessage());
				this.status_data.set(game, "gameSuspended");
				this.GameSuspended(game, e.getMessage());
				return false;
			}

		}

		BigInteger newPayOut = this.payouts.at(day).getOrDefault(game, ZERO);
		payout = payout.add(newPayOut);
		this.payouts.at(day).set(game, payout);
		Context.println("new payout:" + payout + "at day " +day + " ." + TAG);

		if ( this.new_div_changing_time.getOrDefault(ZERO).compareTo(ZERO) != 0 &&
				day.compareTo(this.new_div_changing_time.get()) >= 1) {
			BigInteger accumulate = this.todays_games_excess.getOrDefault(game, ZERO);
			this.todays_games_excess.set(game, accumulate.subtract(payout));
		}
		return true;
	}

	/***
    Get daily payouts for a game in a particular day
    :param day: Index of the day for which payouts is to be returned
    :type day: int
    :return: Payouts of the game in that particular day
    :rtype: int
	 ***/
	@SuppressWarnings("unchecked")
	@External(readonly = true)
	public Map<String, String> get_daily_payouts(@Optional BigInteger day) {

		if (day == null) {
			day = BigInteger.ZERO;
		}

		if (day.compareTo(BigInteger.ONE) == -1) {
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			day = day.add(now.divide(U_SECONDS_DAY));
		}

		Map.Entry<String, String>[] payouts = new Map.Entry[this.get_approved_games().size()];  

		for (int i=0; i<this.get_approved_games().size(); i++) {
			Address game = this.get_approved_games().get(i);
			payouts[i] = Map.entry(game.toString(), String.valueOf(this.payouts.at(day).get(game)));
		}
		return Map.ofEntries(payouts);
	}

	/***
    Returns the metadata fields which the games need to submit while
    submitting proposal.
    :return: List of metadata fields
    :rtype: list
	 ***/
	@External(readonly = true)
	public List<String> get_metadata_fields(){
		return this.METADATA_FIELDS;
	}

	/***
    Returns the proposal data of the game address
    :param _scoreAddress: Game address for which proposal data is to be fetched
    :type _scoreAddress: :class:`iconservice.base.address.Address`
    :return: JSON object of the proposal data of the game
    :rtype: str
	 ***/
	/// question def get_proposal_data(self, _scoreAddress: Address) -> str ? toString ?
	@External(readonly = true)
	public String get_proposal_data(Address _scoreAddress) {
		return this.proposal_data.get(_scoreAddress);
	}

	/***
    Returns all the games' Address regardless of their status.
    :return: List of games' Address
    :rtype: list
	 ***/
	@External(readonly = true)
	public List<Address> get_score_list(){

		Address[] proposal_list = new Address[this.proposal_list.size()];

		for(int i= 0; i< this.proposal_list.size(); i++) {
			proposal_list[i] = this.proposal_list.get(i);
		}		
		return List.of(proposal_list);
	}

	/**
    Returns the revshare wallet address of the game
    :param _scoreAddress: Address of the game for which revenue share wallet
                          address is to be fetched
    :type _scoreAddress: :class:`iconservice.base.address.Address`
    :return: Revenue share wallet address of the game
    :rtype: :class:`iconservice.base.address.Address`
	 ***/
	@External(readonly = true)
	public Address get_revshare_wallet_address(Address _scoreAddress) {

		String gamedata =  this.proposal_data.get(_scoreAddress);
		JsonValue json = Json.parse(gamedata);
		if (!json.isObject()) {
			throw new IllegalArgumentException("metadata is Not a json object");
		}

		String revShareWalletAddressStr = json.asObject().get("revShareWalletAddress").asString();

		return Address.fromString(revShareWalletAddressStr);
	}


	/***
    Returns the available types of games.
    :return: List of types of games that the game owner can choose from
    :rtype: list
	 ***/
	@External(readonly = true)
	public List<String> get_game_type(){
		return this.GAME_TYPE;
	}

	/***
    Returns the status of the game.
    :param _scoreAddress: Address of the game
    :type _scoreAddress: :class:`iconservice.base.address.Address`
    :return: Status of game
    :rtype: str
	 ***/
	@External(readonly = true)
	public String get_game_status(Address _scoreAddress) {

		return this.status_data.get(_scoreAddress);

	}

	/***
    Returns the excess share of game developers and founders
    :return: Game developers share
    :rtype: int
	 ***/
	@External(readonly = true)	
	public BigInteger get_excess() {

		BigInteger positive_excess = BigInteger.ZERO;
		BigInteger game_developers_amount = BigInteger.ZERO;

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

	/***
    Roulette score calls this function if the day has been advanced. This
    function takes the snapshot of the excess made by the game till the
    advancement of day.
    :return: Sum of game developers amount
    :rtype: int
	 ***/
	@External
	public BigInteger record_excess() {
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
			this.games_excess_history.at(day.subtract(BigInteger.ONE)).set(game, game_excess);
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


	/***
    Returns the todays excess of the game. The excess is reset to 0 if it
    remains positive at the end of the day.
    :return: Returns the excess of games at current time
	 ***/
	@SuppressWarnings("unchecked")
	@External(readonly = true)
	public Map<String, String> get_todays_games_excess() {
		Map.Entry<String, String>[] games_excess = new Map.Entry[this.get_approved_games().size()];

		for (int i= 0; i<this.get_approved_games().size(); i++ ) {
			Address game = this.get_approved_games().get(i);
			games_excess[i] = Map.entry(game.toString(), String.valueOf(this.todays_games_excess.get(game)));
		}

		return Map.ofEntries(games_excess);
	}


	/***
    Returns a dictionary with game addresses as keys and the excess as the
    values for the specified day.
    :return: Dictionary of games' address and excess of the games
    :rtype: dict
	 ***/
	//TODO: we can chage this from String, String to String, BigInteger
	@SuppressWarnings("unchecked")
	@External(readonly = true)
	public Map<String, String> get_games_excess(@Optional BigInteger day) {

		if(day == null) {
			day = BigInteger.ZERO;
		}

		if (day.compareTo(BigInteger.ZERO)== 0) {
			return this.get_todays_games_excess();
		}
		if (day.compareTo(BigInteger.ZERO)== -1) {
			BigInteger now = BigInteger.valueOf(Context.getBlockTimestamp());		
			day = day.add(now.divide(U_SECONDS_DAY));
		}
		Map.Entry<String, String>[] games_excess = new Map.Entry[this.get_approved_games().size()];
		for(int i= 0; i< this.get_approved_games().size(); i++) {
			Address game = this.get_approved_games().get(i);
			games_excess[i] = Map.entry(game.toString(), String.valueOf(this.games_excess_history.at(day).get(game)));
		}
		return Map.ofEntries(games_excess);
	}

	@External(readonly = true)
	public Map<String, String> get_yesterdays_games_excess(){
		/***
        Returns the dictionary containing keys as games address and value as
        excess of the game of yesterday
        :return: Dictionary of games' address and excess of the games
        :rtype: dict
		 ***/

		return this.get_games_excess(BigInteger.ONE.negate());
	}


	@Payable
	public void fallback() {}

	@External
	public void set_maximum_loss(BigInteger maxLoss) {
		Context.println("Setting maxLoss of "+ maxLoss.toString() );
		if ( maxLoss.compareTo(_1_ICX)== -1 ) { // 0.1 ICX = 10^18 * 0.1
			Context.revert("maxLoss is set to a value less than 0.1 ICX");
		}
		Address sender = Context.getCaller();
		if (!this.get_admin().contains(sender)) {
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
		if (!this.get_admin().contains(sender)) {
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
		if (!this.get_admin().contains(sender)) {
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