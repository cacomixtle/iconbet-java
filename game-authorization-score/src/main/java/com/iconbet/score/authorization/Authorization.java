package com.iconbet.score.authorization;


import java.math.BigInteger;

import score.Address;
import com.iconloop.score.token.irc2.IRC2;


public class Authorization implements IRC2{

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
    
    
	@Override
	public String name() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public String symbol() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public int decimals() {
		// TODO Auto-generated method stub
		return 0;
	}
	@Override
	public BigInteger totalSupply() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public BigInteger balanceOf(Address _owner) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void transfer(Address _to, BigInteger _value, byte[] _data) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void Transfer(Address _from, Address _to, BigInteger _value, byte[] _data) {
		// TODO Auto-generated method stub
		
	}

  //  public void FundTransfer(Address recipient,BigInteger amount, String note) {};

//    public void Transfer(Address t ,Address s ,BigInteger i,byte[] b){};
    // BigLong d;
}