package com.iconbet.score.tap;

import java.math.BigInteger;

public class Status {
	private Status() {}
	public static final Integer AVAILABLE = 0;
	public static final Integer STAKED = 1;
	public static final Integer UNSTAKING = 2;
	public static final Integer UNSTAKING_PERIOD = 3;

	protected static final BigInteger[] EMPTY_STATUS_ARRAY = new BigInteger[]
			{BigInteger.ZERO,BigInteger.ZERO,BigInteger.ZERO,BigInteger.ZERO};
}
