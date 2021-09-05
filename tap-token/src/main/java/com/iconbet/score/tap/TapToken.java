package com.iconbet.score.tap;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iconloop.score.token.irc2.IRC2;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;

public class TapToken implements IRC2{
	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	private static final String TAG = "TapToken";
	//TODO: verify this value exists as long and not as biginteger
	private static long DAY_TO_MICROSECOND = (long) Math.pow( 24*60*60, 6);
	private static final String BALANCES = "balances";
	private static final String TOTAL_SUPPLY = "total_supply";
	private static final String DECIMALS = "decimals";
	private static final String ADDRESSES = "addresses";

	private static final String EVEN_DAY_CHANGES = "even_day_changes";
	private static final String ODD_DAY_CHANGES = "odd_day_changes";

	private static final String MAX_LOOPS = "max_loops";
	private static final String INDEX_ADDRESS_CHANGES = "index_address_changes";
	private static final String INDEX_UPDATE_BALANCE = "index_update_balance";
	private static final String BALANCE_UPDATE_DB = "balance_update_db";
	private static final String ADDRESS_UPDATE_DB = "address_update_db";

	private static final String DIVIDENDS_SCORE = "dividends_score";
	private static final String BLACKLIST_ADDRESS = "blacklist_address";

	private static final String STAKED_BALANCES = "staked_balances";
	private static final String MINIMUM_STAKE = "minimum_stake";
	private static final String UNSTAKING_PERIOD = "unstaking_period";
	private static final String TOTAL_STAKED_BALANCE = "total_staked_balance";

	private static final String EVEN_DAY_STAKE_CHANGES = "even_day_stake_changes";
	private static final String ODD_DAY_STAKE_CHANGES = "odd_day_stake_changes";

	private static final String INDEX_STAKE_ADDRESS_CHANGES = "index_address_stake_changes";
	private static final String INDEX_UPDATE_STAKE = "index_update_stake";
	private static final String STAKE_UPDATE_DB = "stake_update_db";
	private static final String STAKE_ADDRESS_UPDATE_DB = "stake_address_update_db";

	private static final String STAKING_ENABLED = "staking_enabled";
	private static final String SWITCH_DIVS_TO_STAKED_TAP_ENABLED = "switch_divs_to_staked_tap";

	private static final String PAUSED = "paused";
	private static final String PAUSE_WHITELIST = "pause_whitelist";
	private static final String LOCKLIST = "locklist";

	//TODO:verify the usage of BigInteger
	private final VarDB<BigInteger> totalSupply = Context.newVarDB(TOTAL_SUPPLY, BigInteger.class);
	//this variable is defined as int in the icon samples
	private final VarDB<BigInteger> decimals = Context.newVarDB(DECIMALS, BigInteger.class);
	private final ArrayDB<Address> addresses = Context.newArrayDB(ADDRESSES, Address.class);

	private final DictDB<Address, BigInteger> balances = Context.newDictDB(BALANCES, BigInteger.class);

	private final ArrayDB<Address> evenDayChanges = Context.newArrayDB(EVEN_DAY_CHANGES, Address.class);
	private final ArrayDB<Address> oddDayChanges = Context.newArrayDB(ODD_DAY_CHANGES, Address.class);

	List<ArrayDB<Address>> changes = Arrays.asList(evenDayChanges, oddDayChanges);

	private final VarDB<BigInteger> maxLoop = Context.newVarDB(MAX_LOOPS, BigInteger.class);
	private final VarDB<BigInteger> indexUpdateBalance = Context.newVarDB(INDEX_UPDATE_BALANCE, BigInteger.class);
	private final VarDB<BigInteger> indexAddressChanges = Context.newVarDB(INDEX_ADDRESS_CHANGES, BigInteger.class);

	private final VarDB<BigInteger> balanceUpdateDb = Context.newVarDB(BALANCE_UPDATE_DB, BigInteger.class);
	private final VarDB<Integer> addressUpdateDb = Context.newVarDB(ADDRESS_UPDATE_DB, Integer.class);

	private final VarDB<Address> dividendsScore = Context.newVarDB(DIVIDENDS_SCORE, Address.class);
	private final ArrayDB<Address> blacklistAddress = Context.newArrayDB(BLACKLIST_ADDRESS, Address.class);

	//TODO : Example 2) Two-depth dict (test_dict2[‘key1’][‘key2’]): 
	//lets dig into how it is used self._STAKED_BALANCES, db, value_type=int, depth=2
	//looks like it is a List of Integer instead of Integer only
	private final DictDB<Address, BigInteger[]> stakedBalances = Context.newDictDB(STAKED_BALANCES, BigInteger[].class);
	private final VarDB<BigInteger> minimumStake = Context.newVarDB(MINIMUM_STAKE, BigInteger.class);
	private final VarDB<BigInteger> unstakingPeriod = Context.newVarDB(UNSTAKING_PERIOD, BigInteger.class);
	private final VarDB<BigInteger> totalStakedBalance = Context.newVarDB(TOTAL_STAKED_BALANCE, BigInteger.class);

	private final ArrayDB<Address> evenDayStakeChanges = Context.newArrayDB(EVEN_DAY_STAKE_CHANGES, Address.class);
	private final ArrayDB<Address> oddDayStakeChanges = Context.newArrayDB(ODD_DAY_STAKE_CHANGES, Address.class);

	List<ArrayDB<Address>> stakeChanges = Arrays.asList(evenDayStakeChanges, oddDayStakeChanges);

	private final VarDB<BigInteger> indexUpdateStake = Context.newVarDB(INDEX_UPDATE_STAKE, BigInteger.class);
	private final VarDB<BigInteger> indexStakeAddressChanges = Context.newVarDB(INDEX_STAKE_ADDRESS_CHANGES, BigInteger.class);

	// To choose between even and odd DBs
	private final VarDB<BigInteger> stakeUpdateDb = Context.newVarDB(STAKE_UPDATE_DB, BigInteger.class);

	//very tricky tricky and not usable var, it just store indexes for an array.
	private final VarDB<Integer> stakeAddressUpdateDb = Context.newVarDB(STAKE_ADDRESS_UPDATE_DB, Integer.class);

	private final VarDB<Boolean> stakingEnabled = Context.newVarDB(STAKING_ENABLED, Boolean.class);
	private final VarDB<Boolean> switchDivsToStakedTapEnabled = Context.newVarDB(SWITCH_DIVS_TO_STAKED_TAP_ENABLED, Boolean.class);

	// Pausing and locklist, whitelist implementations
	private final VarDB<Boolean> paused = Context.newVarDB(PAUSED, Boolean.class);
	private final ArrayDB<Address> pauseWhitelist = Context.newArrayDB(PAUSE_WHITELIST, Address.class);
	private final ArrayDB<Address> locklist = Context.newArrayDB(LOCKLIST, Address.class);

	@Override
	@EventLog(indexed=3)
	public void Transfer( Address from, Address to, BigInteger value, byte[] data) {}

	@EventLog(indexed=1)
	protected void LocklistAddress(Address address, String note) {}

	@EventLog(indexed=1)
	protected void WhitelistAddress(Address address, String note){}

	@EventLog(indexed=1)
	protected void BlacklistAddress(Address address, String note){}


	//TODO: looks like this method can be moved into the constructor
	//def on_install(self, _initialSupply: int, _decimals: int) -> None:

	//TODO: not sure where it should live, perhaps we should test the update scenario locally and see what happens
	//from py docs: Invoked when the contract is deployed for update This is the place where you migrate old states.
	//def on_update(self) -> None:

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
		if (Context.getOrigin().equals(Context.getOwner()))
			Context.revert("Only the owner can call the untether method.");
	}

	@Override
	@External(readonly=true)
	public String name() {
		return "TapToken";
	}

	@Override
	@External(readonly=true)
	public String symbol() {
		return "TAP";
	}

	@Override
	@External(readonly=true)
	public int decimals() {
		return this.decimals
				.getOrDefault(BigInteger.ZERO)
				.intValue();
	}

	@Override
	@External(readonly=true)
	public BigInteger totalSupply() {
		return this.totalSupply.get();
	}

	@Override
	@External(readonly=true)
	public BigInteger balanceOf(Address owner) {
		return this.balances.getOrDefault(owner, BigInteger.ZERO);
	}

	@External(readonly=true)
	public BigInteger availableBalanceOf(Address owner) {
		var detailBalance = detailsBalanceOf(owner);
		return detailBalance.getOrDefault("Available balance", BigInteger.ZERO);
	}

	@External(readonly=true)
	public BigInteger stakedBalanceOf(Address owner) {
		return this.stakedBalances
				.getOrDefault(owner, Status.EMPTY_STATUS_ARRAY)
				[Status.STAKED];
	}

	@External(readonly=true)
	public BigInteger unstakedBalanceOf(Address owner) {
		Map<String, BigInteger> detailBalance = detailsBalanceOf(owner);
		return detailBalance.get("Unstaking balance");
	}

	@External(readonly=true)
	public BigInteger totalStakedBalance() {
		return this.totalStakedBalance.get();
	}

	@External(readonly=true)
	public Boolean stakingEnabled() {
		return this.stakingEnabled.get();
	}

	@External(readonly=true)
	public Boolean switchDivsToStakedTapEnabled() {
		return this.switchDivsToStakedTapEnabled.get();
	}

	@External(readonly=true)
	public Boolean getPaused() {
		return this.paused.get();
	}

	//TODO:honor method name convention as snake
	@External(readonly=true)
	public Map<String, BigInteger> detailsBalanceOf(Address owner) {

		//Context.getBlockTimestamp() -- > self.now()
		BigInteger currUnstaked = BigInteger.ZERO;
		BigInteger[] sb = this.stakedBalances.getOrDefault(owner, Status.EMPTY_STATUS_ARRAY);
		if ( sb[Status.UNSTAKING_PERIOD].compareTo( BigInteger.valueOf(Context.getBlockTimestamp())) < 0 ) {
			currUnstaked = sb[Status.UNSTAKING];
		}

		BigInteger availableBalance;
		if (this.firstTime(owner)) {
			availableBalance = this.balanceOf(owner);
		}else {
			availableBalance = sb[Status.AVAILABLE];
		}

		//possible negative value scenario?
		BigInteger unstakingAmount = sb[Status.UNSTAKING].subtract(currUnstaked);

		BigInteger unstakingTime = BigInteger.ZERO;
		if (unstakingAmount.compareTo(BigInteger.ZERO) != 0) {
			unstakingTime = sb[Status.UNSTAKING_PERIOD];
		}

		Map<String, BigInteger> map = new HashMap<>();

		map.put("Total balance", this.balances.get(owner));
		map.put("Available balance", availableBalance.add( currUnstaked) );
		map.put("Staked balance", sb[Status.STAKED]);
		map.put("Unstaking balance", unstakingAmount);
		map.put("Unstaking time (in microseconds)", unstakingTime);

		return map;
	}

	private Boolean firstTime(Address from) {
		BigInteger[] sb = this.stakedBalances.getOrDefault(from, Status.EMPTY_STATUS_ARRAY);
		return
				sb[Status.AVAILABLE].compareTo(BigInteger.ZERO) == 0
				&& sb[Status.STAKED].compareTo(BigInteger.ZERO) == 0
				&& sb[Status.UNSTAKING].compareTo(BigInteger.ZERO) == 0
				&& this.balances.getOrDefault(from, BigInteger.ZERO).compareTo(BigInteger.ZERO) != 0;
	}

	private void checkFirstTime(Address from) {
		//If first time copy the balance to available staked balances
		if (this.firstTime(from)){
			this.stakedBalances.getOrDefault(from, Status.EMPTY_STATUS_ARRAY)[Status.AVAILABLE] = this.balances.get(from);
		}
	}

	private void stakingEnabledOnly() {
		if (! this.stakingEnabled.getOrDefault(false)) {
			Context.revert("Staking must first be enabled.");
		}
	}

	private void switchDivsToStakedTapEnabledOnly() {
		if (! this.switchDivsToStakedTapEnabled.getOrDefault(false)) {
			Context.revert("Switching to dividends for staked tap has to be enabled.");
		}
	}

	@External
	public void toggleStakingEnabled() {
		this.ownerOnly();
		this.stakingEnabled.set(! this.stakingEnabled.getOrDefault(false));
	}

	@External
	public void toggleSwitchDivsToStakedTapEnabled() {
		this.ownerOnly();
		this.switchDivsToStakedTapEnabled.set(! this.switchDivsToStakedTapEnabled.getOrDefault(false));
	}

	@External
	public void togglePaused() {
		this.ownerOnly();
		this.paused.set(! this.paused.getOrDefault(false));
	}

	@External
	public void stake(BigInteger value) {
		this.stakingEnabledOnly();

		//TODO: caller or Origin?
		Address from = Context.getCaller();
		if( value == null) {
			Context.revert("Staked TAP value can't be less than zero");
		}
		if (value.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Staked TAP value can't be less than zero");
		}

		if (value.compareTo(
				this.balances.getOrDefault(from, BigInteger.ZERO) ) > 0 ) {
			Context.revert("Out of TAP balance");
		}

		if (value.compareTo(this.minimumStake.getOrDefault(BigInteger.ZERO)) < 0
				&& value.compareTo(BigInteger.ZERO) != 0) {
			Context.revert("Staked TAP must be greater than the minimum stake amount and non zero");
		}
		this.checkFirstTime(from);
		// Check if the unstaking period has already been reached.
		this.makeAvailable(from);

		for(int i = 0; i < this.locklist.size(); i++ ) {
			if ( this.locklist.get(i).equals(from)) {
				Context.revert("Locked address not permitted to stake.");
			}
		}

		BigInteger[] sb = this.stakedBalances.getOrDefault(from, Status.EMPTY_STATUS_ARRAY);
		BigInteger oldStake = sb[Status.STAKED].add( sb[Status.UNSTAKING]);
		//big integer is immutable, not need this next line
		BigInteger newStake = value;

		BigInteger stakeIncrement = value.subtract( sb[Status.STAKED]);
		BigInteger unstakeAmount = BigInteger.ZERO;
		if (newStake.compareTo(oldStake) > 0 ) {
			BigInteger offset = newStake.subtract(oldStake);
			sb[Status.AVAILABLE] = sb[Status.AVAILABLE].subtract(offset);
		}else {
			unstakeAmount = oldStake.subtract(newStake);
		}

		sb[Status.STAKED] = value;
		sb[Status.UNSTAKING] = unstakeAmount;
		sb[Status.UNSTAKING_PERIOD] = BigInteger.valueOf( Context.getBlockTimestamp())
				.add( this.unstakingPeriod.getOrDefault(BigInteger.ZERO));
		this.totalStakedBalance.set(this.totalStakedBalance.getOrDefault(BigInteger.ZERO).add(stakeIncrement));

		ArrayDB<Address> stakeAddressChanges = this.stakeChanges.get(this.stakeAddressUpdateDb.getOrDefault(0));
		stakeAddressChanges.add(from);
	}

	@Override
	@External
	public void transfer(Address to, BigInteger value, byte[] data) {
		//TODO: review all the loops that are use for searching
		//create a util method for this section of code.
		boolean found = false;
		for(int i = 0; i< this.pauseWhitelist.size(); i++) {
			if(this.pauseWhitelist.get(i) != null 
					&& this.pauseWhitelist.get(i).equals(Context.getCaller())) {
				found = true;
				break;
			}
		}

		if(this.paused.get() && !found) {
			Context.revert("TAP token transfers are paused.");
		}

		found = false;
		for(int i = 0; i< this.pauseWhitelist.size(); i++) {
			if(this.locklist.get(i) != null 
					&& this.locklist.get(i).equals(Context.getCaller())) {
				found = true;
				break;
			}
		}

		if (found) {
			Context.revert("Transfer of TAP has been locked for this address.");
		}

		if (data == null || data.length == 0) {
			data = "None".getBytes();
		}
		this._transfer(Context.getCaller(), to, value, data);
	}

	private void _transfer(Address from, Address to, BigInteger value, byte[] data) {

		// Checks the sending value and balance.
		if (value == null || value.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Transferring value cannot be less than zero");
		}

		BigInteger balanceFrom = this.balances.get(from);
		BigInteger balanceTo = this.balances.get(to);
		if ( balanceFrom.compareTo(value) < 0) {
			Context.revert("Out of balance");
		}
		this.checkFirstTime(from);
		this.checkFirstTime(to);
		this.makeAvailable(to);
		this.makeAvailable(from);

		BigInteger[] sbFrom = this.stakedBalances.get(from);
		BigInteger[] sbTo = this.stakedBalances.get(to);
		if (sbFrom[Status.AVAILABLE].compareTo(value) < 0 ) {
			Context.revert("Out of available balance");
		}

		balanceFrom = balanceFrom.subtract(value);
		balanceTo = balanceTo.add(value);

		sbFrom[Status.AVAILABLE] = (sbFrom[Status.AVAILABLE].subtract(value));
		sbTo[Status.AVAILABLE] = (sbTo[Status.AVAILABLE].add(value));

		if ( !containsInArrayDb(to, this.addresses ) ){
			this.addresses.add(to);
		}
		if (to.isContract()){
			// If the recipient is SCORE,
			//   then calls `tokenFallback` to hand over control.
			Context.call(to, "tokenFallback", from, value, data);
		}

		// Emits an event log `Transfer`
		this.Transfer(from, to, value, data);
		if ( !this.switchDivsToStakedTapEnabled.getOrDefault(false) ){
			ArrayDB<Address> addressChanges = this.changes.get(this.addressUpdateDb.getOrDefault(0));
			if ( !containsInArrayDb(from, this.blacklistAddress) ) {
				addressChanges.add(from);
			}
			if ( ! containsInArrayDb(to, this.blacklistAddress) ){
				addressChanges.add(to);
			}
		}

		Context.println("Transfer({"+from+"}, {"+to+"}, {"+value+"}, {"+data+")"+ TAG);

	}

	private void ownerOnly() {
		//TODO: there is a method called Context.getOrigin()
		//this method works for first time call, test this scenario
		if (!Context.getCaller().equals(Context.getOwner())) {
			Context.revert("Only owner can call this method");
		}
	}

	private void dividendsOnly() {
		if ( ! Context.getCaller().equals(this.dividendsScore.getOrDefault(ZERO_ADDRESS)) ) {
			Context.revert("This method can only be called by the dividends distribution contract");
		}
	}

	private void makeAvailable(Address from) {
		// Check if the unstaking period has already been reached.
		BigInteger[] sb = this.stakedBalances.getOrDefault(from, Status.EMPTY_STATUS_ARRAY);
		if ( sb[Status.UNSTAKING_PERIOD].compareTo( BigInteger.valueOf(Context.getBlockTimestamp()) ) <= 0 ) {
			BigInteger currUnstaked = sb[Status.UNSTAKING];
			sb[Status.UNSTAKING] = BigInteger.ZERO;
			sb[Status.AVAILABLE] = sb[Status.AVAILABLE].add(currUnstaked);
		}
	}

	@External
	public void set_minimum_stake(BigInteger amount) {
		/*
        Set the minimum stake amount
        :param _amount: Minimum amount of stake needed.
		 */
		this.ownerOnly();
		if ( amount == null || amount.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Amount cannot be less than zero");
		}

		//TODO: verify this operation
		BigInteger totalAmount = amount.pow(this.decimals.getOrDefault(BigInteger.ONE).intValue());
		this.minimumStake.set(totalAmount);
	}


	@External
	public void set_unstaking_period(BigInteger time){
		/*
        Set the minimum staking period
        :param _time: Staking time period in days.
		 */

		this.ownerOnly();
		if (time == null || time.compareTo(BigInteger.ZERO) < 0 ) {
			Context.revert("Time cannot be negative.");
		}
		BigInteger totalTime = time.multiply( BigInteger.valueOf(DAY_TO_MICROSECOND));  // convert days to microseconds
		this.unstakingPeriod.set(totalTime);
	}

	@External
	public void set_max_loop(BigInteger loops) {
		/*
        Set the maximum number a for loop can run for any operation
        :param _loops: Maximum number of for loops allowed
        :return:
		 */
		if(loops == null) {
			loops = BigInteger.valueOf(100L);
		}
		this.ownerOnly();
		this.maxLoop.set(loops);
	}

	@External(readonly=true)
	public BigInteger get_minimum_stake() {
		/*
        Returns the minimum stake amount
		 */
		return this.minimumStake.get();
	}

	@External(readonly=true)
	public BigInteger get_unstaking_period(){
		/*
        Returns the minimum staking period in days
		 */
		BigInteger timeInMicroseconds = this.unstakingPeriod.get();
		BigInteger timeInDays = timeInMicroseconds; //TODO: there was a bug? // DAY_TO_MICROSECOND
		return timeInDays;
	}

	@External(readonly=true)
	public BigInteger get_max_loop() {
		/*
        Returns the maximum number of for loops allowed in the score
        :return:
		 */
		return this.maxLoop.get();
	}

	//TODO: variable names affects the contract? probably yes, preserve them
	@External
	public void set_dividends_score(Address _score) {
		/*
        Sets the dividends score address. The function can only be invoked by score owner.
        :param _score: Score address of the dividends contract
        :type _score: :class:`iconservice.base.address.Address`
		 */
		this.ownerOnly();
		this.dividendsScore.set(_score);
	}

	@External(readonly=true)
	public Address get_dividends_score() {
		/*
         Returns the roulette score address.
        :return: Address of the roulette score
        :rtype: :class:`iconservice.base.address.Address`
		 */
		return this.dividendsScore.get();
	}

	@External
	public Map<String, BigInteger> get_balance_updates() {
		/*
        Returns the updated addresses and their balances for today. Returns empty dictionary if the updates has
        completed
        :return: Dictionary contains the addresses and their updated balances. Maximum number of addresses
        and balances returned is defined by the max_loop
		 */
		this.dividendsOnly();
		ArrayDB<Address> balanceChanges = this.changes.get(this.balanceUpdateDb.getOrDefault(BigInteger.ZERO).intValue());

		int lengthList = balanceChanges.size();

		int start = this.indexUpdateBalance.getOrDefault(BigInteger.ZERO).intValue();
		if (start == lengthList){
			if (this.balanceUpdateDb.getOrDefault(BigInteger.ZERO).intValue() != this.addressUpdateDb.getOrDefault(0) ) {
				this.balanceUpdateDb.set(BigInteger.valueOf(this.addressUpdateDb.get()));
				this.indexUpdateBalance.set(this.indexAddressChanges.getOrDefault(BigInteger.ZERO));
			}
			return new HashMap<>();
		}

		int end = Math.min(start + this.maxLoop.getOrDefault(BigInteger.ZERO).intValue(), lengthList);

		Map<String, BigInteger> balances = new HashMap<>();
		for(int i = start; i< end; i++) {
			balances.put(balanceChanges.get(i).toString(), this.balances.get(balanceChanges.get(i)));
		}

		this.indexUpdateBalance.set(BigInteger.valueOf(end));
		return balances;
	}

	@External
	public Boolean clear_yesterdays_changes() {
		/*
        Clears the array db storing yesterday's changes
        :return: True if the array has been emptied
		 */
		this.dividendsOnly();
		int yesterday = (this.addressUpdateDb.getOrDefault(0).intValue() + 1) % 2;
		ArrayDB<Address> yesterdaysChanges = this.changes.get(yesterday);
		int lengthList = yesterdaysChanges.size();
		if (lengthList == 0) {
			return true;
		}

		int loopCount = Math.min(lengthList, this.maxLoop.getOrDefault(BigInteger.ZERO).intValue());
		for (int i= 0; i<loopCount; i++) {
			yesterdaysChanges.pop();
		}

		return yesterdaysChanges.size() <= 0;
	}

	@External(readonly=true)
	public List<Address> get_blacklist_addresses() {
		/*
        Returns all the blacklisted addresses(rewards score address and devs team address)
        :return: List of blacklisted address
        :rtype: list
		 */
		List<Address> addressList = new ArrayList<>();

		for (int i=0; i< this.blacklistAddress.size(); i++) {
			addressList.add(this.blacklistAddress.get(i));
		}
		return addressList;
	}

	@External
	public void remove_from_blacklist(Address _address) {
		/*
        Removes the address from blacklist.
        Only owner can remove the blacklist address
        :param _address: Address to be removed from blacklist
        :type _address: :class:`iconservice.base.address.Address`
        :return:
		 */
		if (Context.getCaller().equals(Context.getOwner()) ){
			if ( !containsInArrayDb(_address, this.blacklistAddress) ){
				//TODO: check if toString produces a s;tring representation or a java object string
				Context.revert(_address + " not in blacklist address");
			}
			this.BlacklistAddress(_address, "Removed from blacklist");
			Address top = this.blacklistAddress.pop();

			if ( top != null && !top.equals(_address) ) {
				for (int i = 0; i< this.blacklistAddress.size(); i++ ) {
					if (this.blacklistAddress.get(i).equals(_address)) {
						this.blacklistAddress.set(i, top);
					}
				}
			}
		}
	}

	@External
	public void set_blacklist_address(Address _address) {
		/*
        The provided address is set as blacklist address and will be excluded from TAP dividends.
        Only the owner can set the blacklist address
        :param _address: Address to be included in the blacklist
        :type _address: :class:`iconservice.base.address.Address`
        :return:
		 */
		if ( Context.getCaller().equals(Context.getOwner())) {
			this.BlacklistAddress(_address, "Added to Blacklist");
			if ( !containsInArrayDb(_address , this.blacklistAddress) ){
				this.blacklistAddress.add(_address);
			}
		}
	}

	@External
	public void switch_address_update_db() {
		/*
        Switches the day when the distribution has to be started
        :return:
		 */
		this.dividendsOnly();
		int newDay = (this.addressUpdateDb.getOrDefault(0) + 1) % 2;
		this.addressUpdateDb.set(newDay);
		ArrayDB<Address> addressChanges = this.changes.get(newDay);
		this.indexAddressChanges.set(BigInteger.valueOf(addressChanges.size()));
	}

	@External
	public Map<String, BigInteger> get_stake_updates() {
		/*
        Returns the updated addresses. Returns empty dictionary if the updates has
        completed
        :return: Dictionary contains the addresses. Maximum number of addresses
        and balances returned is defined by the max_loop
		 */
		this.dividendsOnly();
		this.stakingEnabledOnly();
		this.switchDivsToStakedTapEnabledOnly();

		ArrayDB<Address> stakeChanges = this.stakeChanges.get(this.stakeUpdateDb.getOrDefault(BigInteger.ZERO).intValue());
		int lengthList = stakeChanges.size();

		int start = this.indexUpdateStake.getOrDefault(BigInteger.ZERO).intValue();
		if (start == lengthList) {
			if (this.stakeUpdateDb.getOrDefault(BigInteger.ZERO).intValue() != 
					this.stakeAddressUpdateDb.getOrDefault(0)) {
				this.stakeUpdateDb.set(BigInteger.valueOf(this.stakeAddressUpdateDb.getOrDefault(0)));
				this.indexUpdateStake.set(this.indexStakeAddressChanges.getOrDefault(BigInteger.ZERO));
			}
			return new HashMap<>();
		}
		int end = Math.min(start + this.maxLoop.getOrDefault(BigInteger.ZERO).intValue(), lengthList);

		Map<String, BigInteger> detailedBalances = new HashMap<>();
		for(int i=start; i< end; i++) {
			detailedBalances.put( stakeChanges.get(i).toString(),  this.stakedBalanceOf(stakeChanges.get(i)));
		}
		this.indexUpdateStake.set(BigInteger.valueOf(end));
		return detailedBalances;
	}

	@External
	public Boolean clear_yesterdays_stake_changes() {
		this.stakingEnabledOnly();
		this.switchDivsToStakedTapEnabledOnly();
		this.dividendsOnly();
		int yesterday = (this.stakeAddressUpdateDb.get() + 1) % 2;
		ArrayDB<Address> yesterdaysChanges = this.stakeChanges.get(yesterday);
		int lengthList = yesterdaysChanges.size();
		if (lengthList == 0) {
			return true;
		}
		int loopCount = Math.min(lengthList, this.maxLoop.getOrDefault(BigInteger.ZERO).intValue());
		for (int i=0 ;i < loopCount; i++) {
			yesterdaysChanges.pop();
		}
		return yesterdaysChanges.size() <= 0;
	}

	@External
	public void switch_stake_update_db() {
		this.dividendsOnly();
		this.stakingEnabledOnly();
		this.switchDivsToStakedTapEnabledOnly();

		int newDay = (this.stakeAddressUpdateDb.getOrDefault(0) + 1) % 2;
		this.stakeAddressUpdateDb.set(newDay);
		ArrayDB<Address> stakeChanges = this.stakeChanges.get(newDay);
		this.indexStakeAddressChanges.set(BigInteger.valueOf(stakeChanges.size()));
	}

	@External(readonly=true)
	public List<Address> get_locklist_addresses() {
		/*
        Returns all locked addresses.
        :return: List of locked addresses
        :rtype: list
		 */
		List<Address> addressList = new ArrayList<>();
		for (int i=0; i< this.locklist.size(); i++) {
			addressList.add(locklist.get(i));
		}
		return addressList;
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

}
