package net.cell_lang;

import java.util.Random;
import java.util.Arrays;


public class Test_Ints21 {
  static Random rand = new Random(0);

  public static void run() {
    for (int i=0 ; i < 100000 ; i++) {
      int[] array = runRandomSortTest();
      TestSearches_Ints21.run(array);
      if ((i + 1) % 100 == 0)
        System.out.print('.');
    }
    System.out.println();
  }

  static int[] runRandomSortTest() {
    int len = rand.nextInt(200);
    int[] array = new int[2 * len];

    int range = 1 + rand.nextInt(1000);
    for (int i=0 ; i < 2 * len ; i++)
      array[i] = rand.nextInt(range);

    boolean[] bitmap2 = new boolean[range];
    for (int i=0 ; i < len ; i++) {
      int field2 = array[2 * i + 1];
      bitmap2[field2] = true;
    }

    int[] ref = sort21(array, len);

    Ints21.sort(array, len);

    for (int i=1 ; i < len ; i++)
      if (!isOrd(array, i)) {
        System.out.println("ERROR (1)");
        for (int j=0 ; j < len ; j++)
          System.out.printf("%3d:  %5d, %5d   %5d, %5d\n", j, array[2*j], array[2*j+1], ref[2*j], ref[2*j+1]);
        System.exit(1);
      }

    for (int i=0 ; i < 2 * len ; i++)
      if (array[i] != ref[i]) {
        System.out.println("ERROR (2)");
        System.out.printf("\n\nlen = %d, range = %d, i = %d\n\n", len, range, i);
        for (int j=0 ; j < len ; j++)
          System.out.printf("%3d:  %5d, %5d   %5d, %5d\n", j, array[2*j], array[2*j+1], ref[2*j], ref[2*j+1]);
        System.exit(1);
      }

    Miscellanea._assert(Arrays.equals(array, ref));

    for (int i=0 ; i < range ; i++) {
      boolean expected = bitmap2[i];
      boolean found = Ints21.contains2(array, len, i);
      if (found != expected) {
        System.out.println("ERROR (3)");
        System.exit(1);
      }
    }

    return array;
  }

  static int[] sort21(int[] array, int len) {
    long[] pairs = new long[len];
    for (int i=0 ; i < len ; i++)
      pairs[i] = pack2(array, 2 * i);

    Arrays.sort(pairs);

    int[] res = new int[2 * len];
    for (int i=0 ; i < len ; i++)
      unpack2(pairs[i], res, 2 * i);

    return res;
  }

  static long pack2(int[] ints, int offset) {
    long res = (((long) ints[offset + 1]) << 32) + ints[offset];

    int[] rec = new int[2];
    unpack2(res, rec, 0);
    if (ints[offset] != rec[0] | ints[offset+1] != rec[1]) {
      System.out.println("ERROR (100)");
      System.exit(1);
    }

    return res;
  }

  static void unpack2(long pair, int[] ints, int offset) {
    ints[offset]   = (int) pair;
    ints[offset+1] = (int) (pair >>> 32);
  }

  static boolean isOrd(int[] array, int i) {
    int offset = 2 * i;

    int prev1 = array[offset-2];
    int prev2 = array[offset-1];
    int curr1 = array[offset];
    int curr2 = array[offset+1];

    if (prev2 != curr2)
      return prev2 < curr2;
    else
      return prev1 <= curr1;
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class TestSearches_Ints21 {
  static void run(int[] array) {
    int range = 1000;
    int len = array.length / 2;
    boolean[][] bitmap = buildBitmap(array, range);
    int[] counts1 = buildCounts1(array, range);
    int[] counts2 = buildCounts2(array, range);

    // contains
    for (int i=0 ; i < range ; i++)
      for (int j=0 ; j < range ; j++)
        check(Ints21.contains(array, len, i, j) == bitmap[i][j]);

    // contains2/3, indexFirst2, count2
    for (int i=0 ; i < range ; i++) {
      int count = counts2[i];
      if (count != 0) {
        check(Ints21.contains2(array, len, i));
        int idx = Ints21.indexFirst2(array, len, i);
        check(idx != -1);
        check(Ints21.count2(array, len, i, idx) == count);
      }
      else {
        check(!Ints21.contains2(array, len, i));
        check(Ints21.indexFirst2(array, len, i) == -1);
      }
    }

    //## TODO: contains2/4
    // boolean contains2(int[] array, int offset, int count, int val2)
  }

  static boolean[][] buildBitmap(int[] array, int size) {
    boolean[][] bitmap = new boolean[size][];
    for (int i=0 ; i < size ; i++)
      bitmap[i] = new boolean[size];
    for (int i=0 ; i < array.length ; i += 2)
      bitmap[array[i]][array[i+1]] = true;
    return bitmap;
  }

  static int[] buildCounts1(int[] array, int size) {
    int[] counts = new int[size];
    for (int i=0 ; i < array.length ; i += 2)
      counts[array[i]]++;
    return counts;
  }

  static int[] buildCounts2(int[] array, int size) {
    int[] counts = new int[size];
    for (int i=0 ; i < array.length ; i += 2)
      counts[array[i+1]]++;
    return counts;
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}
