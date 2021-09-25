package com.iconbet.score.promotion;

public final class ArrayUtils {

	ArrayUtils(){}

    // A utility function to swap two elements
    public static void swap(Object[] arr, int i, int j){
    	Object temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }

    /* This function takes last element as pivot, places
    the pivot element at its correct position in sorted
    array, and places all smaller (smaller than pivot)
    to left of pivot and all greater elements to right
    of pivot */
    @SuppressWarnings({ "unchecked", "rawtypes" })
	public static int partition(Object[] arr, int low, int high, Comparator c){
        
        // pivot
    	Object pivot = arr[high];
        
        // Index of smaller element and
        // indicates the right position
        // of pivot found so far
        int i = (low - 1);

        for(int j = low; j <= high - 1; j++)
        {
            
            // If current element is smaller
            // than the pivot
            if ( c.compare(arr[j], pivot) < 0)
            {
                
                // Increment index of
                // smaller element
                i++;
                swap(arr, i, j);
            }
        }
        swap(arr, i + 1, high);
        return (i + 1);
    }

    /* The main function that implements QuickSort
            arr[] --> Array to be sorted,
            low --> Starting index,
            high --> Ending index
    */
    @SuppressWarnings({"rawtypes"})
    public static void quickSort(Object[] arr, int low, int high, Comparator c){
        if (low < high)
        {
            
            // pi is partitioning index, arr[p]
            // is now at right place
            int pi = partition(arr, low, high,c);

            // Separately sort elements before
            // partition and after partition
            quickSort(arr, low, pi - 1, c);
            quickSort(arr, pi + 1, high, c);
        }
    }

    public static Object[] top(Object[] arr, int max, boolean startFromEnd) {

    	if(arr == null || arr.length < max ) {
    		return arr;
    	}

    	Object[] dest = new Object[max];
    	if(startFromEnd) {
    		System.arraycopy(arr, arr.length-max, dest, 0, max);
    	}else {
    		System.arraycopy(arr, 0, dest, 0, max);	
    	}

    	return dest;
    }

}
