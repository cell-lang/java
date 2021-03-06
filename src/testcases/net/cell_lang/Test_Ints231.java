package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


public class Test_Ints231 {
  static Random rand = new Random(0);

  public static void run() {
    for (int i=0 ; i < 100000 ; i++) {
      int[] array = runRandomSortTest();
      TestSearches_Ints231.run(array, 1000);
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

    boolean[][] bitmap23 = new boolean[range][];
    for (int i=0 ; i < range ; i++)
      bitmap23[i] = new boolean[range];

    for (int i=0 ; i < len ; i++) {
      int field2 = array[3 * i + 1];
      int field3 = array[3 * i + 2];
      bitmap23[field2][field3] = true;
    }


    int[] ref = sort231(array, len);

    Ints231.sort(array, len);

    for (int i=1 ; i < len ; i++)
      if (!isOrd(array, i)) {
        System.out.println("ERROR (1)");
        System.out.printf("\n\nlen = %d, range = %d, i = %d\n\n", len, range, i);
        printArray(array, ref, len);
        System.exit(1);
      }

    for (int i=0 ; i < 3 * len ; i++)
      if (array[i] != ref[i]) {
        System.out.println("ERROR (2)");
        System.out.printf("\n\nlen = %d, range = %d, i = %d\n\n", len, range, i);
        printArray(array, ref, len);
        System.exit(1);
      }

    Miscellanea._assert(Arrays.equals(array, ref));

    for (int i2=0 ; i2 < range ; i2++)
      for (int i3=0 ; i3 < range ; i3++) {
        boolean expected = bitmap23[i2][i3];
        if (i2 == 1 & i3 == 475)
          Miscellanea.debugFlag = true;
        boolean found = Ints231.contains23(array, len, i2, i3);
        Miscellanea.debugFlag = false;
        if (found != expected) {
          System.out.println("ERROR (3)");
          System.out.printf("\n\nlen = %d, range = %d, i2 = %d, i3 = %d\n", len, range, i2, i3);
          System.out.printf("expected = %s, found = %s\n\n", expected ? "true" : "false", found ? "true" : "false");
          printArray(array, len);
          System.exit(1);
        }
      }

    return array;
  }

  static int[] sort231(int[] array, int len) {
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
    BigInteger part1 = BigInteger.valueOf(ints[offset+1]).shiftLeft(64);
    BigInteger part2 = BigInteger.valueOf(ints[offset+2]).shiftLeft(32);
    BigInteger part3 = BigInteger.valueOf(ints[offset]);
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
    ints[offset+1] = triplet.shiftRight(64).intValue();
    ints[offset+2] = triplet.shiftRight(32).intValue();
    ints[offset]   = triplet.intValue();
  }

  static boolean isOrd(int[] array, int i) {
    int offset = 3 * i;

    int prev1 = array[offset-3];
    int prev2 = array[offset-2];
    int prev3 = array[offset-1];
    int curr1 = array[offset];
    int curr2 = array[offset+1];
    int curr3 = array[offset+2];

    if (prev2 != curr2)
      return prev2 < curr2;

    if (prev3 != curr3)
      return prev3 < curr3;

    return prev1 <= curr1;
  }

  static void printArray(int[] array, int[] ref, int len) {
    for (int i=0 ; i < len ; i++)
      System.out.printf("%3d:  %5d, %5d, %5d   %5d, %5d, %5d\n",
        i, array[3*i], array[3*i+1], array[3*i+2], ref[3*i], ref[3*i+1], ref[3*i+2]);
  }

  static void printArray(int[] array, int len) {
    for (int i=0 ; i < len ; i++)
      System.out.printf("%3d:  %5d, %5d, %5d\n", i, array[3*i], array[3*i+1], array[3*i+2]);
  }
}

////////////////////////////////////////////////////////////////////////////////
////////////////////////////////////////////////////////////////////////////////

class TestSearches_Ints231 {
  static void run(int[] array, int range) {
    int len = array.length / 3;
    int[] counts2 = buildCounts2(array, range);
    int[][] counts23 = buildCounts23(array, range);

    // contains2/3, indexFirst2, count2
    for (int i=0 ; i < range ; i++) {
      int count = counts2[i];
      if (count != 0) {
        check(Ints231.contains2(array, len, i));
        int idx = Ints231.indexFirst2(array, len, i);
        check(idx != -1);
        check(Ints231.count2(array, len, i, idx) == count);
      }
      else {
        check(!Ints231.contains2(array, len, i));
        check(Ints231.indexFirst2(array, len, i) == -1);
      }
    }

    // contains23/4, indexFirst23, count23
    for (int i=0 ; i < range ; i++)
      for (int j=0 ; j < range ; j++) {
        int count = counts23[i][j];
        if (count != 0) {
          check(Ints231.contains23(array, len, i, j));
          int idx = Ints231.indexFirst23(array, len, i, j);
          check(idx != -1);
          check(Ints231.count23(array, len, i, j, idx) == count);
        }
        else {
          check(!Ints231.contains23(array, len, i, j));
          check(Ints231.indexFirst23(array, len, i, j) == -1);
        }
      }

    //## TODO: contains1/4, contains23/5
    // boolean contains1(int[] array, int offset, int count, int val1)
    // boolean contains23(int[] array, int offset, int count, int val1, int val2)
  }

  static int[] buildCounts2(int[] array, int size) {
    int[] counts = new int[size];
    for (int i=0 ; i < array.length ; i += 3)
      counts[array[i+1]]++;
    return counts;
  }

  static int[][] buildCounts23(int[] array, int size) {
    int[][] counts = new int[size][];
    for (int i=0 ; i < size ; i++)
      counts[i] = new int[size];
    for (int i=0 ; i < array.length ; i += 3)
      counts[array[i+1]][array[i+2]]++;
    return counts;
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}
