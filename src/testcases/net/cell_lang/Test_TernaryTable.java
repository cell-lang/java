package net.cell_lang;

import java.util.Random;
import java.util.Arrays;


class Test_TernaryTable {
  static Random random = new Random(0);

  public static void run() {
    for (int i=1 ; i < 50 ; i++) {
      // for (int j=0 ; j < 100 ; j++) {
        run(i, false);
      // }
        System.out.printf("%d\n", i);
    }

    // for (int i=1 ; i < 240 ; i++) {
    //   run(i, false);
    // }
  }

  public static void run(int range, boolean trace) {
    TernaryTable table = new TernaryTable(null, null, null);

    boolean[][][] bitMap = new boolean[range][][];
    for (int i=0 ; i < range ; i++) {
      bitMap[i] = new boolean[range][];
      for (int j=0 ; j < range ; j++)
        bitMap[i][j] = new boolean[range];
    }
    checkTable(table, bitMap, range);

    // Inserting until the table is full
    for (int i=0 ; i < range * range * range ; i++) {
      int s1 = random.nextInt(range);
      int s2 = random.nextInt(range);
      int s3 = random.nextInt(range);

      while (bitMap[s1][s2][s3]) {
        s3 = (s3 + 1) % range;
        if (s3 == 0) {
          s2 = (s2 + 1) % range;
          if (s2 == 0)
            s1 = (s1 + 1) % range;
        }
      }

      if (trace)
        System.out.printf("Inserting: (%d, %d, %d)\n", s1, s2, s3);

      table.insert(s1, s2, s3);
      bitMap[s1][s2][s3] = true;

      checkTable(table, bitMap, range);
    }

    // Deleting until the table is empty
    for (int i=0 ; i < range * range * range ; i++) {
      int s1 = random.nextInt(range);
      int s2 = random.nextInt(range);
      int s3 = random.nextInt(range);

      while (!bitMap[s1][s2][s3]) {
        s3 = (s3 + 1) % range;
        if (s3 == 0) {
          s2 = (s2 + 1) % range;
          if (s2 == 0)
            s1 = (s1 + 1) % range;
        }
      }

      if (trace)
        System.out.printf("Deleting: (%d, %d, %d)", s1, s2, s3);

      table.delete(s1, s2, s3);
      bitMap[s1][s2][s3] = false;

      checkTable(table, bitMap, range);
    }
  }

  static void checkTable(TernaryTable table, boolean[][][] bitMap, int range) {
    // table.check();

    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=0 ; s2 < range ; s2++)
        for (int s3=0 ; s3 < range ; s3++)
          if (table.contains(s1, s2, s3) != bitMap[s1][s2][s3]) {
            System.out.println("Error (_, _, _)!\n");
            printDiffs(table, bitMap, range);
            System.exit(1);
          }

    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=0 ; s2 < range ; s2++) {
        int[] expected = match12(bitMap, s1, s2, range);
        int[] actual = match12(table, s1, s2, range);

        if (table.contains12(s1, s2) != (expected.length > 0)) {
          System.out.println("Error (_, _, ?) - A\n");
          System.exit(1);
        }

        if (!Arrays.equals(actual, expected)) {
          System.out.println("Error (_, _, ?) - B\n");
          System.exit(1);
        }
      }

    for (int s1=0 ; s1 < range ; s1++)
      for (int s3=0 ; s3 < range ; s3++) {
        int[] expected = match13(bitMap, s1, s3, range);
        int[] actual = match13(table, s1, s3, range);

        if (table.contains13(s1, s3) != (expected.length > 0)) {
          System.out.println("Error (_, ?, _) - A\n");
          System.exit(1);
        }

        if (!Arrays.equals(actual, expected)) {
          System.out.println("Error (_, ?, _) - B\n");
          System.exit(1);
        }
      }

    for (int s2=0 ; s2 < range ; s2++)
      for (int s3=0 ; s3 < range ; s3++) {
        int[] expected = match23(bitMap, s2, s3, range);
        int[] actual = match23(table, s2, s3, range);

        if (table.contains23(s2, s3) != (expected.length > 0)) {
          System.out.println("Error (?, _, _) - A\n");
          System.exit(1);
        }

        if (!Arrays.equals(actual, expected)) {
          System.out.println("Error (?, _, _) - B\n");
          System.exit(1);
        }
      }

    for (int s1=0 ; s1 < range ; s1++) {
      int[] expected = match1(bitMap, s1, range);
      int[] actual = match1(table, s1, range);

      if (table.contains1(s1) != (expected.length > 0)) {
        System.out.println("Error (_, ?, ?) - A\n");
        System.exit(1);
      }

      if (!Arrays.equals(actual, expected)) {
        System.out.println("Error (_, ?, ?) - B\n");
        printDiffs(table, bitMap, range);

        System.out.printf("range = %d, s1 = %d\n", range, s1);
        System.out.printf("expected: %s\n", Arrays.toString(expected));
        System.out.printf("actual: %s\n", Arrays.toString(actual));
        System.exit(1);
      }
    }

    for (int s2=0 ; s2 < range ; s2++) {
      int[] expected = match2(bitMap, s2, range);
      int[] actual = match2(table, s2, range);

      if (table.contains2(s2) != (expected.length > 0)) {
        System.out.println("Error (?, _, ?) - A\n");
        System.exit(1);
      }

      if (!Arrays.equals(actual, expected)) {
        System.out.println("Error (?, _, ?) - B\n");
        System.exit(1);
      }
    }

    for (int s3=0 ; s3 < range ; s3++) {
      int[] expected = match3(bitMap, s3, range);
      int[] actual = match3(table, s3, range);

      if (table.contains3(s3) != (expected.length > 0)) {
        System.out.println("Error (?, ?, _) - A\n");
        System.exit(1);
      }

      if (!Arrays.equals(actual, expected)) {
        System.out.println("Error (?, ?, _) - B\n");
        System.exit(1);
      }
    }
  }

  static int[] match12(boolean[][][] bitMap, int s1, int s2, int range) {
    int count = 0;
    int[] buffer = new int[range];
    for (int s3=0 ; s3 < range ; s3++)
      if (bitMap[s1][s2][s3])
        buffer[count++] = s3;
    return Arrays.copyOf(buffer, count);
  }

  static int[] match12(TernaryTable table, int s1, int s2, int range) {
    int count = 0;
    int[] buffer = new int[range];
    TernaryTable.Iter it = table.getIter12(s1, s2);
    while (!it.done()) {
      buffer[count++] = it.get3();
      it.next();
    }
    buffer = Arrays.copyOf(buffer, count);
    Arrays.sort(buffer);
    return buffer;
  }

  static int[] match13(boolean[][][] bitMap, int s1, int s3, int range) {
    int count = 0;
    int[] buffer = new int[range];
    for (int s2=0 ; s2 < range ; s2++)
      if (bitMap[s1][s2][s3])
        buffer[count++] = s2;
    return Arrays.copyOf(buffer, count);
  }

  static int[] match13(TernaryTable table, int s1, int s3, int range) {
    int count = 0;
    int[] buffer = new int[range];
    TernaryTable.Iter it = table.getIter13(s1, s3);
    while (!it.done()) {
      buffer[count++] = it.get2();
      it.next();
    }
    buffer = Arrays.copyOf(buffer, count);
    Arrays.sort(buffer);
    return buffer;
  }

  static int[] match23(boolean[][][] bitMap, int s2, int s3, int range) {
    int count = 0;
    int[] buffer = new int[range];
    for (int s1=0 ; s1 < range ; s1++)
      if (bitMap[s1][s2][s3])
        buffer[count++] = s1;
    return Arrays.copyOf(buffer, count);
  }

  static int[] match23(TernaryTable table, int s2, int s3, int range) {
    int count = 0;
    int[] buffer = new int[range];
    TernaryTable.Iter it = table.getIter23(s2, s3);
    while (!it.done()) {
      buffer[count++] = it.get1();
      it.next();
    }
    buffer = Arrays.copyOf(buffer, count);
    Arrays.sort(buffer);
    return buffer;
  }

  static int[] match1(boolean[][][] bitMap, int s1, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    for (int s2=0 ; s2 < range ; s2++)
      for (int s3=0 ; s3 < range ; s3++)
        if (bitMap[s1][s2][s3]) {
          buffer[count++] = s2;
          buffer[count++] = s3;
        }
    return Arrays.copyOf(buffer, count);
  }

  static int[] match1(TernaryTable table, int s1, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    TernaryTable.Iter it = table.getIter1(s1);
    while (!it.done()) {
      buffer[count++] = it.get2();
      buffer[count++] = it.get3();
      it.next();
    }
    return sort2(Arrays.copyOf(buffer, count));
  }

  static int[] match2(boolean[][][] bitMap, int s2, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    for (int s1=0 ; s1 < range ; s1++)
      for (int s3=0 ; s3 < range ; s3++)
        if (bitMap[s1][s2][s3]) {
          buffer[count++] = s1;
          buffer[count++] = s3;
        }
    return Arrays.copyOf(buffer, count);
  }

  static int[] match2(TernaryTable table, int s2, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    TernaryTable.Iter it = table.getIter2(s2);
    while (!it.done()) {
      buffer[count++] = it.get1();
      buffer[count++] = it.get3();
      it.next();
    }
    return sort2(Arrays.copyOf(buffer, count));
  }

  static int[] match3(boolean[][][] bitMap, int s3, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=0 ; s2 < range ; s2++)
        if (bitMap[s1][s2][s3]) {
          buffer[count++] = s1;
          buffer[count++] = s2;
        }
    return Arrays.copyOf(buffer, count);
  }

  static int[] match3(TernaryTable table, int s3, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    TernaryTable.Iter it = table.getIter3(s3);
    while (!it.done()) {
      buffer[count++] = it.get1();
      buffer[count++] = it.get2();
      it.next();
    }
    return sort2(Arrays.copyOf(buffer, count));
  }

  static int[] sort2(int[] pairsArray) {
    long[] packed = pack2(pairsArray);
    Arrays.sort(packed);
    return unpack2(packed);
  }

  static long[] pack2(int[] pairsArray) {
    Miscellanea._assert(pairsArray.length % 2 == 0);
    int len = pairsArray.length / 2;
    long[] packed = new long[len];
    for (int i=0 ; i < len ; i++) {
      long mostSign = pairsArray[2 * i];
      long leastSign = pairsArray[2 * i + 1];
      packed[i] = (mostSign << 32) + leastSign;
    }
    Miscellanea._assert(Arrays.equals(unpack2(packed), pairsArray));
    return packed;
  }

  static int[] unpack2(long[] array) {
    int len = array.length;
    int[] unpacked = new int[2 * len];
    for (int i=0 ; i < len ; i++) {
      long pair = array[i];
      unpacked[2 * i] = (int) (pair >>> 32);
      unpacked[2 * i + 1] = (int) pair;
    }
    return unpacked;
  }

  static void printDiffs(TernaryTable table, boolean[][][] bitMap, int range) {
    for (int s1=0 ; s1 < range ; s1++) {
      for (int s2=0 ; s2 < range ; s2++) {
        for (int s3=0 ; s3 < range ; s3++) {
          int actual = table.contains(s1, s2, s3) ? 1 : 0;
          int expected = bitMap[s1][s2][s3] ? 1 : 0;
          System.out.printf("%d/%d ", actual, expected);
        }
        System.out.println();
      }
      System.out.println();
    }
  }
}

// for (int k=0 ; k < expValues.length ; k++)
// System.out.printf("%d ", expValues[k]);
// System.out.println();
// System.out.println(table.containsField1(i) ? "true" : "false");
