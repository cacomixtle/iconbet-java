package com.iconbet.score.tap;

import static java.math.BigInteger.ZERO;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;

import com.iconloop.score.token.irc2.IRC2;

import score.Address;
import score.ArrayDB;
import score.BranchDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;

public class TapToken implements IRC2{
	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "TapToken";
	//TODO: verify this value exists as long and not as biginteger
	private static final long DAY_TO_MICROSECOND = 86_400_000_000l;
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

	private List<ArrayDB<Address>> changes = List.of(evenDayChanges, oddDayChanges);

	private final VarDB<BigInteger> maxLoop = Context.newVarDB(MAX_LOOPS, BigInteger.class);
	private final VarDB<BigInteger> indexUpdateBalance = Context.newVarDB(INDEX_UPDATE_BALANCE, BigInteger.class);
	private final VarDB<BigInteger> indexAddressChanges = Context.newVarDB(INDEX_ADDRESS_CHANGES, BigInteger.class);

	private final VarDB<BigInteger> balanceUpdateDb = Context.newVarDB(BALANCE_UPDATE_DB, BigInteger.class);
	private final VarDB<Integer> addressUpdateDb = Context.newVarDB(ADDRESS_UPDATE_DB, Integer.class);

	private final VarDB<Address> dividendsScore = Context.newVarDB(DIVIDENDS_SCORE, Address.class);
	private final ArrayDB<Address> blacklistAddress = Context.newArrayDB(BLACKLIST_ADDRESS, Address.class);

	//TODO : Example 2) Two-depth dict (test_dict2[‘key1’][‘key2’]): 
	//lets dig into how it is used self._STAKED_BALANCES, db, value_type=int, depth=2
	//verify if BranchDB can support multiples Dicdb keys like thousands, else we will need to go back to old impl or even a tricky data structure impl
	private final BranchDB<Address, DictDB<Integer,BigInteger>> stakedBalances = Context.newBranchDB(STAKED_BALANCES, BigInteger.class);
	private final VarDB<BigInteger> minimumStake = Context.newVarDB(MINIMUM_STAKE, BigInteger.class);
	private final VarDB<BigInteger> unstakingPeriod = Context.newVarDB(UNSTAKING_PERIOD, BigInteger.class);
	private final VarDB<BigInteger> totalStakedBalance = Context.newVarDB(TOTAL_STAKED_BALANCE, BigInteger.class);

	private final ArrayDB<Address> evenDayStakeChanges = Context.newArrayDB(EVEN_DAY_STAKE_CHANGES, Address.class);
	private final ArrayDB<Address> oddDayStakeChanges = Context.newArrayDB(ODD_DAY_STAKE_CHANGES, Address.class);

	private List<ArrayDB<Address>> stakeChanges  = List.of(evenDayStakeChanges, oddDayStakeChanges);

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

	public TapToken(BigInteger _initialSupply, BigInteger _decimals) {
		if (_initialSupply == null || _initialSupply.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Initial supply cannot be less than zero");
		}

		if (_decimals == null || _decimals.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Decimals cannot be less than zero");
		}

		//TODO: make sure iconbet do not want decimals a biginteger like 2^2147483647 decimals
		BigInteger totalSupply = _initialSupply.multiply( pow( BigInteger.TEN , _decimals.intValue()) );
		Context.println(TAG+" : total_supply "+ totalSupply );

		this.totalSupply.set(totalSupply);
		this.decimals.set(_decimals);
		this.balances.set(Context.getOwner(), totalSupply);
		this.addresses.add(Context.getOwner());

	}

	@Override
	@EventLog(indexed=3)
	public void Transfer( Address from, Address to, BigInteger value, byte[] data) {}

	@EventLog(indexed=1)
	protected void LocklistAddress(Address address, String note) {}

	@EventLog(indexed=1)
	protected void WhitelistAddress(Address address, String note){}

	@EventLog(indexed=1)
	protected void BlacklistAddress(Address address, String note){}

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
		return TAG;
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
		return this.totalSupply.getOrDefault(BigInteger.ZERO);
	}

	@Override
	@External(readonly=true)
	public BigInteger balanceOf(Address _owner) {
		return this.balances.getOrDefault(_owner, BigInteger.ZERO);
	}

	@External(readonly=true)
	public BigInteger available_balance_of(Address _owner) {
		var detailBalance = details_balanceOf(_owner);
		if(detailBalance.containsKey("Available balance")) {
			return detailBalance.get("Available balance");
		}else {
			return BigInteger.ZERO;
		}
	}

	@External(readonly=true)
	public BigInteger staked_balanceOf(Address _owner) {
		return this.stakedBalances.at(_owner).getOrDefault(Status.STAKED, BigInteger.ZERO);
	}

	@External(readonly=true)
	public BigInteger unstaked_balanceOf(Address _owner) {
		Map<String, BigInteger> detailBalance = details_balanceOf(_owner);
		if(detailBalance.containsKey("Unstaking balance")) {
			return detailBalance.get("Unstaking balance");
		}else {
			return BigInteger.ZERO;
		}
	}

	@External(readonly=true)
	public BigInteger total_staked_balance() {
		return this.totalStakedBalance.getOrDefault(BigInteger.ZERO);
	}

	@External(readonly=true)
	public boolean staking_enabled() {
		return this.stakingEnabled.getOrDefault(false);
	}

	@External(readonly=true)
	public boolean switch_divs_to_staked_tap_enabled() {
		return this.switchDivsToStakedTapEnabled.getOrDefault(false);
	}

	@External(readonly=true)
	public boolean getPaused() {
		return this.paused.getOrDefault(false);
	}

	//TODO:honor method name convention as snake
	@External(readonly=true)
	public Map<String, BigInteger> details_balanceOf(Address _owner) {

		//Context.getBlockTimestamp() -- > self.now()
		BigInteger currUnstaked = BigInteger.ZERO;
		DictDB<Integer, BigInteger> sb = this.stakedBalances.at(_owner);
		if (sb.getOrDefault(Status.UNSTAKING_PERIOD, ZERO).compareTo( BigInteger.valueOf(Context.getBlockTimestamp())) < 0 ) {
			currUnstaked = sb.getOrDefault(Status.UNSTAKING, ZERO);
		}

		BigInteger availableBalance;
		if (this.firstTime(_owner)) {
			availableBalance = this.balanceOf(_owner);
		}else{
			availableBalance = sb.getOrDefault(Status.AVAILABLE, ZERO);
		}

		//TODO: possible negative value scenario in py?
		BigInteger unstakingAmount = sb.getOrDefault(Status.UNSTAKING, ZERO);
		if( unstakingAmount.compareTo(ZERO) > 0) {
			unstakingAmount = unstakingAmount.subtract(currUnstaked);
		}

		BigInteger unstakingTime = BigInteger.ZERO;
		if ( !unstakingAmount.equals(ZERO)) {
			unstakingTime = sb.getOrDefault(Status.UNSTAKING_PERIOD, ZERO);
		}

		return Map.of(
				"Total balance", this.balances.getOrDefault(_owner, BigInteger.ZERO),
				"Available balance", availableBalance.add(currUnstaked),
				"Staked balance", sb.getOrDefault(Status.STAKED, ZERO),
				"Unstaking balance", unstakingAmount,
				"Unstaking time (in microseconds)", unstakingTime);

	}

	private boolean firstTime(Address from) {
		DictDB<Integer, BigInteger> sb = this.stakedBalances.at(from);
		return
				ZERO.equals( sb.getOrDefault(Status.AVAILABLE, ZERO))
				&& ZERO.equals( sb.getOrDefault(Status.STAKED, ZERO))
				&& ZERO.equals(sb.getOrDefault(Status.UNSTAKING, ZERO))
				&& this.balances.getOrDefault(from, BigInteger.ZERO).compareTo(BigInteger.ZERO) != 0;
	}

	private void checkFirstTime(Address from) {
		//If first time copy the balance to available staked balances
		if (this.firstTime(from)){
			this.stakedBalances.at(from).set(Status.AVAILABLE, this.balances.getOrDefault(from, BigInteger.ZERO));
		}
	}

	private void stakingEnabledOnly() {
		Context.println("staking enabled? : " + this.stakingEnabled.get());
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
	public void toggle_staking_enabled() {
		this.ownerOnly();
		this.stakingEnabled.set(! this.stakingEnabled.getOrDefault(false));
		Context.println("enabled staking?: " + this.stakingEnabled.get());
	}

	@External
	public void toggle_switch_divs_to_staked_tap_enabled() {
		this.ownerOnly();
		this.switchDivsToStakedTapEnabled.set(! this.switchDivsToStakedTapEnabled.getOrDefault(false));
	}

	@External
	public void togglePaused() {
		this.ownerOnly();
		this.paused.set(! this.paused.getOrDefault(false));
	}

	@External
	public void stake(BigInteger _value) {
		this.stakingEnabledOnly();

		Address from = Context.getCaller();
		if( _value == null) {
			Context.revert("Staked TAP value can't be less than zero");
		}
		if (_value.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Staked TAP value can't be less than zero");
		}

		if (_value.compareTo(
				this.balances.getOrDefault(from, BigInteger.ZERO) ) > 0 ) {
			Context.revert("Out of TAP balance");
		}

		if (_value.compareTo(this.minimumStake.getOrDefault(BigInteger.ZERO)) < 0
				&& _value.compareTo(BigInteger.ZERO) != 0) {
			Context.revert("Staked TAP must be greater than the minimum stake amount and non zero");
		}
		this.checkFirstTime(from);
		// Check if the unstaking period has already been reached.
		this.makeAvailable(from);

		if( containsInArrayDb(from, locklist)) {
			Context.revert("Locked address not permitted to stake.");
		}

		DictDB<Integer, BigInteger> sb = this.stakedBalances.at(from);
		BigInteger oldStake = sb.getOrDefault(Status.STAKED, ZERO).add( sb.get(Status.UNSTAKING));
		//big integer is immutable, not need this next line
		BigInteger newStake = _value;

		BigInteger stakeIncrement = _value.subtract( sb.getOrDefault(Status.STAKED, ZERO));
		BigInteger unstakeAmount = BigInteger.ZERO;
		if (newStake.compareTo(oldStake) > 0 ) {
			BigInteger offset = newStake.subtract(oldStake);
			sb.set(Status.AVAILABLE, sb.get(Status.AVAILABLE).subtract(offset));
		}else {
			unstakeAmount = oldStake.subtract(newStake);
		}

		sb.set(Status.STAKED, _value);
		sb.set(Status.UNSTAKING, unstakeAmount);
		sb.set(Status.UNSTAKING_PERIOD, BigInteger.valueOf( Context.getBlockTimestamp())
				.add( this.unstakingPeriod.getOrDefault(BigInteger.ZERO)));
		this.totalStakedBalance.set(this.totalStakedBalance.getOrDefault(BigInteger.ZERO).add(stakeIncrement));

		ArrayDB<Address> stakeAddressChanges = this.stakeChanges.get(this.stakeAddressUpdateDb.getOrDefault(0));
		stakeAddressChanges.add(from);
	}

	@Override
	@External
	public void transfer(Address _to, BigInteger _value, byte[] _data) {
		//TODO: review all the loops that are use for searching

		boolean found = containsInArrayDb(Context.getCaller(), this.pauseWhitelist);
		if(this.paused.getOrDefault(false) && !found) {
			Context.revert("TAP token transfers are paused.");
		}

		found = containsInArrayDb(Context.getCaller(), locklist);
		if (found) {
			Context.revert("Transfer of TAP has been locked for this address.");
		}

		if (_data == null || _data.length == 0) {
			_data = "None".getBytes();
		}
		this._transfer(Context.getCaller(), _to, _value, _data);
	}

	private void _transfer(Address from, Address to, BigInteger value, byte[] data) {

		// Checks the sending value and balance.
		if (value == null || value.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Transferring value cannot be less than zero");
		}

		BigInteger balanceFrom = this.balances.getOrDefault(from, BigInteger.ZERO);
		if ( balanceFrom.compareTo(value) < 0) {
			Context.revert("Out of balance");
		}

		this.checkFirstTime(from);
		this.checkFirstTime(to);
		this.makeAvailable(to);
		this.makeAvailable(from);

		DictDB<Integer, BigInteger> sbFrom = this.stakedBalances.at(from);
		DictDB<Integer, BigInteger> sbTo = this.stakedBalances.at(to);

		if (sbFrom.getOrDefault(Status.AVAILABLE, ZERO).compareTo(value) < 0 ) {
			Context.revert("Out of available balance");
		}

		this.balances.set(from, balanceFrom.subtract(value));

		BigInteger balanceTo = this.balances.getOrDefault(to, BigInteger.ZERO);
		this.balances.set(to, balanceTo.add(value));
		Context.println("new balance of 'to' ( "+ to +"): " + this.balances.get(to));

		sbFrom.set(Status.AVAILABLE, sbFrom.getOrDefault(Status.AVAILABLE, ZERO).subtract(value) );
		sbTo.set(Status.AVAILABLE, sbTo.getOrDefault(Status.AVAILABLE, ZERO).add(value));

		if ( !containsInArrayDb(to, this.addresses ) ){
			this.addresses.add(to);
		}

		if (to.isContract()){
			// If the recipient is SCORE,
			//   then calls `tokenFallback` to hand over control.
			Context.call(to, "tokenFallback", from, value, data);
		}

		// Emits an event log `Transfer`
		Context.println("Emit an event log Transfer from "+ from + " to " + to);
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

		DictDB<Integer, BigInteger> sb = this.stakedBalances.at(from);

		if ( sb.getOrDefault(Status.UNSTAKING_PERIOD, ZERO).compareTo( BigInteger.valueOf(Context.getBlockTimestamp()) ) <= 0 ) {
			BigInteger currUnstaked = sb.getOrDefault(Status.UNSTAKING, ZERO);
			sb.set(Status.UNSTAKING, BigInteger.ZERO);
			sb.set(Status.AVAILABLE, sb.getOrDefault(Status.AVAILABLE, ZERO).add(currUnstaked));
		}
	}

	@External
	public void set_minimum_stake(BigInteger _amount) {
		/*
        Set the minimum stake amount
        :param _amount: Minimum amount of stake needed.
		 */
		this.ownerOnly();
		if ( _amount == null || _amount.compareTo(BigInteger.ZERO) < 0) {
			Context.revert("Amount cannot be less than zero");
		}

		//TODO: verify this operation
		BigInteger totalAmount = pow(_amount , this.decimals.getOrDefault(BigInteger.ONE).intValue());
		this.minimumStake.set(totalAmount);
	}


	/*
    Set the minimum staking period
    :param _time: Staking time period in days.
	 */
	@External
	public void set_unstaking_period(BigInteger _time){

		this.ownerOnly();
		if (_time == null || _time.compareTo(BigInteger.ZERO) < 0 ) {
			Context.revert("Time cannot be negative.");
		}
		BigInteger totalTime = _time.multiply( BigInteger.valueOf(DAY_TO_MICROSECOND));  // convert days to microseconds
		this.unstakingPeriod.set(totalTime);
	}

	/*
    Set the maximum number a for loop can run for any operation
    :param _loops: Maximum number of for loops allowed
    :return:
	 */
	@External
	public void set_max_loop(BigInteger _loops) {
		if(_loops == null) {
			_loops = BigInteger.valueOf(100L);
		}
		this.ownerOnly();
		this.maxLoop.set(_loops);
	}

	/*
    Returns the minimum stake amount
	 */
	@External(readonly=true)
	public BigInteger get_minimum_stake() {
		return this.minimumStake.get();
	}

	/*
    Returns the minimum staking period in days
	 */
	@External(readonly=true)
	public BigInteger get_unstaking_period(){
		BigInteger timeInMicroseconds = this.unstakingPeriod.get();
		BigInteger timeInDays = timeInMicroseconds; //TODO: there was a bug? // DAY_TO_MICROSECOND
		return timeInDays;
	}

	/*
    Returns the maximum number of for loops allowed in the score
    :return:
	 */
	@External(readonly=true)
	public BigInteger get_max_loop() {
		return this.maxLoop.get();
	}

	/*
    Sets the dividends score address. The function can only be invoked by score owner.
    :param _score: Score address of the dividends contract
    :type _score: :class:`iconservice.base.address.Address`
	 */
	@External
	public void set_dividends_score(Address _score) {
		this.ownerOnly();
		this.dividendsScore.set(_score);
	}

	/*
    Returns the roulette score address.
   :return: Address of the roulette score
   :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_dividends_score() {
		return this.dividendsScore.get();
	}

	/*
    Returns the updated addresses and their balances for today. Returns empty dictionary if the updates has
    completed
    :return: Dictionary contains the addresses and their updated balances. Maximum number of addresses
    and balances returned is defined by the max_loop
	 */
	@External
	public Map<String, BigInteger> get_balance_updates() {
		this.dividendsOnly();
		ArrayDB<Address> balanceChanges = this.changes.get(this.balanceUpdateDb.getOrDefault(BigInteger.ZERO).intValue());

		int lengthList = balanceChanges.size();

		int start = this.indexUpdateBalance.getOrDefault(BigInteger.ZERO).intValue();
		if (start == lengthList){
			if (this.balanceUpdateDb.getOrDefault(BigInteger.ZERO).intValue() != this.addressUpdateDb.getOrDefault(0) ) {
				this.balanceUpdateDb.set(BigInteger.valueOf(this.addressUpdateDb.get()));
				this.indexUpdateBalance.set(this.indexAddressChanges.getOrDefault(BigInteger.ZERO));
			}
			return Map.of();
		}

		int end = Math.min(start + this.maxLoop.getOrDefault(BigInteger.ZERO).intValue(), lengthList);

		@SuppressWarnings("unchecked")
		Map.Entry<String, BigInteger>[] entries = new Map.Entry[end-start];
		//TODO: validate this logic
		int j = 0;
		for(int i=start; i< end; i++) {
			entries[j] = Map.entry(balanceChanges.get(i).toString(), this.balances.get(balanceChanges.get(i)));
			j++;
		}
		this.indexUpdateBalance.set(BigInteger.valueOf(end));
		return Map.ofEntries(entries);
	}

	/*
    Clears the array db storing yesterday's changes
    :return: True if the array has been emptied
	 */
	@External
	public boolean clear_yesterdays_changes() {
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

	/*
    Returns all the blacklisted addresses(rewards score address and devs team address)
    :return: List of blacklisted address
    :rtype: list
	 */
	@External(readonly=true)
	public List<Address> get_blacklist_addresses() {

		return arrayDbToList(this.blacklistAddress);
	}

	/*
    Removes the address from blacklist.
    Only owner can remove the blacklist address
    :param _address: Address to be removed from blacklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void remove_from_blacklist(Address _address) {

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

	/*
    The provided address is set as blacklist address and will be excluded from TAP dividends.
    Only the owner can set the blacklist address
    :param _address: Address to be included in the blacklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_blacklist_address(Address _address) {
		if ( Context.getCaller().equals(Context.getOwner())) {
			this.BlacklistAddress(_address, "Added to Blacklist");
			if ( !containsInArrayDb(_address , this.blacklistAddress) ){
				this.blacklistAddress.add(_address);
			}
		}
	}

	/*
    Switches the day when the distribution has to be started
    :return:
	 */
	@External
	public void switch_address_update_db() {
		this.dividendsOnly();
		int newDay = (this.addressUpdateDb.getOrDefault(0) + 1) % 2;
		this.addressUpdateDb.set(newDay);
		ArrayDB<Address> addressChanges = this.changes.get(newDay);
		this.indexAddressChanges.set(BigInteger.valueOf(addressChanges.size()));
	}

	/*
    Returns the updated addresses. Returns empty dictionary if the updates has
    completed
    :return: Dictionary contains the addresses. Maximum number of addresses
    and balances returned is defined by the max_loop
	 */
	@External
	public Map<String, BigInteger> get_stake_updates() {
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
			return Map.of();
		}
		int end = Math.min(start + this.maxLoop.getOrDefault(BigInteger.ZERO).intValue(), lengthList);

		@SuppressWarnings("unchecked")
		Map.Entry<String, BigInteger>[] entries = new Map.Entry[end-start];
		//TODO: validate this logic
		int j = 0;
		for(int i=start; i< end; i++) {
			entries[j] = Map.entry(stakeChanges.get(i).toString(),  this.staked_balanceOf(stakeChanges.get(i)));
			j++;
		}
		this.indexUpdateStake.set(BigInteger.valueOf(end));
		return Map.ofEntries(entries);
	}

	@External
	public boolean clear_yesterdays_stake_changes() {
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

	/*
    Returns all locked addresses.
    :return: List of locked addresses
    :rtype: list
	 */
	@External(readonly=true)
	public List<Address> get_locklist_addresses() {

		return arrayDbToList(this.locklist);
	}

	/*
    Removes the address from the locklist.
    Only owner can remove the locklist address
    :param _address: Address to be removed from locklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void remove_from_locklist(Address _address) {
		this.ownerOnly();
		if (!containsInArrayDb(_address, this.locklist)){
			Context.revert(_address+" not in locklist address");
		}
		this.LocklistAddress(_address, "Removed from Locklist");
		Address top = this.locklist.pop();
		if (!top.equals(_address)){
			for(int i=0; i< this.locklist.size(); i++) {
				if (this.locklist.get(i).equals(_address)){
					this.locklist.set(i, top);
				}
			}
		}
	}

	/*
    Add address to list of addresses that cannot transfer TAP.
    Only the owner can set the locklist address
    :param _address: Address to be included in the locklist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_locklist_address(Address _address) {
		this.ownerOnly();
		this.stakingEnabledOnly();

		this.LocklistAddress(_address, "Added to Locklist");
		if ( !containsInArrayDb(_address, this.locklist)) {
			this.locklist.add(_address);
		}

		// Unstake TAP of locklist address

		BigInteger stakedBalance = this.stakedBalances.at(_address).getOrDefault(Status.STAKED, ZERO);
		if (stakedBalance.compareTo(BigInteger.ZERO) > 0) {
			// Check if the unstaking period has already been reached.
			this.makeAvailable(_address);
			DictDB<Integer, BigInteger> sb = this.stakedBalances.at(_address);
			sb.set(Status.STAKED, BigInteger.ZERO);
			sb.set(Status.UNSTAKING, sb.get(Status.UNSTAKING).add(stakedBalance));
			sb.set(Status.UNSTAKING_PERIOD, this.unstakingPeriod.get().add(BigInteger.valueOf(Context.getBlockTimestamp())));
			this.totalStakedBalance.set( this.totalStakedBalance.getOrDefault(BigInteger.ZERO).subtract(stakedBalance ));
			ArrayDB<Address> stakeAddressChanges = this.stakeChanges.get(this.stakeAddressUpdateDb.get());
			stakeAddressChanges.add(_address);
		}
	}

	/*
    Returns all addresses whitelisted during pause.
    :return: List of whitelisted addresses
    :rtype: list
	 */
	@External(readonly=true)
	public List<Address> get_whitelist_addresses() {

		return arrayDbToList(this.pauseWhitelist);
	}

	/*
    Removes the address from whitelist.
    Only owner can remove the whitelist address
    :param _address: Address to be removed from whitelist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void remove_from_whitelist(Address _address) {
		this.ownerOnly();
		if (!containsInArrayDb(_address, this.pauseWhitelist)) {
			Context.revert(_address+ " not in whitelist address");
		}
		this.WhitelistAddress(_address, "Removed from whitelist");
		Address top = this.pauseWhitelist.pop();
		if (!top.equals(_address)) {
			for(int i=0; i< this.pauseWhitelist.size(); i++) {
				if (this.pauseWhitelist.get(i).equals(_address)) {
					this.pauseWhitelist.set(i,top);
				}
			}
		}
	}

	/*
    Add address to list of addresses exempt from transfer pause.
    Only the owner can set the whitelist address
    :param _address: Address to be included in the whitelist
    :type _address: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_whitelist_address(Address _address) {
		this.ownerOnly();
		this.WhitelistAddress(_address, "Added to Pause Whitelist");
		if ( !containsInArrayDb(_address, this.pauseWhitelist)) {
			this.pauseWhitelist.add(_address);
		}
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

	// BigInteger#pow() is not implemented in the shadow BigInteger.
	// we need to use our implementation for that.
	private static BigInteger pow(BigInteger base, int exponent) {
		BigInteger result = BigInteger.ONE;
		for (int i = 0; i < exponent; i++) {
			result = result.multiply(base);
		}
		return result;
	}

	private <T> List<T> arrayDbToList(ArrayDB<T> arraydb) {
		@SuppressWarnings("unchecked")
		T[] addressList = (T[])new Object[arraydb.size()];

		for (int i=0; i< arraydb.size(); i++) {
			addressList[i] = arraydb.get(i);
		}
		return List.of(addressList);
	}
}
