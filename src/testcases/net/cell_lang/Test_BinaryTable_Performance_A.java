package net.cell_lang;

import java.io.File;
import java.util.Scanner;


class Test_BinaryTable_Performance_A {
  public static void run() {
    int insCount = 3431966;
    int[] arg1Ins = new int[insCount];
    int[] arg2Ins = new int[insCount];

    int delCount = 322357;
    int[] arg2Del = new int[delCount];

    Scanner scanner;

    try {
      scanner = new Scanner(new File("data/insertions.txt"));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    int maxArg1 = -1;
    int maxArg2 = -1;

    for (int i=0 ; i < insCount ; i++) {
      String plus = scanner.next();
      Miscellanea._assert(plus.equals("+"));
      int arg1 = scanner.nextInt();
      int arg2 = scanner.nextInt();
      arg1Ins[i] = arg1;
      arg2Ins[i] = arg2;
      if (arg1 > maxArg1)
        maxArg1 = arg1;
      if (arg2 > maxArg2)
        maxArg2 = arg2;
    }


    try {
      scanner = new Scanner(new File("data/deletions.txt"));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

    for (int i=0 ; i < delCount ; i++) {
      String minus = scanner.next();
      Miscellanea._assert(minus.equals("-"));
      int arg2 = scanner.nextInt();
      arg2Del[i] = arg2;
    }

    int noOfRuns = 20;
    long[] times = new long[noOfRuns];
    for (int i=0 ; i < 20 ; i++) {
      ObjStore store1 = new ObjStore();
      ObjStore store2 = new ObjStore();
      ObjStoreUpdater storeUpdater1 = new ObjStoreUpdater(store1);
      ObjStoreUpdater storeUpdater2 = new ObjStoreUpdater(store2);

      // System.out.println("Inserting...");
      // for (int j=0 ; j < insCount ; j++) {
      //   int surr1 = storeUpdater1.lookupOrInsertValue(new FloatObj(j));
      //   Miscellanea._assert(surr1 == j);
      //   storeUpdater1.apply();
      //   storeUpdater1.reset();

      //   int surr2 = storeUpdater2.lookupOrInsertValue(new FloatObj(j));
      //   Miscellanea._assert(surr2 == j);
      //   storeUpdater2.apply();
      //   storeUpdater2.reset();

      //   if ((j != 0) & (j % 1000 == 0)) {
      //     System.out.print(".");
      //     System.out.flush();
      //   }
      // }
      // System.out.println("\nDone\n");

      for (int j=0 ; j <= maxArg1 ; j++) {
        int surr1 = storeUpdater1.lookupOrInsertValue(new FloatObj(j));
        Miscellanea._assert(surr1 == j);
        storeUpdater1.apply();
        storeUpdater1.reset();
      }


      for (int j=0 ; j <= maxArg2 ; j++) {
        int surr2 = storeUpdater2.lookupOrInsertValue(new FloatObj(j));
        Miscellanea._assert(surr2 == j);
        storeUpdater2.apply();
        storeUpdater2.reset();
      }

      System.gc();

      BinaryTable table = new BinaryTable(null, null);
      BinaryTableUpdater updater = new BinaryTableUpdater("", table, storeUpdater1, storeUpdater2);

      long startTime = System.currentTimeMillis();

      // update(table, updater, arg1Ins, arg2Ins, arg2Del);
      insert(table, updater, arg1Ins, arg2Ins);
      remove(updater, arg2Del);

      long endTime = System.currentTimeMillis();
      times[i] = endTime - startTime;
      System.out.printf("%4d ", times[i]);
    }

    int totalTime = 0;
    for (int i=5 ; i < times.length ; i++)
      totalTime += times[i];
    System.out.printf("- %d\n", totalTime / (times.length - 5));
  }

  // static void update(BinaryTable table, BinaryTableUpdater updater, int[] arg1Ins, int[] arg2Ins, int[] arg2Del) {
  //   insert(table, updater, arg1Ins, arg2Ins);
  //   remove(updater, args2Del);
  // }

  static void insert(BinaryTable table, BinaryTableUpdater updater, int[] arg1Ins, int[] arg2Ins) {
    int count = arg1Ins.length;
    for (int i=0 ; i < count ; i++) {
      updater.insert(arg1Ins[i], arg2Ins[i]);

      // if (i != 0 & i % 1024 == 0) {
        updater.apply();
        updater.reset();
      // }

      if (i == 0) {
        int[] arg1s = table.restrict2(arg2Ins[0]);
        Miscellanea._assert(arg1s.length == 1);
        Miscellanea._assert(arg1s[0] == arg1Ins[0]);
      }
    }

    // updater.apply();
    // updater.reset();
  }

  static void remove(BinaryTableUpdater updater, int[] arg2Del) {
    int count = arg2Del.length;
    for (int i=0 ; i < count ; i++) {
      updater.delete2(arg2Del[i]);
      updater.apply();
      updater.store1.applyDelayedReleases();
      updater.store2.applyDelayedReleases();
      updater.reset();
    }
  }
}
