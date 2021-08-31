package com.iconbet.score.tap;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

import com.iconloop.score.token.irc2.IRC2;

import score.Address;
import score.ArrayDB;
import score.Context;
import score.DictDB;
import score.VarDB;
import score.annotation.EventLog;
import score.annotation.External;

public class TapToken implements IRC2{

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
    private final VarDB<BigInteger> addressUpdateDb = Context.newVarDB(ADDRESS_UPDATE_DB, BigInteger.class);

    private final VarDB<Address> dividendsScore = Context.newVarDB(DIVIDENDS_SCORE, Address.class);
    private final ArrayDB<Address> blacklistAddress = Context.newArrayDB(BLACKLIST_ADDRESS, Address.class);

    //TODO : Example 2) Two-depth dict (test_dict2[‘key1’][‘key2’]): 
    //lets dig into how it is used self._STAKED_BALANCES, db, value_type=int, depth=2
    //probably it is a List of Integer instead of Integer only
    private final DictDB<Address, Integer> stakedBalances = Context.newDictDB(STAKED_BALANCES, Integer.class);
    private final VarDB<BigInteger> minimumStake = Context.newVarDB(MINIMUM_STAKE, BigInteger.class);
    private final VarDB<BigInteger> unstakingPeriod = Context.newVarDB(UNSTAKING_PERIOD, BigInteger.class);
    private final VarDB<BigInteger> totalStakedBalance = Context.newVarDB(TOTAL_STAKED_BALANCE, BigInteger.class);

    private final ArrayDB<Address> evenDayStakeChanges = Context.newArrayDB(EVEN_DAY_STAKE_CHANGES, Address.class);
    private final ArrayDB<Address> oddDayStakeChanges = Context.newArrayDB(ODD_DAY_STAKE_CHANGES, Address.class);
    
    List<ArrayDB<Address>>  stakeChanges = Arrays.asList(evenDayStakeChanges, oddDayStakeChanges);

    private final VarDB<BigInteger> indexUpdateStake = Context.newVarDB(INDEX_UPDATE_STAKE, BigInteger.class);
    private final VarDB<BigInteger> indexStakeAddressChanges = Context.newVarDB(INDEX_STAKE_ADDRESS_CHANGES, BigInteger.class);

    // To choose between even and odd DBs
    private final VarDB<BigInteger> stakeUpdateDb = Context.newVarDB(STAKE_UPDATE_DB, BigInteger.class);
    private final VarDB<BigInteger> stakeAddressUpdateDb = Context.newVarDB(STAKE_ADDRESS_UPDATE_DB, BigInteger.class);

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
		return this.decimals.get().intValue();
	}

	@Override
	@External(readonly=true)
	public BigInteger totalSupply() {
		return this.totalSupply.get();
	}

	@Override
	@External(readonly=true)
	public BigInteger balanceOf(Address _owner) {
		return this.balances.get(_owner);
	}

	@Override
	@External
	public void transfer(Address _to, BigInteger _value, byte[] _data) {
		// TODO Auto-generated method stub
		
	}

}
