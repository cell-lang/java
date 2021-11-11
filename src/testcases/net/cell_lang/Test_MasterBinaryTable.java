package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;


class Test_MasterBinaryTable {
  static int counter = 0;
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
    MasterBinaryTable table = new MasterBinaryTable(null, null);

    int[][] surrMap = new int[range][];
    for (int i=0 ; i < range ; i++) {
      surrMap[i] = new int[range];
      for (int j=0 ; j < range ; j++)
        surrMap[i][j] = 0xFFFFFFFF;
    }
    checkTable(table, surrMap, range);

    // Inserting until the table is full
    for (int i=0 ; i < range * range ; i++) {
      int surr1 = random.nextInt(range);
      int surr2 = random.nextInt(range);

      while (surrMap[surr1][surr2] != 0xFFFFFFFF) {
        surr2 = (surr2 + 1) % range;
        if (surr2 == 0)
          surr1 = (surr1 + 1) % range;
      }

      if (trace)
        System.out.printf("+ %2d %2d - %d\n", surr1, surr2, counter);

      // if (counter == ) {
      //   System.out.println();
      //   printDiffs(table, surrMap, range);
      //   System.out.println();
      // }

      boolean[] wasNew = new boolean[1];
      int pairSurr = table.insert(surr1, surr2, wasNew);
      check(wasNew[0], "Effective insert failed");

      int pairSurr2 = table.insert(surr1, surr2, wasNew);
      check(pairSurr2 == pairSurr, "Got the wrong pair surrogate");
      check(!wasNew[0], "Fake insert failed");

      surrMap[surr1][surr2] = pairSurr;

      checkTable(table, surrMap, range);

      counter++;
    }

    // Deleting until the table is empty
    for (int i=0 ; i < range * range ; i++) {
      int surr1 = random.nextInt(range);
      int surr2 = random.nextInt(range);

      while (surrMap[surr1][surr2] == 0xFFFFFFFF) {
        surr2 = (surr2 + 1) % range;
        if (surr2 == 0)
          surr1 = (surr1 + 1) % range;
      }

      if (trace)
        System.out.printf("- %2d %2d - %d\n", surr1, surr2, counter);

      // if (counter == ) {
      //   System.out.println();
      //   printDiffs(table, surrMap, range);
      //   System.out.println();
      // }

      boolean res = table.delete(surr1, surr2);
      check(res, "Effective delete failed");
      res = table.delete(surr1, surr2);
      check(!res, "Useless delete failed");

      surrMap[surr1][surr2] = 0xFFFFFFFF;

      checkTable(table, surrMap, range);

      counter++;
    }
  }

  static void check(boolean b, String msg) {
    if (!b) {
      System.out.println(msg);
      // Exception e = new Exception();
      // e.printStackTrace();
      System.exit(1);
    }
  }

  static void checkTable(MasterBinaryTable table, int[][] surrMap, int size) {
    table.check();

    for (int i=0 ; i < size ; i++)
      for (int j=0 ; j < size ; j++)
        if (table.lookup(i, j) != surrMap[i][j]) {
          System.out.println("ERROR (1)!\n");
          printDiffs(table, surrMap, size);
          //throw new Exception();
          boolean b = table.contains(i, j);
          System.exit(b ? 1 : 2);
        }

    { int expectedCount = 0;
      for (int i=0 ; i < size ; i++)
        for (int j=0 ; j < size ; j++)
          if (surrMap[i][j] != 0xFFFFFFFF)
            expectedCount++;

      int actualCount = 0;
      MasterBinaryTable.Iter it = table.getIter();
      while (!it.done()) {
        int arg1 = it.get1();
        int arg2 = it.get2();
        if (surrMap[arg1][arg2] == 0xFFFFFFFF) {
          System.out.println("ERROR (1)!\n");
          System.exit(1);
        }
        actualCount++;
        it.next();
      }

      if (actualCount != expectedCount) {
        System.out.println("ERROR (1)!\n");
        System.out.printf("Actual count = %d, expected = %d\n\n", actualCount, expectedCount);
        printDiffs(table, surrMap, size);

        it = table.getIter();
        while (!it.done()) {
          int arg1 = it.get1();
          int arg2 = it.get2();
          System.out.printf("%2d %2d\n", arg1, arg2);
          it.next();
        }

        System.exit(1);
      }

      if (table.size() != expectedCount) {
        System.out.println("ERROR (1/C)!\n");
        System.exit(1);
      }
    }

    for (int i=0 ; i < size ; i++) {
      int count = 0;
      int[] list = new int[size];
      for (int j=0 ; j < size ; j++)
        if (surrMap[i][j] != 0xFFFFFFFF)
          list[count++] = j;
      int[] expValues = Arrays.copyOf(list, count);

      if (table.count1(i) != count) {
        System.out.println("ERROR (5/A)!\n");
        System.exit(1);
      }

      if (table.contains1(i) != (expValues.length > 0)) {
        System.out.println("ERROR (2)!\n");
        for (int k=0 ; k < expValues.length ; k++)
          System.out.printf("%d ", expValues[k]);
        System.out.println();
        System.out.println(table.contains1(i) ? "true" : "false");
        System.exit(1);
      }

      int[] actualValues = table.restrict1(i);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (3)!\n");
        System.exit(1);
      }

      MasterBinaryTable.Iter it = table.getIter1(i);
      count = 0;
      while (!it.done()) {
        list[count++] = it.get1();
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
        if (surrMap[i][j] != 0xFFFFFFFF)
          list[count++] = i;
      int[] expValues = Arrays.copyOf(list, count);

      if (table.count2(j) != count) {
        System.out.println("ERROR (5/A)!\n");
        System.exit(1);
      }

      if (table.contains2(j) != (expValues.length > 0)) {
        System.out.println("ERROR (5)!\n");
        System.exit(1);
      }

      int[] actualValues = table.restrict2(j);
      Arrays.sort(actualValues);
      if (!Arrays.equals(actualValues, expValues)) {
        System.out.println("ERROR (6)!\n");
        System.exit(1);
      }

      count = 0;
      MasterBinaryTable.Iter it = table.getIter2(j);
      while (!it.done()) {
        list[count++] = it.get1();
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

  static void printDiffs(MasterBinaryTable table, int[][] surrMap, int size) {
    for (int i=0 ; i < size ; i++) {
      for (int j=0 ; j < size ; j++) {
        int actual = table.contains(i, j) ? 1 : 0;
        int expected = surrMap[i][j] != 0xFFFFFFFF ? 1 : 0;
        System.out.printf("%d/%d ", actual, expected);
      }
      System.out.println();
    }
    System.out.println();
    System.out.println();

    for (int i=0 ; i < size ; i++) {
      for (int j=0 ; j < size ; j++) {
        int actual = table.lookup(i, j);
        int expected = surrMap[i][j];
        System.out.printf("%d/%d ", actual, expected);
      }
      System.out.println();
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  static void run2() {
    ArrayList<Integer> badValues = new ArrayList<Integer>();
    for (int i=0 ; i <= 16 ; i++)
      badValues.add(15 * i);

    MasterBinaryTable table = new MasterBinaryTable(null, null);

    for (int i=0 ; i < badValues.size() ; i++) {
      if (i == badValues.size() - 1)
        System.out.print("");

      int surrI = badValues.get(i);

      table.insert(0, surrI);

      for (int j=0 ; j <= i ; j++) {
        int surrJ = badValues.get(j);
        if (!table.contains(0, surrJ)) {
          System.out.printf("ERROR FOUND: i = %d, @i = %d, j = %d, @j = %d\n", i, surrI, j, surrJ);
          boolean res = table.contains(0, surrJ);
          System.exit(1);
        }
      }

      int[] surrs = table.restrict1(0);
      Arrays.sort(surrs);
      int[] inputs = new int[i+1];
      for (int j=0 ; j <= i ; j++)
        inputs[j] = badValues.get(j);
      if (!Arrays.equals(inputs, surrs)) {
        System.out.printf("Found bug: i = %d, @i = %d\n", i, surrI);
        for (int j=0 ; j < inputs.length ; j++)
          System.out.printf(" %3d", inputs[j]);
        System.out.println();
        for (int j=0 ; j < surrs.length ; j++)
          System.out.printf(" %3d", surrs[j]);
        System.out.println();
        surrs = table.restrict1(0);
        System.exit(1);
      }
    }
  }

  static void run3() {
    MasterBinaryTable table1 = new MasterBinaryTable(null, null);
    MasterBinaryTable table2 = new MasterBinaryTable(null, null);

    for (int i=0 ; i < 10000 ; i++) {
      table1.insert(0, i);
      table2.insert(i, 0);

      for (int j=0 ; j <= i ; j++) {
        if (!table1.contains(0, j)) {
          System.out.printf("ERROR FOUND (1): i = %d, j = %d\n", i, j);
          System.exit(1);
        }

        if (!table1.contains2(j)) {
          System.out.printf("ERROR FOUND (2): i = %d, j = %d\n", i, j);
          System.exit(1);
        }

        if (!table2.contains(j, 0)) {
          System.out.printf("ERROR FOUND (3): i = %d, j = %d\n", i, j);
          System.exit(1);
        }

        if (!table2.contains1(j)) {
          System.out.printf("ERROR FOUND (4): i = %d, j = %d\n", i, j);
          System.exit(1);
        }
      }

      int[] surrs = table1.restrict1(0);
      Arrays.sort(surrs);
      if (surrs.length != i + 1) {
        System.out.printf("ERROR (5)!\n");
        System.exit(1);
      }
      for (int k=0 ; k <= i ; k++)
        if (surrs[k] != k) {
          System.out.printf("Found bug: i = %d, k = %d\n", i, k);
          // for (int j=0 ; j < inputs.length ; j++)
          //   System.out.printf(" %3d", inputs[j]);
          // System.out.println();
          for (int j=0 ; j < surrs.length ; j++)
            System.out.printf(" %3d", surrs[j]);
          System.out.println();
          surrs = table1.restrict1(0);
          System.exit(1);
        }
    }
  }
}
