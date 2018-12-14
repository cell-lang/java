package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;


class Test_SymBinaryTable {
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
    SymBinaryTable table = new SymBinaryTable(null);

    boolean[][] bitMap = new boolean[range][];
    for (int i=0 ; i < range ; i++)
      bitMap[i] = new boolean[range];
    checkTable(table, bitMap, range);

    int numOfOps = (range * (range + 1)) / 2;

    // Inserting until the table is full
    for (int i=0 ; i < numOfOps ; i++) {
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
      bitMap[surr2][surr1] = true;

      checkTable(table, bitMap, range);
    }

    for (int i=0 ; i < range ; i++)
      for (int j=0 ; j < range ; j++)
        if (!bitMap[i][j]) {
          System.out.println("ERROR - LINE 59");
          System.exit(1);
        }

    // Deleting until the table is empty
    for (int i=0 ; i < numOfOps ; i++) {
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
      bitMap[surr2][surr1] = false;

      checkTable(table, bitMap, range);
    }

    for (int i=0 ; i < range ; i++)
      for (int j=0 ; j < range ; j++)
        if (bitMap[i][j]) {
          System.out.println("ERROR - LINE 59");
          System.exit(1);
        }
  }

  static void checkTable(SymBinaryTable table, boolean[][] bitMap, int size) {
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

      if (table.contains(i) != (expValues.length > 0)) {
        System.out.println("ERROR (2)!\n");
        for (int k=0 ; k < expValues.length ; k++)
          System.out.printf("%d ", expValues[k]);
        System.out.println();
        System.out.println(table.contains(i) ? "true" : "false");
        System.exit(1);
      }

      int[] actualValues = table.lookup(i);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (3)!\n");
        System.exit(1);
      }

      SymBinaryTable.Iter it = table.getIter(i);
      count = 0;
      while (!it.done()) {
        if (it.get1() != i) {
          System.out.println("ERROR (4.0)!\n");
          System.exit(1);
        }
        list[count++] = it.get2();
        it.next();
      }
      actualValues = Arrays.copyOf(list, count);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (4)!\n");
        // System.out.printf("Expected No: %d, actual No: %d\n", expValues.length, actualValues.length);
        // for (int j=0 ; j < expValues.length ; j++)
        //   System.out.printf("%d ", expValues[j]);
        // System.out.println();
        // for (int j=0 ; j < actualValues.length ; j++)
        //   System.out.printf("%d ", actualValues[j]);
        // System.out.println();
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

      if (table.contains(j) != (expValues.length > 0)) {
        System.out.println("ERROR (5)!\n");
        System.exit(1);
      }

      int[] actualValues = table.lookup(j);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (6)!\n");
        System.exit(1);
      }

      // count = 0;
      // SymBinaryTable.Iter it = table.getIter(j);
      // while (!it.done()) {
      //   list[count++] = it.get1();
      //   it.next();
      // }
      // actualValues = Arrays.copyOf(list, count);
      // Arrays.sort(actualValues);
      // if (!Arrays.equals(actualValues, expValues)) {
      //   System.out.println("ERROR (7)!\n");

      //   System.out.printf("Expected No: %d, actual No: %d\n", expValues.length, actualValues.length);
      //   for (int k=0 ; k < expValues.length ; k++)
      //     System.out.printf("%d ", expValues[k]);
      //   System.out.println();
      //   for (int k=0 ; k < actualValues.length ; k++)
      //     System.out.printf("%d ", actualValues[k]);
      //   System.out.println();

      //   System.exit(1);
      // }
    }
  }

  static void printDiffs(SymBinaryTable table, boolean[][] bitMap, int size) {
    for (int i=0 ; i < size ; i++) {
      for (int j=0 ; j < size ; j++) {
        int actual = table.contains(i, j) ? 1 : 0;
        int expected = bitMap[i][j] ? 1 : 0;
        System.out.printf("%d/%d ", actual, expected);
      }
      System.out.println();
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // static void run2() {
  //   ArrayList<Integer> badValues = new ArrayList<Integer>();
  //   for (int i=0 ; i <= 16 ; i++)
  //     badValues.add(15 * i);

  //   SymBinaryTable table = new SymBinaryTable(null);

  //   for (int i=0 ; i < badValues.size() ; i++) {
  //     if (i == badValues.size() - 1)
  //       System.out.print("");

  //     int surrI = badValues.get(i);

  //     table.insert(0, surrI);

  //     for (int j=0 ; j <= i ; j++) {
  //       int surrJ = badValues.get(j);
  //       if (!table.contains(0, surrJ)) {
  //         System.out.printf("ERROR FOUND: i = %d, @i = %d, j = %d, @j = %d\n", i, surrI, j, surrJ);
  //         boolean res = table.contains(0, surrJ);
  //         System.exit(1);
  //       }
  //     }

  //     int[] surrs = table.lookupByCol1(0);
  //     Arrays.sort(surrs);
  //     int[] inputs = new int[i+1];
  //     for (int j=0 ; j <= i ; j++)
  //       inputs[j] = badValues.get(j);
  //     if (!Arrays.equals(inputs, surrs)) {
  //       System.out.printf("Found bug: i = %d, @i = %d\n", i, surrI);
  //       for (int j=0 ; j < inputs.length ; j++)
  //         System.out.printf(" %3d", inputs[j]);
  //       System.out.println();
  //       for (int j=0 ; j < surrs.length ; j++)
  //         System.out.printf(" %3d", surrs[j]);
  //       System.out.println();
  //       surrs = table.lookupByCol1(0);
  //       System.exit(1);
  //     }
  //   }
  // }

  // static void run3() {
  //   SymBinaryTable table1 = new SymBinaryTable(null, null);
  //   SymBinaryTable table2 = new SymBinaryTable(null, null);

  //   for (int i=0 ; i < 10000 ; i++) {
  //     table1.insert(0, i);
  //     table2.insert(i, 0);

  //     for (int j=0 ; j <= i ; j++) {
  //       if (!table1.contains(0, j)) {
  //         System.out.printf("ERROR FOUND (1): i = %d, j = %d\n", i, j);
  //         System.exit(1);
  //       }

  //       if (!table1.contains2(j)) {
  //         System.out.printf("ERROR FOUND (2): i = %d, j = %d\n", i, j);
  //         System.exit(1);
  //       }

  //       if (!table2.contains(j, 0)) {
  //         System.out.printf("ERROR FOUND (3): i = %d, j = %d\n", i, j);
  //         System.exit(1);
  //       }

  //       if (!table2.contains1(j)) {
  //         System.out.printf("ERROR FOUND (4): i = %d, j = %d\n", i, j);
  //         System.exit(1);
  //       }
  //     }

  //     int[] surrs = table1.lookupByCol1(0);
  //     Arrays.sort(surrs);
  //     if (surrs.length != i + 1) {
  //       System.out.printf("ERROR (5)!\n");
  //       System.exit(1);
  //     }
  //     for (int k=0 ; k <= i ; k++)
  //       if (surrs[k] != k) {
  //         System.out.printf("Found bug: i = %d, k = %d\n", i, k);
  //         // for (int j=0 ; j < inputs.length ; j++)
  //         //   System.out.printf(" %3d", inputs[j]);
  //         // System.out.println();
  //         for (int j=0 ; j < surrs.length ; j++)
  //           System.out.printf(" %3d", surrs[j]);
  //         System.out.println();
  //         surrs = table1.lookupByCol1(0);
  //         System.exit(1);
  //       }
  //   }
  // }
}
