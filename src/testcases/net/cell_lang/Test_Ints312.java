package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


public class Test_Ints312 {
  static Random rand = new Random(0);

  public static void run() {
    for (int i=0 ; i < 100000 ; i++) {
      runRandomTest();
      if ((i + 1) % 100 == 0)
        System.out.print('.');
    }
    System.out.println();
  }

  static void runRandomTest() {
    int len = rand.nextInt(200);
    int[] array = new int[3 * len];

    int range = 1 + rand.nextInt(1000);
    for (int i=0 ; i < 3 * len ; i++)
      array[i] = rand.nextInt(range);

    boolean[][] bitmap31 = new boolean[range][];
    for (int i=0 ; i < range ; i++)
      bitmap31[i] = new boolean[range];
    for (int i=0 ; i < len ; i++) {
      int field1 = array[3 * i];
      int field3 = array[3 * i + 2];
      bitmap31[field3][field1] = true;
    }

    boolean[] bitmap3 = new boolean[range];
    for (int i=0 ; i < len ; i++) {
      int field3 = array[3 * i + 2];
      bitmap3[field3] = true;
    }

    int[] ref = sort312(array, len);

    Ints312.sort(array, len);

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

    for (int i3=0 ; i3 < range ; i3++)
      for (int i1=0 ; i1 < range ; i1++) {
        boolean expected = bitmap31[i3][i1];
        boolean found = Ints312.contains13(array, len, i1, i3);
        if (found != expected) {
          System.out.println("ERROR (3)");
          System.exit(1);
        }
      }

    for (int i3=0 ; i3 < range ; i3++) {
      boolean expected = bitmap3[i3];
      boolean found = Ints312.contains3(array, len, i3);
      if (found != expected) {
        System.out.println("ERROR (4)");
        System.exit(1);
      }
    }
  }

  static int[] sort312(int[] array, int len) {
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
    BigInteger part1 = BigInteger.valueOf(ints[offset+2]).shiftLeft(64);
    BigInteger part2 = BigInteger.valueOf(ints[offset]).shiftLeft(32);
    BigInteger part3 = BigInteger.valueOf(ints[offset+1]);
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
    ints[offset+2] = triplet.shiftRight(64).intValue();
    ints[offset]   = triplet.shiftRight(32).intValue();
    ints[offset+1] = triplet.intValue();
  }

  static boolean isOrd(int[] array, int i) {
    int offset = 3 * i;

    int prev1 = array[offset-3];
    int prev2 = array[offset-2];
    int prev3 = array[offset-1];
    int curr1 = array[offset];
    int curr2 = array[offset+1];
    int curr3 = array[offset+2];

    if (prev3 != curr3)
      return prev3 < curr3;

    if (prev1 != curr1)
      return prev1 < curr1;

    return prev2 <= curr2;
  }
}
