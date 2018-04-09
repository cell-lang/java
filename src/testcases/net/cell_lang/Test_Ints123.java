package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


public class Test_Ints123 {
  static Random rand = new Random(0);

  public static void main(String[] args) {
    for (int i=0 ; i < 100000 ; i++) {
      runInts123RandomTest();
      // if ((i + 1) % 100 == 0)
      //   System.out.print('.');
    }
    // System.out.println();
  }

  static void runInts123RandomTest() {
    int len = rand.nextInt(200);
    int[] array = new int[3 * len];

    int range = 1 + rand.nextInt(1000);
    for (int i=0 ; i < 3 * len ; i++)
      array[i] = rand.nextInt(range);

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
      System.out.println("ERROR (3)");
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
