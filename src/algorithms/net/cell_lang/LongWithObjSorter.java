package net.cell_lang;


class LongWithObjSorter {
  public static void sort(long[] array, int size, Obj[] objs) {
    _sort(array, 0, size-1, objs);
  }

  public static void sort(long[] array, int start, int end, Obj[] objs) {
    _sort(array, start, end-1, objs);
  }

  //////////////////////////////////////////////////////////////////////////////

  private static void _sort(long[] array, int first, int last, Obj[] objs) {
    if (first >= last)
      return;

    int pivot = first + (last - first) / 2;

    if (pivot != first)
      swap(first, pivot, array, objs);

    int low = first + 1;
    int high = last;

    for ( ; ; ) {
      // Incrementing low until it points to the first slot that does
      // not contain a value that is lower or equal to the pivot
      // Such slot may be the first element after the end of the array
      while (low <= last && !isGreater(array[low], array[first]))
        low++;

      // Decrementing high until it points to the first slot whose
      // value is lower or equal to the pivot. Such slot may be the
      // first one, which contains the pivot
      while (high > first && isGreater(array[high], array[first]))
        high--;

      Miscellanea._assert(low != high);
      Miscellanea._assert(low < high | low == high + 1);

      // Once low and high have moved past each other all elements have been partitioned
      if (low > high)
        break;

      // Swapping the first pair of out-of-order elements before resuming the scan
      swap(low++, high--, array, objs);
    }

    // Putting the pivot between the two partitions
    int lastLeq = low - 1;
    if (lastLeq != first)
      swap(lastLeq, first, array, objs);

    // Now the lower-or-equal partition starts at 'first' and ends at
    // 'lastLeq - 1' (inclusive), since lastLeq contains the pivot
    _sort(array, first, lastLeq-1, objs);

    // The greater-then partition starts at high + 1 and
    // continues until the end of the array
    _sort(array, high+1, last, objs);
  }


  //////////////////////////////////////////////////////////////////////////////

  private static void swap(int idx1, int idx2, long[] array, Obj[] objs) {
    long tmp = array[idx1];
    array[idx1] = array[idx2];
    array[idx2] = tmp;

    Obj tmpObj = objs[idx1];
    objs[idx1] = objs[idx2];
    objs[idx2] = tmpObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static boolean isGreater(long value1, long value2) {
    return value1 > value2;
  }
}
