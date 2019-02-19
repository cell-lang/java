package net.cell_lang;


class Ints231 {
  public static void sort(int[] array, int size) {
    sort(array, 0, size-1);
  }

  private static void sort(int[] array, int first, int last) {
    if (first >= last)
      return;

    int pivot = first + (last - first) / 2;

    if (pivot != first)
      swap(first, pivot, array);

    int low = first + 1;
    int high = last;

    for ( ; ; ) {
      // Incrementing low until it points to the first slot that does
      // not contain a value that is lower or equal to the pivot
      // Such slot may be the first element after the end of the array
      while (low <= last && !isGreater(low, first, array))
        low++;

      // Decrementing high until it points to the first slot whose
      // value is lower or equal to the pivot. Such slot may be the
      // first one, which contains the pivot
      while (high > first && isGreater(high, first, array))
        high--;

      Miscellanea._assert(low != high);
      Miscellanea._assert(low < high | low == high + 1);

      // Once low and high have moved past each other all elements have been partitioned
      if (low > high)
        break;

      // Swapping the first pair of out-of-order elements before resuming the scan
      swap(low++, high--, array);
    }

    // Putting the pivot between the two partitions
    int lastLeq = low - 1;
    if (lastLeq != first)
      swap(lastLeq, first, array);

    // Now the lower-or-equal partition starts at 'first' and ends at
    // 'lastLeq - 1' (inclusive), since lastLeq contains the pivot
    sort(array, first, lastLeq-1);

    // The greater-then partition starts at high + 1 and
    // continues until the end of the array
    sort(array, high+1, last);
  }


  //////////////////////////////////////////////////////////////////////////////

  private static void swap(int idx1, int idx2, int[] array) {
    int offset1 = 3 * idx1;
    int offset2 = 3 * idx2;
    int tmp0 = array[offset1];
    int tmp1 = array[offset1 + 1];
    int tmp2 = array[offset1 + 2];
    array[offset1]     = array[offset2];
    array[offset1 + 1] = array[offset2 + 1];
    array[offset1 + 2] = array[offset2 + 2];
    array[offset2]     = tmp0;
    array[offset2 + 1] = tmp1;
    array[offset2 + 2] = tmp2;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static boolean contains2(int[] array, int size, int val2) {
    return contains2(array, 0, size, val2);
  }

  public static boolean contains2(int[] array, int offset, int count, int val2) {
    int low = offset;
    int high = offset + count - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int ord = rangeCheck2(mid, val2, array);
      if (ord == -1) // mid < target range
        low = mid + 1;
      else if (ord == 1) // mid > target range
        high = mid - 1;
      else
        return true;
    }

    return false;
  }

  public static boolean contains23(int[] array, int size, int val2, int val3) {
    return contains23(array, 0, size, val2, val3);
  }

  public static boolean contains23(int[] array, int offset, int count, int val2, int val3) {
    int low = offset;
    int high = offset + count - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int ord = rangeCheck23(mid, val2, val3, array);
      if (ord == -1) // mid < target range
        low = mid + 1;
      else if (ord == 1) // mid > target range
        high = mid - 1;
      else
        return true;
    }

    return false;
  }

  public static int indexFirst2(int[] array, int size, int val2) {
    int low = 0;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int ord = rangeStartCheck2(mid, val2, array);
      if (ord == -1) // mid < target range start
        low = mid + 1;
      else if (ord == 1) // mid > target range start
        high = mid - 1;
      else
        return mid;
    }

    return -1;
  }

  public static int indexFirst23(int[] array, int size, int val2, int val3) {
    int low = 0;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int ord = rangeStartCheck23(mid, val2, val3, array);
      if (ord == -1) // mid < target range start
        low = mid + 1;
      else if (ord == 1) // mid > target range start
        high = mid - 1;
      else
        return mid;
    }

    return -1;
  }

  public static int count2(int[] array, int size, int val2, int offset) {
    return rangeEndExclusive2(val2, offset, array, size) - offset;
  }

  public static int count23(int[] array, int size, int val2, int val3, int offset) {
    return rangeEndExclusive23(val2, val3, offset, array, size) - offset;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static int rangeEndExclusive2(int val2, int offset, int[] array, int size) {
    int low = offset;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int ord = rangeEndCheck2(mid, val2, array, size);
      if (ord == -1) // mid < target range end
        low = mid + 1;
      else if (ord == 1) // mid > target range end
        high = mid - 1;
      else
        return mid + 1;
    }

    return offset;
  }

  private static int rangeEndExclusive23(int val2, int val3, int offset, int[] array, int size) {
    int low = offset;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      int ord = rangeEndCheck23(mid, val2, val3, array, size);
      if (ord == -1) // mid < target range end
        low = mid + 1;
      else if (ord == 1) // mid > target range end
        high = mid - 1;
      else
        return mid + 1;
    }

    return offset;
  }


  private static int rangeStartCheck2(int idx, int val2, int[] array) {
    int ord = rangeCheck2(idx, val2, array);
    if (ord != 0 | idx == 0)
      return ord;
    ord = rangeCheck2(idx-1, val2, array);
    Miscellanea._assert(ord == 0 | ord == -1);
    return ord == -1 ? 0 : 1;
  }

  private static int rangeEndCheck2(int idx, int val2, int[] array, int size) {
    int ord = rangeCheck2(idx, val2, array);
    if (ord != 0 | idx == size-1)
      return ord;
    ord = rangeCheck2(idx+1, val2, array);
    Miscellanea._assert(ord == 0 | ord == 1);
    return ord == 1 ? 0 : -1;
  }

  private static int rangeStartCheck23(int idx, int val2, int val3, int[] array) {
    int ord = rangeCheck23(idx, val2, val3, array);
    if (ord != 0 | idx == 0)
      return ord;
    ord = rangeCheck23(idx-1, val2, val3, array);
    Miscellanea._assert(ord == 0 | ord == -1);
    return ord == -1 ? 0 : 1;
  }

  private static int rangeEndCheck23(int idx, int val2, int val3, int[] array, int size) {
    int ord = rangeCheck23(idx, val2, val3, array);
    if (ord != 0 | idx == size-1)
      return ord;
    ord = rangeCheck23(idx+1, val2, val3, array);
    Miscellanea._assert(ord == 0 | ord == 1);
    return ord == 1 ? 0 : -1;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static boolean isGreater(int idx1, int idx2, int[] array) {
    int offset1 = 3 * idx1;
    int offset2 = 3 * idx2;
    int elem1 = array[offset1 + 1];
    int elem2 = array[offset2 + 1];
    if (elem1 != elem2)
      return elem1 > elem2;
    elem1 = array[offset1 + 2];
    elem2 = array[offset2 + 2];
    if (elem1 != elem2)
      return elem1 > elem2;
    elem1 = array[offset1];
    elem2 = array[offset2];
    return elem1 > elem2;
  }

  private static int rangeCheck2(int idx, int val2, int[] array) {
    int val = array[3 * idx + 1];
    if (val < val2)
      return -1;
    if (val > val2)
      return 1;
    return 0;
  }

  private static int rangeCheck23(int idx, int val2, int val3, int[] array) {
    int offset = 3 * idx;
    int val = array[offset + 1];
    if (val < val2)
      return -1;
    if (val > val2)
      return 1;
    val = array[offset + 2];
    if (val < val3)
      return -1;
    if (val > val3)
      return 1;
    return 0;
  }
}
