package com.iconbet.score.reward;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.TWO;
import static java.math.BigInteger.ZERO;

import java.math.BigInteger;
import java.util.Map;

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

public class RewardDistribution {
	protected static final Address ZERO_ADDRESS = new Address(new byte[Address.LENGTH]);

	public static final String TAG = "REWARDS";
	public static final BigInteger DAILY_TOKEN_DISTRIBUTION = new BigInteger("1000000000000000000000000");
	public static final boolean DEBUG = false;
	public static final BigInteger TAP = BigInteger.valueOf(1000000000000000000l);

	private static final String _WAGERS = "wagers";
	private static final String _DAY = "day";
	private static final String _EVEN_DAY = "even_day";
	private static final String _ODD_DAY = "odd_day";
	private static final String _EVEN_DAY_TOTAL = "even_day_total";
	private static final String _ODD_DAY_TOTAL = "odd_day_total";
	private static final String _WAGER_TOTAL = "wager_total";

	private static final String _DAILY_DIST = "daily_dist";
	private static final String _DIST_INDEX = "dist_index";
	private static final String _DIST_COMPLETE = "dist_complete";

	private static final String _GAME_SCORE = "game_score";
	private static final String _TOKEN_SCORE = "token_score";
	private static final String _DIVIDENDS_SCORE = "dividends_score";
	private static final String _BATCH_SIZE = "batch_size";

	private static String _REWARDS_GONE = "rewards_gone";
	private static String _YESTERDAYS_TAP_DISTRIBUTION = "yesterdays_tap_distribution";

	@EventLog(indexed=2)
	public void FundTransfer(String sweep_to, BigInteger amount, String note) {}

	@EventLog(indexed=2)
	public void TokenTransfer(Address recipient, BigInteger amount) {}

	//TODO: review this py dept = 2 data structure and possible null pointer ex.
	private BranchDB<BigInteger, DictDB<String, BigInteger>> _wagers = Context.newBranchDB(_WAGERS, BigInteger.class);
	private VarDB<BigInteger> _day_index = Context.newVarDB(_DAY, BigInteger.class);
	private ArrayDB<String> _even_day_addresses = Context.newArrayDB(_EVEN_DAY, String.class);
	private ArrayDB<String> _odd_day_addresses = Context.newArrayDB(_ODD_DAY, String.class);
	@SuppressWarnings("unchecked")
	private ArrayDB<String>[] _addresses = new ArrayDB[] {this._even_day_addresses, this._odd_day_addresses};
	private VarDB<BigInteger> _even_day_total = Context.newVarDB(_EVEN_DAY_TOTAL, BigInteger.class);
	private VarDB<BigInteger> _odd_day_total = Context.newVarDB(_ODD_DAY_TOTAL, BigInteger.class);
	@SuppressWarnings("unchecked")
	private VarDB<BigInteger>[] _daily_totals = new VarDB[] {this._even_day_total, this._odd_day_total};
	private VarDB<BigInteger> _wager_total = Context.newVarDB(_WAGER_TOTAL, BigInteger.class);
	private VarDB<BigInteger> _daily_dist = Context.newVarDB(_DAILY_DIST, BigInteger.class);
	private VarDB<BigInteger> _dist_index = Context.newVarDB(_DIST_INDEX, BigInteger.class);
	private VarDB<Boolean> _dist_complete = Context.newVarDB(_DIST_COMPLETE, Boolean.class);

	private VarDB<Address> _game_score = Context.newVarDB(_GAME_SCORE, Address.class);
	private VarDB<Address> _token_score = Context.newVarDB(_TOKEN_SCORE, Address.class);
	private VarDB<Address> _dividends_score = Context.newVarDB(_DIVIDENDS_SCORE, Address.class);
	private VarDB<BigInteger> _batch_size = Context.newVarDB(_BATCH_SIZE, BigInteger.class);

	// rewards gone variable checks if the 500M tap token held for distribution is completed
	private VarDB<Boolean> _rewards_gone = Context.newVarDB(_REWARDS_GONE, Boolean.class);
	private VarDB<BigInteger> _yesterdays_tap_distribution = Context.newVarDB(_YESTERDAYS_TAP_DISTRIBUTION, BigInteger.class);

	public RewardDistribution(@Optional boolean _on_update_var) {
		if(_on_update_var) {
			Context.println("updating contract only");
			onUpdate();
			return;
		}
		Context.println("In __init__. "+ TAG);
		Context.println("owner is " +Context.getOwner() +". "+ TAG);
		this._day_index.set(ZERO);
		this._dist_index.set(ZERO);
		this._dist_complete.set(true);

		this._even_day_total.set(ZERO);
		this._odd_day_total.set(ZERO);
		this._rewards_gone.set(false);

	}

	public void onUpdate() {
		Context.println("calling on update. "+TAG);
	}

	/*
    Sets the tap token score address
    :param _score: Address of the token score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_token_score(Address _score) {
		if (Context.getCaller().equals(Context.getOwner())) {
			this._token_score.set(_score);
		}
	}

	/*
    Returns the tap token score address
    :return: Address of the tap token score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_token_score() {
		return this._token_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Sets the dividends distribution score address
    :param _score: Address of the dividends distribution score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_dividends_score(Address _score) {
		if (Context.getCaller().equals(Context.getOwner())) {
			this._dividends_score.set(_score);
		}
	}

	/*
    Returns the dividends distribution score address
    :return: Address of the dividends distribution score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_dividends_score() {
		return this._dividends_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Sets the roulette score address
    :param _score: Address of the roulette score
    :type _score: :class:`iconservice.base.address.Address`
    :return:
	 */
	@External
	public void set_game_score(Address _score) {

		if (Context.getCaller().equals(Context.getOwner())) {
			this._game_score.set(_score);
		}
	}

	/*
    Returns the roulette score address
    :return: Address of the roulette score
    :rtype: :class:`iconservice.base.address.Address`
	 */
	@External(readonly=true)
	public Address get_game_score() {
		return this._game_score.getOrDefault(ZERO_ADDRESS);
	}

	/*
    Checks the status for tap token distribution
    :return: True if tap token has been distributed for previous day
    :rtype: bool
	 */
	@External(readonly=true)
	public boolean rewards_dist_complete() {
		return this._dist_complete.getOrDefault(false);
	}

	/*
    Provides total wagers made in current day.
    :return: Total wagers made in current day in loop
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_todays_total_wagers() {
		return this._daily_totals[this._day_index.get().intValue()].getOrDefault(ZERO);
	}

	/*
    Returns total wagers made by the player in the current day
    :param _player: Player address for which the wagers has to be checked
    :type _player: str
    :return: Wagers made by the player in current day
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_daily_wagers(String _player) {
		return this._wagers.at(this._day_index.get()).getOrDefault(_player, ZERO);
	}


	/*
    Returns the expected TAP tokens the player will receive according to the total wagers at that moment
    :param _player: Player address for which expected rewards is to be checked
    :type _player: str
    :return: Expected TAP tokens that the player can receive
    :rtype: int
	 */
	@External(readonly=true)
	public BigInteger get_expected_rewards(String _player) {
		BigInteger total = this.get_todays_total_wagers();
		if (total.equals(ZERO)) {
			return ZERO;
		}
		BigInteger expectedRewards = this.get_todays_tap_distribution().multiply(this.get_daily_wagers(_player)).divide(total);
		return expectedRewards;
	}

	/*
    Returns the amount of TAP to be distributed today
    :return:
	 */
	@External(readonly=true)
	public BigInteger get_todays_tap_distribution() {

		Context.println("calling tap-token["+ this._token_score.get() +"].balanceOf for reward address: "+ Context.getAddress().toString());
		BigInteger remainingTokens = Context.call(BigInteger.class, this._token_score.get(),  "balanceOf", Context.getAddress());
		Context.println("remain tokens: "+ remainingTokens + " of "+ Context.getAddress());
		if (remainingTokens.equals(BigInteger.valueOf(264000000).multiply(TAP))) {
			return TWO.multiply(DAILY_TOKEN_DISTRIBUTION).add(remainingTokens).mod(DAILY_TOKEN_DISTRIBUTION);
		}else if (remainingTokens.compareTo( BigInteger.valueOf(251000000).multiply(TAP) ) >= 0) {
			return DAILY_TOKEN_DISTRIBUTION.add(remainingTokens).mod(DAILY_TOKEN_DISTRIBUTION);
		}else {
			return BigInteger.valueOf(200_000).multiply(TAP)
					.max(
							this._yesterdays_tap_distribution.getOrDefault(ZERO)
							.multiply(BigInteger.valueOf(995))
							.divide(BigInteger.valueOf(1000))
							)
					.min(remainingTokens);
		}
	}

	/*
    A function to redefine the value of self.owner once it is possible.
    To be included through an update if it is added to IconService.

    Sets the value of self.owner to the score holding the game treasury.
	 */
	@External
	public void untether() {
		if (! Context.getOrigin().equals(Context.getOwner())) {
			Context.revert("Only the owner can call the untether method.");
		}
	}

	/*
    Returns all the addresses which have played games today and yesterday with their wagered amount in the entire
    platform
    :return: JSON data of yesterday's and today's players and their wagers
    :rtype: str
	 */
	@SuppressWarnings("unchecked")
	@External(readonly=true)
	public String get_daily_wager_totals() {
		Context.println(Context.getCaller() +" is getting daily wagers. "+ TAG);
		BigInteger index = this._day_index.get();

		Map.Entry<String, BigInteger>[] today = new Map.Entry[this._addresses[index.intValue()].size()];
		int j = 0;

		for(int i =0 ; i < this._addresses[index.intValue()].size(); i++){
			String address = this._addresses[index.intValue()].get(i);
			BigInteger amount = this._wagers.at(index).get(address);
			today[j] = Map.entry(address, amount);
			j++;
			Context.println("Wager amount of "+ amount+" being added. "+ TAG);
		}

		index = this._day_index.get().add(ONE).mod(TWO);

		Map.Entry<String, BigInteger>[] yesterday = new Map.Entry[this._addresses[index.intValue()].size()];
		j = 0;
		for(int i =0 ; i < this._addresses[index.intValue()].size(); i++){
			String address = this._addresses[index.intValue()].get(i);
			BigInteger amount = this._wagers.at(index).get(address);
			yesterday[j] = Map.entry(address, amount);
			j++;
			Context.println("Wager amount of "+ amount+" being added. "+ TAG);
		}
		Map<String, Map<String, BigInteger>> dailyWagers = Map.of(
				"today", Map.ofEntries(today),
				"yesterday", Map.ofEntries(yesterday)
				);
		String json = mapToJsonString(dailyWagers);
		Context.println("Wager totals " + json + " "+ TAG);
		return json;
	}

	/*
    Records data of wagers made by players in any games in the ICONbet platform. If the day has changed then
    data for the index of today is cleared. Index can be 0 or 1. The wagerers from previous day are made eligible to
    receive TAP tokens. Calls the distribute function of dividends distribution score and distribute function for
    TAP tokens distribution if they are not completed.
    :param player: Address of the player playing any games in ICONbet platform
    :type player: str
    :param wager: Wager amount of the player
    :type wager: int
    :param day_index: Day index for which player data is to be recorded(0 or 1)
    :type day_index: int
    :return:
	 */
	@External
	public void accumulate_wagers(String  player, BigInteger  wager, BigInteger  day_index) {
		if ( !Context.getCaller().equals(this._game_score.get()) ) {
			Context.revert("This function can only be called from the game score.");
		}
		Context.println("In accumulate_wagers, day_index = "+ day_index +". "+ TAG);
		BigInteger day = this._day_index.get();
		Context.println(this._day_index +" = "+ day + ". "+ TAG);
		if ( day.compareTo(day_index) != 0) {
			Context.println("Setting self._day_index to "+ day_index+ ". "+ TAG);
			this._day_index.set(day_index);

			for( int i = 0; i < this._addresses[day_index.intValue()].size(); i++) {
				//TODO: review removal logic
				this._wagers.at(day_index).set(this._addresses[day_index.intValue()].pop(), null);
			}

			if ( !this._rewards_gone.get()) {
				BigInteger remainingTokens = Context.call(BigInteger.class, this._token_score.get(),  "balanceOf", Context.getAddress());
				if (remainingTokens.equals(ZERO)) {
					this._rewards_gone.set(true);
				}else {
					this._set_batch_size();
					this._dist_index.set(ZERO);
					this._dist_complete.set(false);
					this._wager_total.set(this._daily_totals[day.intValue()].get());
					this._set_daily_dist(remainingTokens);
				}
			}
			this._daily_totals[day_index.intValue()].set(ZERO);
		}

		Context.println("Lengths: " + this._addresses[0].size() +" , " + this._addresses[1].size() +" "+ TAG);
		Context.println("Adding wager from "+ player +". "+ TAG);
		this._daily_totals[day_index.intValue()].set(this._daily_totals[day_index.intValue()].get().add(wager));
		Context.println("Total wagers = " + this._daily_totals[day_index.intValue()].get() + ". "+ TAG);
		if (containsInArrayDb(player, this._addresses[day_index.intValue()]) ) {
			Context.println("Adding wager to " + player + " in _addresses[" + day_index.intValue() +" ]. " + TAG);
			this._wagers.at(day_index).set(player,  this._wagers.at(day_index).get(player).add(wager) );
		}else {
			Context.println("Putting "+ player +" in _addresses["+ day_index.intValue() + "]. " + TAG);
			this._addresses[day_index.intValue()].add(player);
			this._wagers.at(day_index).set(player, wager);
		}

		Boolean distribute = Context.call(Boolean.class, this._dividends_score.get(), "distribute");
		if (distribute != null && distribute) {
			this._distribute();
		}
		Context.println("Done in accumulate_wagers.  self._day_index = " +this._day_index.get() + ". "+ TAG);
	}

	/*
    Sets the batch size to be used for TAP distribution. Uses the function from roulette score
    :return:
	 */
	public void _set_batch_size() {
		BigInteger size = Context.call(BigInteger.class, this._game_score.get(), "get_batch_size", 
				BigInteger.valueOf(
						this._addresses[this._day_index.get().intValue()]
								.size()));
		this._batch_size.set(size);
	}

	/*
    Main distribution function to distribute the TAP token to the wagerers. Distributes the TAP token only if this
    contract holds some TAP token.
    :return:
	 */    
	public void _distribute() {
		if (this._rewards_gone.get()) {
			this._dist_complete.set(true);
			return;
		}
		Context.println("Beginning rewards distribution. "+ TAG);
		int index = (this._day_index.getOrDefault(ZERO).intValue() + 1) % 2;
		int count = this._batch_size.getOrDefault(ZERO).intValue();
		ArrayDB<String> addresses = this._addresses[index];
		int length = addresses.size();
		int start = this._dist_index.get().intValue();
		int remainingAddresses = length - start;
		if (count > remainingAddresses) {
			count = remainingAddresses;
		}
		int end = start + count;
		Context.println("Length of address list: " + length + ". Remaining = " + remainingAddresses + " "+ TAG);

		BigInteger totalDist = this._daily_dist.getOrDefault(ZERO);
		BigInteger totalWagers = this._wager_total.getOrDefault(ZERO);
		if (totalWagers.equals(ZERO) ) {
			this._dist_index.set(ZERO);
			this._dist_complete.set(true);
			return;
		}

		for (int i=start; i<end; i++ ) {
			BigInteger wagered = this._wagers.at(BigInteger.valueOf(index)).getOrDefault(addresses.get(i), ZERO);
			BigInteger rewardsDue = totalDist.multiply(wagered).divide(totalWagers);
			totalDist = totalDist.subtract(rewardsDue);
			totalWagers = totalWagers.subtract(wagered);
			Context.println("Rewards due to "+ addresses.get(i) +" = " + rewardsDue +" "+ TAG);
			try {
				Context.println("Trying to send to (" + addresses.get(i) +"): " +rewardsDue +". "+ TAG);
				Address fullAddress = Address.fromString(addresses.get(i));
				Context.call(this._token_score.get(),  "transfer", fullAddress, rewardsDue);
				this.TokenTransfer(fullAddress, rewardsDue);
				Context.println("Sent player (" + addresses.get(i) +") " + rewardsDue +". "+ TAG);
			} catch(Exception e) {
				Context.println("Send failed. Exception: " + e.getMessage() + " "+ TAG);
				Context.revert("Network problem. Rewards not sent. Will try again later. Exception: " + e.getMessage());
			}
		}
		this._daily_dist.set(totalDist);
		this._wager_total.set(totalWagers);
		if (end == length) {
			this._dist_index.set(ZERO);
			this._dist_complete.set(true);
		}else{
			this._dist_index.set(this._dist_index.getOrDefault(ZERO).add(BigInteger.valueOf(count)));
		}
	}

	/*
    Sets the amount of TAP to be distributed on each day
    :param remaining_tokens: Remaining TAP tokens on the rewards contract
    :return:
	 */
	private void _set_daily_dist(BigInteger remaining_tokens) {
		if (remaining_tokens.equals( BigInteger.valueOf(264000000).multiply(TAP) )) {
			this._daily_dist.set( TWO.multiply(DAILY_TOKEN_DISTRIBUTION).add(remaining_tokens).mod(DAILY_TOKEN_DISTRIBUTION));
			this._yesterdays_tap_distribution.set(DAILY_TOKEN_DISTRIBUTION);
		}else if (remaining_tokens.compareTo(BigInteger.valueOf(251000000).multiply(TAP) ) >= 0 ) {
			this._daily_dist.set(DAILY_TOKEN_DISTRIBUTION.add(remaining_tokens).mod(DAILY_TOKEN_DISTRIBUTION));
			this._yesterdays_tap_distribution.set(DAILY_TOKEN_DISTRIBUTION);
		}else {
			BigInteger dailyDist = BigInteger.valueOf(200_000).multiply(TAP)
					.max(
							this._yesterdays_tap_distribution.get()
							.multiply(BigInteger.valueOf(995))
							.divide(BigInteger.valueOf(1000))
							);
			dailyDist = dailyDist.min(remaining_tokens);
			this._yesterdays_tap_distribution.set(dailyDist);
			this._daily_dist.set(dailyDist);
		}
	}

	@Payable
	public void fallback() {
		Context.revert("This contract doesn't accept ICX");
	}

	/*This score will hold the 80% of TAP tokens for distribution.*/
	@External
	public void tokenFallback(Address _from, BigInteger _value,byte[] _data) {

		Context.println("calling balance of token score "+ this._token_score.get() + " for addr "  +Context.getAddress());
		BigInteger remainingTokens = Context.call(BigInteger.class, this._token_score.get(), "balanceOf", Context.getAddress());
		Context.println("remaining tokens of "+ Context.getAddress() +": "+ remainingTokens);
		if (remainingTokens.equals( BigInteger.valueOf(264000000).multiply(TAP)) ){
			Context.revert("Not able to receive further TAP when the balance is 264M tap tokens");
		}
		String symbol = Context.call(String.class, this._token_score.get(), "symbol");
		if ( !symbol.equals("TAP") ) {
			Context.revert("The Rewards Score can only receive TAP tokens.");
		}
		this._rewards_gone.set(false);
		Context.println(_value + " TAP tokens received from "+ _from + ". " + TAG);
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

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static <K,V> String mapToJsonString(Map<K, V > map) {
		if( map.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder("{");
		for (Map.Entry<K, V> entry : map.entrySet()) {
			if(entry.getValue() instanceof Map) {
				String subEntry = mapToJsonString((Map)entry.getValue());
				if(subEntry != null) {
					sb.append("\""+entry.getKey()+"\":\""+ subEntry+"\",");	
				}else {
					sb.append("\""+entry.getKey()+"\":null ,");
				}
			}else {
				sb.append("\""+entry.getKey()+"\":\""+entry.getValue()+"\",");
			}
		}
		char c = sb.charAt(sb.length()-1);
		if(c == ',') {
			sb.deleteCharAt(sb.length()-1);
		}
		sb.append("}");
		String json = sb.toString();
		Context.println(json);
		return json;
	}

}
