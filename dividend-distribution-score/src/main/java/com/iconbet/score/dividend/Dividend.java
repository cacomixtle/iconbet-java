package com.iconbet.score.dividend;

import java.util.List;

import score.Context;
import score.VarDB;

public class Dividend {

	public static final String TAG = "ICONbet Dividends";
	public static List<String> DIVIDEND_CATEGORIES = List.of("_tap", "_gamedev", "_promo", "_platform");

	private static final String _DIVS_DIST_COMPLETE = "dist_complete";

	private static final String _TAP_DIST_INDEX = "dist_index";
	private static final String _BATCH_SIZE = "batch_size";

	private static final String _TAP_BALANCES = "balances";

	private static final String _TOTAL_DIVS = "total_divs";
	private static final String _REMAINING_TAP_DIVS = "remaining_divs";
	private static final String _REMAINING_GAMEDEV_DIVS = "remaining_gamedev_divs";
	private static final String _PLATFORM_DIVS = "platform_divs";
	private static final String _PROMO_DIVS = "promo_divs";
	private static final String _DAOFUND_DIVS = "daofund_divs";

	private static final String _TOTAL_ELIGIBLE_TAP_TOKENS = "remaining_tokens";
	private static final String _BLACKLIST_ADDRESS = "blacklist_addresses";
	private static final String _INHOUSE_GAMES = "inhouse_games";

	private static final String _GAMES_LIST = "games_list";
	private static final String _GAMES_EXCESS = "games_excess";
	private static final String _REVSHARE_WALLET_ADDRESS = "revshare_wallet_address";

	private static final String _DIVIDEND_PERCENTAGE = "dividend_percentage";

	private static final String _TOKEN_SCORE = "token_score";
	private static final String _GAME_SCORE = "game_score";
	private static final String _PROMO_SCORE = "promo_score";
	private static final String _DAOFUND_SCORE = "daofund_score";
	private static final String _GAME_AUTH_SCORE = "game_auth_score";
	private static final String _DIVIDENDS_RECEIVED = "dividends_received";

	private static final String _STAKE_HOLDERS = "stake_holders";
	private static final String _STAKE_BALANCES = "stake_balances";
	private static final String _TOTAL_ELIGIBLE_STAKED_TAP_TOKENS = "total_eligible_staked_tap_tokens";
	private static final String  _STAKE_DIST_INDEX = "stake_dist_index";

	private static final String _SWITCH_DIVIDENDS_TO_STAKED_TAP = "switch_dividends_to_staked_tap";

	private static final String _EXCEPTION_ADDRESS = "exception_address";

    //# Variables related to completion of distribution
	private final VarDB<Boolean> _divs_dist_complete =  Context.newVarDB(_DIVS_DIST_COMPLETE, Boolean.class);

			    
}
