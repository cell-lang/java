package net.cell_lang;

import java.util.Random;
import java.util.Arrays;


class Test_Sym12TernaryTable {
  static Random random = new Random(0);

  public static void run() {
    for (int i=1 ; i <= 24 ; i++) {
      for (int j=0 ; j < 100 ; j++) {
        run(i, false);
      }
      System.out.printf("%d\n", i);
    }

    System.out.println();

    for (int i=1 ; i < 27 ; i++) {
      run(i, false);
      System.out.printf("%d\n", i);
    }
  }

  public static void run(int range, boolean trace) {
    Sym12TernaryTable table = new Sym12TernaryTable(null, null);

    boolean[][][] bitMap = new boolean[range][][];
    for (int i=0 ; i < range ; i++) {
      bitMap[i] = new boolean[range][];
      for (int j=0 ; j < range ; j++)
        bitMap[i][j] = new boolean[range];
    }
    checkTable(table, bitMap, range);

    int numOfOps = ((range + 1) * range * range) / 2;

    // Inserting until the table is full
    for (int i=0 ; i < numOfOps ; i++) {
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
      bitMap[s2][s1][s3] = true;

      checkTable(table, bitMap, range);
    }

    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=0 ; s2 < range ; s2++)
        for (int s3=0 ; s3 < range ; s3++)
          if (!bitMap[s1][s2][s3]) {
            System.out.println("ERROR - LINE 65");
            System.exit(1);
          }

    // Deleting until the table is empty
    for (int i=0 ; i < numOfOps ; i++) {
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
        System.out.printf("Deleting:  (%d, %d, %d)\n", s1, s2, s3);

      table.delete(s1, s2, s3);
      bitMap[s1][s2][s3] = false;
      bitMap[s2][s1][s3] = false;

      checkTable(table, bitMap, range);
    }
  }

  static void checkTable(Sym12TernaryTable table, boolean[][][] bitMap, int range) {
    // table.check();

    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=0 ; s2 < range ; s2++)
        for (int s3=0 ; s3 < range ; s3++)
          if (table.contains(s1, s2, s3) != bitMap[s1][s2][s3]) {
            System.out.println("Error (_, _, _)!\n");
            printDiffs(table, bitMap, range);
            System.exit(1);
          }

    { int expectedCount = 0;
      for (int i=0 ; i < range ; i++)
        for (int j=0 ; j <= i ; j++)
          for (int k=0 ; k < range ; k++)
            if (bitMap[i][j][k])
              expectedCount++;

      int actualCount = 0;

      Sym12TernaryTable.Iter it = table.getIter();
      while (!it.done()) {
        int arg1 = it.get1();
        int arg2 = it.get2();
        int arg3 = it.get3();
        if (!bitMap[arg1][arg2][arg3] | !bitMap[arg2][arg1][arg3]) {
          System.out.println("ERROR (?, ?, ?) - A\n");
          System.exit(1);
        }
        actualCount++;
        it.next();
      }

      if (actualCount != expectedCount) {
        System.out.println("ERROR (?, ?, ?) - B\n");
        System.out.printf("Actual count = %d, expected = %d\n\n", actualCount, expectedCount);
        printDiffs(table, bitMap, range);
        System.exit(1);
      }

      if (table.size() != expectedCount) {
        System.out.println("ERROR (?, ?, ?) - B\n");
        System.exit(1);
      }
    }

    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=0 ; s2 < range ; s2++) {
        int[] expected = match12(bitMap, s1, s2, range);
        int[] actual = match12(table, s1, s2, range);

        if (table.count12(s1, s2) != expected.length) {
          System.out.println("Error (_, _, ?) - A\n");
          System.exit(1);
        }

        if (table.contains12(s1, s2) != (expected.length > 0)) {
          System.out.println("Error (_, _, ?) - A\n");
          System.exit(1);
        }

        if (!Arrays.equals(actual, expected)) {
          System.out.println("Error (_, _, ?) - B\n");
          // System.out.printf("s1 = %d, s2 = %d\n", s1, s2);
          // System.out.print("actual   = ");
          // for (int i=0 ; i < actual.length ; i++)
          //   System.out.printf("%d ", actual[i]);
          // System.out.println();
          // System.out.print("expected = ");
          // for (int i=0 ; i < expected.length ; i++)
          //   System.out.printf("%d ", expected[i]);
          // System.out.println();

          // actual = match12(table, s1, s2, range);
          // System.out.print("actual   = ");
          // for (int i=0 ; i < actual.length ; i++)
          //   System.out.printf("%d ", actual[i]);
          // System.out.println();


          System.exit(1);
        }
      }

    for (int s1=0 ; s1 < range ; s1++)
      for (int s3=0 ; s3 < range ; s3++) {
        int[] expected = match13(bitMap, s1, s3, range);
        int[] actual = match13(table, s1, s3, range);

        if (table.count_13_23(s1, s3) != expected.length) {
          System.out.println("Error (_, ?, _) - A\n");
          System.exit(1);
        }

        if (table.contains_13_23(s1, s3) != (expected.length > 0)) {
          System.out.println("Error (_, ?, _) - A\n");
          System.exit(1);
        }

        if (!Arrays.equals(actual, expected)) {
          System.out.println("Error (_, ?, _) - B\n");

          System.out.printf("s1 = %d, s3 = %d\n", s1, s3);
          System.out.print("actual   = ");
          for (int i=0 ; i < actual.length ; i++)
            System.out.printf("%d ", actual[i]);
          System.out.println();
          System.out.print("expected = ");
          for (int i=0 ; i < expected.length ; i++)
            System.out.printf("%d ", expected[i]);
          System.out.println();

          actual = match13(table, s1, s3, range);
          System.out.print("actual   = ");
          for (int i=0 ; i < actual.length ; i++)
            System.out.printf("%d ", actual[i]);
          System.out.println();

          System.exit(1);
        }
      }

    for (int s2=0 ; s2 < range ; s2++)
      for (int s3=0 ; s3 < range ; s3++) {
        int[] expected = match23(bitMap, s2, s3, range);
        int[] actual = match23(table, s2, s3, range);

        if (table.count_13_23(s2, s3) != expected.length) {
          System.out.println("Error (?, _, _) - A\n");
          System.exit(1);
        }

        if (table.contains_13_23(s2, s3) != (expected.length > 0)) {
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

      if (2 * table.count_1_2(s1) != expected.length) {
        System.out.printf("Error (%d, ?, ?) - A\n", s1);
        System.out.printf("table.count_1_2(%d) = %d, expected = %d\n", s1, table.count_1_2(s1), expected.length);
        printDiffs(table, bitMap, range);
        System.out.printf("expected = %s\n", Arrays.toString(expected));
        System.exit(1);
      }

      if (table.contains_1_2(s1) != (expected.length > 0)) {
        System.out.println("Error (_, ?, ?) - A\n");
        System.exit(1);
      }

      if (!Arrays.equals(actual, expected)) {
        System.out.println("Error (_, ?, ?) - B\n");
        printDiffs(table, bitMap, range);

        System.out.printf("range = %d, s1 = %d\n", range, s1);
        System.out.printf("expected: %s\n", Arrays.toString(expected));
        System.out.printf("actual: %s\n", Arrays.toString(actual));

        actual = match1(table, s1, range);

        System.exit(1);
      }
    }

    for (int s2=0 ; s2 < range ; s2++) {
      int[] expected = match2(bitMap, s2, range);
      int[] actual = match2(table, s2, range);

      if (2 * table.count_1_2(s2) != expected.length) {
        System.out.println("Error (?, _, ?) - A\n");
        System.exit(1);
      }

      if (table.contains_1_2(s2) != (expected.length > 0)) {
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

      if (2 * table.count3(s3) != expected.length) {
        System.out.println("Error (?, ?, _) - A\n");
        System.exit(1);
      }

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

  static int[] match12(Sym12TernaryTable table, int s1, int s2, int range) {
    int count = 0;
    int[] buffer = new int[range];
    Sym12TernaryTable.Iter it = table.getIter12(s1, s2);
    while (!it.done()) {
      buffer[count++] = it.get1();
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

  static int[] match13(Sym12TernaryTable table, int s1, int s3, int range) {
    int count = 0;
    int[] buffer = new int[range];
    Sym12TernaryTable.Iter it = table.getIter_13_23(s1, s3);
    while (!it.done()) {
      if (count >= range) {
        for (int i=0 ; i < buffer.length ; i++)
          System.out.printf("%d ", buffer[i]);
        System.out.println();
      }
      buffer[count++] = it.get1();
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

  static int[] match23(Sym12TernaryTable table, int s2, int s3, int range) {
    int count = 0;
    int[] buffer = new int[range];
    Sym12TernaryTable.Iter it = table.getIter_13_23(s2, s3);
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

  static int[] match1(Sym12TernaryTable table, int s1, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    Sym12TernaryTable.Iter it = table.getIter_1_2(s1);
    while (!it.done()) {
      buffer[count++] = it.get1();
      buffer[count++] = it.get2();
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

  static int[] match2(Sym12TernaryTable table, int s2, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    Sym12TernaryTable.Iter it = table.getIter_1_2(s2);
    while (!it.done()) {
      buffer[count++] = it.get1();
      buffer[count++] = it.get2();
      it.next();
    }
    return sort2(Arrays.copyOf(buffer, count));
  }

  static int[] match3(boolean[][][] bitMap, int s3, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    for (int s1=0 ; s1 < range ; s1++)
      for (int s2=s1 ; s2 < range ; s2++)
        if (bitMap[s1][s2][s3]) {
          buffer[count++] = s1;
          buffer[count++] = s2;
        }
    return Arrays.copyOf(buffer, count);
  }

  static int[] match3(Sym12TernaryTable table, int s3, int range) {
    int count = 0;
    int[] buffer = new int[2 * range * range];
    Sym12TernaryTable.Iter it = table.getIter3(s3);
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

  static void printDiffs(Sym12TernaryTable table, boolean[][][] bitMap, int range) {
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

    System.out.println();
    System.out.println();

    for (int s1=0 ; s1 < range ; s1++) {
      for (int s2=0 ; s2 < range ; s2++) {
        for (int s3=0 ; s3 < range ; s3++) {
          boolean actual = table.contains(s1, s2, s3);
          boolean expected = bitMap[s1][s2][s3];
          if (expected)
            System.out.printf("%2d, %2d, %2d  %s\n", s1, s2, s3, actual ? "" : "ERROR");
        }
      }
    }

    System.out.println();
  }
}
