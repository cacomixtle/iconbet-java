package com.iconbet.score.tap;

import java.math.BigInteger;
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
		if (value.compareTo(BigInteger.ZERO) < 0) {
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

		boolean found = false;
		for(int i = 0; i< this.pauseWhitelist.size(); i++) {
			if(this.pauseWhitelist.get(i) != null 
					&& this.pauseWhitelist.get(i).equals(Context.getCaller())) {
				found = true;
				break;
			}
		}

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
		
		//can we replace Logger with Context.println(); ? 
		//remove logger lines
		//Logger.debug(f"Transfer({_from}, {_to}, {_value}, {_data})", TAG)
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
