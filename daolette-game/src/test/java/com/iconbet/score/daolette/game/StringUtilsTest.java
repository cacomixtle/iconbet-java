package com.iconbet.score.daolette.game;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.stream.Stream;

import org.junit.jupiter.api.Test;

class StringUtilsTest {

	@Test
	void testSplit() {
		String list = "1,2,3,4,5,6,7,8,9,";
		String[] array = StringUtils.split(list, ',');
		assertEquals(10, array.length);

		list = "1,2,3,4,5,6,7,8,9";
		array = StringUtils.split(list, ',');
		assertEquals(9, array.length);

		list = ",1,2,3,4,5,6,7,8,9";
		array = StringUtils.split(list, ',');
		System.out.println(array.length);
		Stream.of(array).forEach(s -> System.out.println(s));
		assertEquals(10, array.length);

		list = "1";
		array = StringUtils.split(list, ',');
		Stream.of(array).forEach(s -> System.out.println(s));
		assertEquals(1, array.length);
		assertEquals(array[0], "1");
	}
}
