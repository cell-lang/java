package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


public class Test_Ints123 {
  static Random rand = new Random(0);

  public static void run() {
    for (int i=0 ; i < 100000 ; i++) {
      int[] array = runRandomSortTest();
      TestSearches_Ints123.run(array, 1000);
      if ((i + 1) % 100 == 0)
        System.out.print('.');
    }
    System.out.println();
  }

  static int[] runRandomSortTest() {
    int len = rand.nextInt(200);
    int[] array = new int[3 * len];

    int range = 1 + rand.nextInt(1000);
    for (int i=0 ; i < 3 * len ; i++)
      array[i] = rand.nextInt(range);

    boolean[][] bitmap12 = new boolean[range][];
    for (int i=0 ; i < range ; i++)
      bitmap12[i] = new boolean[range];

    for (int i=0 ; i < len ; i++) {
      int field1 = array[3 * i];
      int field2 = array[3 * i + 1];
      bitmap12[field1][field2] = true;
    }

    int[] ref = sort123(array, len);

    Ints123.sort(array, len);

    for (int i=1 ; i < len ; i++)
      if (!isOrd(array, i)) {
        System.out.println("ERROR (1)");
        System.exit(1);
      }

    for (int i=0 ; i < 3 * len ; i++)
      if (array[i] != ref[i]) {
        System.out.println("ERROR (2)");
        System.out.printf("\n\nlen = %d, range = %d, i = %d\n\n", len, range, i);
        for (int j=0 ; j < len ; j++)
          System.out.printf("%3d:  %5d, %5d, %5d   %5d, %5d, %5d\n",
            j, array[3*j], array[3*j+1], array[3*j+2], ref[3*j], ref[3*j+1], ref[3*j+2]);
        System.exit(1);
      }

    Miscellanea._assert(Arrays.equals(array, ref));

    for (int i=0 ; i < range ; i++)
      for (int j=0 ; j < range ; j++) {
        boolean expected = bitmap12[i][j];
        boolean found = Ints123.contains12(array, len, i, j);
        if (found != expected) {
          System.out.println("ERROR (3)");
          System.exit(1);
        }
      }

    return array;
  }

  static int[] sort123(int[] array, int len) {
    BigInteger[] triplets = new BigInteger[len];
    for (int i=0 ; i < len ; i++)
      triplets[i] = pack3(array, 3 * i);

    Arrays.sort(triplets);

    int[] res = new int[3 * len];
    for (int i=0 ; i < len ; i++)
      unpack3(triplets[i], res, 3 * i);

    return res;
  }

  static BigInteger pack3(int[] ints, int offset) {
    BigInteger part1 = BigInteger.valueOf(ints[offset]).shiftLeft(64);
    BigInteger part2 = BigInteger.valueOf(ints[offset+1]).shiftLeft(32);
    BigInteger part3 = BigInteger.valueOf(ints[offset+2]);
    BigInteger res = part1.add(part2).add(part3);

    int[] rec = new int[3];
    unpack3(res, rec, 0);
    if (ints[offset] != rec[0] | ints[offset+1] != rec[1] | ints[offset+2] != rec[2]) {
      System.out.println("ERROR (100)");
      System.exit(1);
    }

    return res;
  }

  static void unpack3(BigInteger triplet, int[] ints, int offset) {
    ints[offset] = triplet.shiftRight(64).intValue();
    ints[offset+1] = triplet.shiftRight(32).intValue();
    ints[offset+2] = triplet.intValue();
  }

  static boolean isOrd(int[] array, int i) {
    int offset = 3 * i;

    int prev1 = array[offset-3];
    int prev2 = array[offset-2];
    int prev3 = array[offset-1];
    int curr1 = array[offset];
    int curr2 = array[offset+1];
    int curr3 = array[offset+2];

    if (prev1 != curr1)
      return prev1 < curr1;

    if (prev2 != curr2)
      return prev2 < curr2;

    return prev3 <= curr3;
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class TestSearches_Ints123 {
  static void run(int[] array, int range) {
    int len = array.length / 3;
    int[] counts1 = buildCounts1(array, range);
    int[][] counts12 = buildCounts12(array, range);

    // contains1/3, indexFirst1, count1
    for (int i=0 ; i < range ; i++) {
      int count = counts1[i];
      if (count != 0) {
        check(Ints123.contains1(array, len, i));
        int idx = Ints123.indexFirst1(array, len, i);
        check(idx != -1);
        check(Ints123.count1(array, len, i, idx) == count);
      }
      else {
        check(!Ints123.contains1(array, len, i));
        check(Ints123.indexFirst1(array, len, i) == -1);
      }
    }

    // contains12/4, indexFirst12, count12
    for (int i=0 ; i < range ; i++)
      for (int j=0 ; j < range ; j++) {
        int count = counts12[i][j];
        if (count != 0) {
          check(Ints123.contains12(array, len, i, j));
          int idx = Ints123.indexFirst12(array, len, i, j);
          check(idx != -1);
          check(Ints123.count12(array, len, i, j, idx) == count);
        }
        else {
          check(!Ints123.contains12(array, len, i, j));
          check(Ints123.indexFirst12(array, len, i, j) == -1);
        }
      }

    //## TODO: contains1/4, contains12/5
    // boolean contains1(int[] array, int offset, int count, int val1)
    // boolean contains12(int[] array, int offset, int count, int val1, int val2)
  }

  static int[] buildCounts1(int[] array, int size) {
    int[] counts = new int[size];
    for (int i=0 ; i < array.length ; i += 3)
      counts[array[i]]++;
    return counts;
  }

  static int[][] buildCounts12(int[] array, int size) {
    int[][] counts = new int[size][];
    for (int i=0 ; i < size ; i++)
      counts[i] = new int[size];
    for (int i=0 ; i < array.length ; i += 3)
      counts[array[i]][array[i+1]]++;
    return counts;
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}
