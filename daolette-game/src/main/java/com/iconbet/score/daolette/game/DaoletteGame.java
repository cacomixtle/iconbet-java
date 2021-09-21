package com.iconbet.score.daolette.game;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import score.Address;
import score.Context;
import score.VarDB;
import score.annotation.EventLog;

public class DaoletteGame {

	public static final String TAG = "DAOLETTE";

	private static final int[] BET_LIMIT_RATIOS = new int[] {147, 2675, 4315, 2725, 1930, 1454, 1136, 908, 738, 606,
	                                               500, 413, 341, 280, 227, 182, 142, 107, 76, 48, 23};

	private static final BigInteger BET_MIN = new BigInteger("100000000000000000");  // 1.0E+17, .1 ICX

	private static final String[] BET_TYPES = new String[] {"none", "bet_on_numbers", "bet_on_color", "bet_on_even_odd", "bet_on_number", "number_factor"};

	private static final Set<Integer> WHEEL_ORDER = new HashSet<>(Arrays.asList(2, 20, 3, 17, 6, 16, 7, 13, 10, 12,
	               11, 9, 14, 8, 15, 5, 18, 4, 19, 1, 0));

	private static final Set<Integer> WHEEL_BLACK = new HashSet<>(Arrays.asList(2,3,6,7,10,11,14,15,18,19));

	private static final Set<Integer> SET_BLACK = new HashSet<>(Arrays.asList( 2, 3, 6, 7, 10, 11, 14, 15, 18, 19));

	private static final Set<Integer> WHEEL_RED  = new HashSet<>( Arrays.asList(1,4,5,8,9,12,13,16,17,20));

	private static final Set<Integer> SET_RED = new HashSet<>(Arrays.asList( 1, 4, 5, 8, 9, 12, 13, 16, 17, 20));

	private static final Set<Integer> WHEEL_ODD = new HashSet<>( Arrays.asList(1,3,5,7,9,11,13,15,17,19));

	private static final Set<Integer> SET_ODD = new HashSet<>( Arrays.asList( 1, 3, 5, 7, 9, 11, 13, 15, 17, 19));

	private static final Set<Integer> WHEEL_EVEN = new HashSet<>( Arrays.asList(2,4,6,8,10,12,14,16,18,20));

	private static final Set<Integer> SET_EVEN = new HashSet<>( Arrays.asList( 2, 4, 6, 8, 10, 12, 14, 16, 18, 20));

	private static final Map<String, Float> MULTIPLIERS = Map.of(
			"bet_on_color", 2f,
			"bet_on_even_odd", 2f,
			"bet_on_number", 20f,
			"number_factor", 20.685f);

    private String _GAME_ON = "game_on";
    private String _TREASURY_SCORE="treasury_score";

	private  VarDB<Boolean> _game_on = Context.newVarDB(this._GAME_ON, Boolean.class);
    private VarDB<Address> _treasury_score = Context.newVarDB(this._TREASURY_SCORE, Address.class);

	public DaoletteGame() {
        Context.println("In __init__."+ TAG);
        Context.println("owner is "+ Context.getOwner() + ". "+ TAG);
        this._game_on.set(false);
	}

    @EventLog(indexed=2)
    public void BetSource(Address v, BigInteger timestamp) {}

    @EventLog(indexed=2)
    public void BetPlaced(BigInteger amount, String numbers) {}

    @EventLog(indexed=2)
    public void BetResult(String spin, String winningNumber, BigInteger payout) {}

    
}
