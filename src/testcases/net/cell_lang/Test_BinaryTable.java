package net.cell_lang;

import java.util.Random;
import java.util.Arrays;


class Test_BinaryTable {
  static Random random = new Random(0);

  public static void run() {
    for (int i=1 ; i < 50 ; i++) {
      for (int j=0 ; j < 100 ; j++) {
        run(i, false);
      }
      System.out.printf("%d%n", i);
    }

    for (int i=1 ; i < 240 ; i++) {
      run(i, false);
      System.out.printf("%d%n", i);
    }
  }

  public static void run(int range, boolean trace) {
    BinaryTable table = new BinaryTable(null, null);

    boolean[][] bitMap = new boolean[range][];
    for (int i=0 ; i < range ; i++)
      bitMap[i] = new boolean[range];
    checkTable(table, bitMap, range);

    // Inserting until the table is full
    for (int i=0 ; i < range * range ; i++) {
      int surr1 = random.nextInt(range);
      int surr2 = random.nextInt(range);

      while (bitMap[surr1][surr2]) {
        surr2 = (surr2 + 1) % range;
        if (surr2 == 0)
          surr1 = (surr1 + 1) % range;
      }

      if (trace)
        System.out.printf("Inserting: (%d, %d)\n", surr1, surr2);

      table.insert(surr1, surr2);
      bitMap[surr1][surr2] = true;

      checkTable(table, bitMap, range);
    }

    // Deleting until the table is empty
    for (int i=0 ; i < range * range ; i++) {
      int surr1 = random.nextInt(range);
      int surr2 = random.nextInt(range);

      while (!bitMap[surr1][surr2]) {
        surr2 = (surr2 + 1) % range;
        if (surr2 == 0)
          surr1 = (surr1 + 1) % range;
      }

      if (trace)
        System.out.printf("Deleting: (%d, %d)", surr1, surr2);

      table.delete(surr1, surr2);
      bitMap[surr1][surr2] = false;

      checkTable(table, bitMap, range);
    }
  }

  static void checkTable(BinaryTable table, boolean[][] bitMap, int size) {
    table.check();

    for (int i=0 ; i < size ; i++)
      for (int j=0 ; j < size ; j++)
        if (table.contains(i, j) != bitMap[i][j]) {
          System.out.println("ERROR (1)!\n");
          printDiffs(table, bitMap, size);
          //throw new Exception();
          System.exit(1);
        }

    for (int i=0 ; i < size ; i++) {
      int count = 0;
      int[] list = new int[size];
      for (int j=0 ; j < size ; j++)
        if (bitMap[i][j])
          list[count++] = j;
      int[] expValues = Arrays.copyOf(list, count);

      if (table.contains1(i) != (expValues.length > 0)) {
        System.out.println("ERROR (2)!\n");
        for (int k=0 ; k < expValues.length ; k++)
          System.out.printf("%d ", expValues[k]);
        System.out.println();
        System.out.println(table.contains1(i) ? "true" : "false");
        System.exit(1);
      }

      int[] actualValues = table.lookupByCol1(i);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (3)!\n");
        System.exit(1);
      }

      BinaryTable.Iter it = table.getIter1(i);
      count = 0;
      while (!it.done()) {
        list[count++] = it.getField2();
        it.next();
      }
      actualValues = Arrays.copyOf(list, count);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (4)!\n");
        System.exit(1);
      }
    }

    for (int j=0 ; j < size ; j++) {
      int count = 0;
      int[] list = new int[size];
      for (int i=0 ; i < size ; i++)
        if (bitMap[i][j])
          list[count++] = i;
      int[] expValues = Arrays.copyOf(list, count);

      if (table.contains2(j) != (expValues.length > 0)) {
        System.out.println("ERROR (5)!\n");
        System.exit(1);
      }

      int[] actualValues = table.lookupByCol2(j);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (6)!\n");
        System.exit(1);
      }

      count = 0;
      BinaryTable.Iter it = table.getIter2(j);
      while (!it.done()) {
        list[count++] = it.getField1();
        it.next();
      }
      actualValues = Arrays.copyOf(list, count);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (4)!\n");
        System.exit(1);
      }
    }
  }

  static void printDiffs(BinaryTable table, boolean[][] bitMap, int size) {
    for (int i=0 ; i < size ; i++) {
      for (int j=0 ; j < size ; j++) {
        int actual = table.contains(i, j) ? 1 : 0;
        int expected = bitMap[i][j] ? 1 : 0;
        System.out.printf("%d/%d ", actual, expected);
      }
      System.out.println();
    }
  }
}
