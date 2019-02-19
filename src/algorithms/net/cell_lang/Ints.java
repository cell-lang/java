package net.cell_lang;

import java.util.Arrays;


class Ints {
  public static void sort(int[] array) {
    Arrays.sort(array);
  }

  public static boolean contains(int[] array, int value) {
    return Arrays.binarySearch(array, value) >= 0;
  }

  public static boolean contains(int[] array, int count, int value) {
    return Arrays.binarySearch(array, 0, count, value) >= 0;
  }
}
