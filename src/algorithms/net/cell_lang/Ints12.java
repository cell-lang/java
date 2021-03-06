package net.cell_lang;


class Ints12 {
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
    int offset1 = 2 * idx1;
    int offset2 = 2 * idx2;
    int tmp0 = array[offset1];
    int tmp1 = array[offset1 + 1];
    array[offset1]     = array[offset2];
    array[offset1 + 1] = array[offset2 + 1];
    array[offset2]     = tmp0;
    array[offset2 + 1] = tmp1;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static boolean contains(int[] array, int size, int val1, int val2) {
    int low = 0;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      switch (ordCheck(mid, val1, val2, array)) {
        case -1: // mid < target range
          low = mid + 1;
          break;

        case 0: // mid in target range
          return true;

        case 1: // mid > target range
          high = mid - 1;
          break;
      }
    }

    return false;
  }

  public static boolean contains1(int[] array, int size, int val1) {
    return contains1(array, 0, size, val1);
  }

  public static boolean contains1(int[] array, int offset, int count, int val1) {
    int low = offset;
    int high = offset + count - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      switch (rangeCheck1(mid, val1, array)) {
        case -1: // mid < target range
          low = mid + 1;
          break;

        case 0: // mid in target range
          return true;

        case 1: // mid > target range
          high = mid - 1;
          break;
      }
    }

    return false;
  }

  public static int indexFirst1(int[] array, int size, int val1) {
    int low = 0;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      switch (rangeStartCheck1(mid, val1, array)) {
        case -1: // mid < target range start
          low = mid + 1;
          break;

        case 0: // mid == target range start
          return mid;

        case 1: // mid > target range start
          high = mid - 1;
          break;
      }
    }

    return -1;
  }

  public static int count1(int[] array, int size, int val1, int offset) {
    return rangeEndExclusive(array, size, val1, offset) - offset;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static int rangeEndExclusive(int[] array, int size, int val1, int offset) {
    int low = offset;
    int high = size - 1;

    while (low <= high) {
      int mid = low + (high - low) / 2;
      switch (rangeEndCheck1(mid, val1, array, size)) {
        case -1: // mid < target range end
          low = mid + 1;
          break;

        case 0: // mid == target range end
          return mid + 1;

        case 1: // mid > target range end
          high = mid - 1;
          break;
      }
    }

    return offset;
  }

  private static int rangeStartCheck1(int idx, int val1, int[] array) {
    int ord = rangeCheck1(idx, val1, array);
    if (ord != 0 | idx == 0)
      return ord;
    ord = rangeCheck1(idx-1, val1, array);
    Miscellanea._assert(ord == 0 | ord == -1);
    return ord == -1 ? 0 : 1;
  }

  private static int rangeEndCheck1(int idx, int val1, int[] array, int size) {
    int ord = rangeCheck1(idx, val1, array);
    if (ord != 0 | idx == size-1)
      return ord;
    ord = rangeCheck1(idx+1, val1, array);
    if (!(ord == 0 | ord == 1))
      System.out.printf("\n");
    Miscellanea._assert(ord == 0 | ord == 1);
    return ord == 1 ? 0 : -1;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static boolean isGreater(int idx1, int idx2, int[] array) {
    int offset1 = 2 * idx1;
    int offset2 = 2 * idx2;
    int elem1 = array[offset1];
    int elem2 = array[offset2];
    if (elem1 != elem2)
      return elem1 > elem2;
    elem1 = array[offset1 + 1];
    elem2 = array[offset2 + 1];
    return elem1 > elem2;
  }

  private static int ordCheck(int idx, int val1, int val2, int[] array) {
    int offset = 2 * idx;
    int val = array[offset];
    if (val < val1)
      return -1;
    if (val > val1)
      return 1;
    val = array[offset + 1];
    if (val < val2)
      return -1;
    if (val > val2)
      return 1;
    return 0;
  }

  private static int rangeCheck1(int idx, int val1, int[] array) {
    int offset = 2 * idx;
    int val = array[offset];
    if (val < val1)
      return -1;
    if (val > val1)
      return 1;
    return 0;
  }
}
