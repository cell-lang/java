package net.cell_lang;


class PackedIntPairs {
  public static void sort(long[] array, int size) {
    Array.sort(array, size);
  }

  public static void sortFlipped(long[] array, int size) {
    flip(array, size);
    Array.sort(array, size);
    flip(array, size);
  }

  //////////////////////////////////////////////////////////////////////////////

  public static boolean containsMajor(long[] array, int size, int value) {
    int low = 0;
    int high = size - 1;

    while (low <= high) {
      int midIdx = low + (high - low) / 2;
      int majorVal = Miscellanea.high(array[midIdx]);

      if (majorVal < value)
        // midIdx is below the target range
        low = midIdx + 1;
      else if (majorVal > value)
        // midIdx is above the target range
        high = midIdx - 1;
      else
        return true;
    }

    return false;
  }

  public static boolean containsMinor(long[] array, int size, int value) {
    int low = 0;
    int high = size - 1;

    while (low <= high) {
      int midIdx = low + (high - low) / 2;
      int minorVal = Miscellanea.low(array[midIdx]);

      if (minorVal < value)
        // midIdx is below the target range
        low = midIdx + 1;
      else if (minorVal > value)
        // midIdx is above the target range
        high = midIdx - 1;
      else
        return true;
    }

    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static void flip(long[] array, int size) {
    for (int i=0 ; i < size ; i++) {
      long entry = array[i];
      array[i] = Miscellanea.pack(Miscellanea.high(entry), Miscellanea.low(entry));
    }
  }
}