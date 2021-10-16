package com.iconbet.score.daolette.game;

import java.util.List;

public final class ArrayUtils {

	ArrayUtils() {}

	@SuppressWarnings("unchecked")
	public static <E> List<E> removeElement(List<E> list, E element){
		E[] array = (E[])list.toArray();

		boolean found = false;
		for(int i = 0; i < array.length; i++) {
			if(array[i].equals(element)) {
				int numMoved = array.length - i - 1;
				System.arraycopy(array, i+1, array, i, numMoved);
				found = true;
				break;
			}
		}
		if(!found) {
			return list;
		}

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

		return List.of((E[])result);
	}

	//TODO: not used
	@SuppressWarnings("unchecked")
	public static <E> List<E> removeElementIndexGeneric(List<E> list, int index){
		E[] array = (E[])list.toArray();

		if(index >= list.size()) {
			return list;
		}

		int numMoved = array.length - index - 1;
		System.arraycopy(array, index+1, array, index, numMoved);

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

		return List.of((E[])result);
	}

	//TODO: not used
	@SuppressWarnings({ "rawtypes" })
	public static List removeElementByIndex(List list, int index){
		Object[] array = list.toArray();

		if(index >= list.size()) {
			return list;
		}

		int numMoved = array.length - index - 1;
		System.arraycopy(array, index+1, array, index, numMoved);

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

		return List.of(result);
	}

	@SuppressWarnings({ "rawtypes" })
	public static List removeElement(List list, int value){
		Object[] array = list.toArray();

		int index = list.indexOf(value);

		if(index == -1) {
			return list;
		}

		int numMoved = array.length - index - 1;
		System.arraycopy(array, index+1, array, index, numMoved);

        array[array.length-1] = null;

        Object[] result = new Object[array.length-1];

        System.arraycopy(array, 0, result, 0, result.length);

		return List.of(result);
	}
}
