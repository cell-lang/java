package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


class Test_ForeignKey_UU {
  static Random rand = new Random(0);

  public static void run() {
    ValueStore store = new ValueStore();
    UnaryTable source = new UnaryTable(store);
    UnaryTable target = new UnaryTable(store);

    ValueStoreUpdater storeUpdater = new ValueStoreUpdater(store);
    UnaryTableUpdater sourceUpdater = new UnaryTableUpdater(source, storeUpdater);
    UnaryTableUpdater targetUpdater = new UnaryTableUpdater(target, storeUpdater);

    int[] values = new int[1000];
    boolean[] sourceBitmap = new boolean[1000];
    boolean[] targetBitmap = new boolean[1000];

    for (int i=0 ; i < 1000 ; i++)
      for ( ; ; ) {
        int value = rand.nextInt(10000);
        boolean found = false;
        for (int j=0 ; j < i ; j++)
          if (values[j] == value) {
            found = true;
            break;
          }
        if (!found) {
          values[i] = value;
          break;
        }
      }

    // Inserting some random values in target
    int targetInsertCount = rand.nextInt(100);
    for (int i=0 ; i < targetInsertCount ; i++) {
      int idx = rand.nextInt(1000);
      Obj obj = IntObj.get(values[idx]);
      int surr = storeUpdater.lookupValueEx(obj);
      if (surr == -1)
        surr = storeUpdater.insert(obj);
      targetUpdater.insert(surr);
      targetBitmap[idx] = true;
    }
    storeUpdater.apply();
    targetUpdater.apply();
    targetUpdater.finish();
    storeUpdater.reset();
    targetUpdater.reset();

    int okCount = 0;
    int okCountDeleted = 0;
    int okCountInserted = 0;
    int notOkCount = 0;

    for (int _t=0 ; _t < 100000 ; _t++) {
      boolean[] newSourceBitmap = Arrays.copyOf(sourceBitmap, sourceBitmap.length);
      boolean[] newTargetBitmap = Arrays.copyOf(targetBitmap, targetBitmap.length);

      // Inserting and removing a few values at random in or from source
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx = rand.nextInt(1000);
        Obj obj = IntObj.get(values[idx]);
        int surr = storeUpdater.lookupValueEx(obj);
        if (surr == -1)
          surr = storeUpdater.insert(obj);
        if (sourceBitmap[idx])
          sourceUpdater.delete(surr);
        else
          sourceUpdater.insert(surr);
        newSourceBitmap[idx] = !sourceBitmap[idx];
      }

      // Inserting and removing a few values at random in or from source
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx = rand.nextInt(1000);
        Obj obj = IntObj.get(values[idx]);
        int surr = storeUpdater.lookupValueEx(obj);
        if (surr == -1)
          surr = storeUpdater.insert(obj);
        if (targetBitmap[idx])
          targetUpdater.delete(surr);
        else
          targetUpdater.insert(surr);
        newTargetBitmap[idx] = !targetBitmap[idx];
      }

      boolean ok = true;
      for (int i=0 ; i < 1000 ; i++)
        if (newSourceBitmap[i] & !newTargetBitmap[i]) {
          ok = false;
          break;
        }

      if (sourceUpdater.checkForeignKeys(targetUpdater) != ok) {
        System.out.printf("ERROR: %d, ok = %s, okCount = %d, notOkCount = %d\n",
          _t, ok ? "true" : "false", okCount, notOkCount);

        System.out.println("\nTarget only:");
        for (int i=0 ; i < 1000 ; i++)
          if (!newSourceBitmap[i] & newTargetBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\nSource only:");
        for (int i=0 ; i < 1000 ; i++)
          if (newSourceBitmap[i] & !newTargetBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\nBoth:");
        for (int i=0 ; i < 1000 ; i++)
          if (newSourceBitmap[i] & newTargetBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\nDeleted from source:");
        for (int i=0 ; i < 1000 ; i++)
          if (sourceBitmap[i] & !newSourceBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\nInserted into source:");
        for (int i=0 ; i < 1000 ; i++)
          if (!sourceBitmap[i] & newSourceBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\nDeleted from target:");
        for (int i=0 ; i < 1000 ; i++)
          if (targetBitmap[i] & !newTargetBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\nInserted into target:");
        for (int i=0 ; i < 1000 ; i++)
          if (!targetBitmap[i] & newTargetBitmap[i])
            System.out.printf("%03d: %4d -> %3d\n", i, values[i], storeUpdater.lookupValueEx(IntObj.get(values[i])));

        System.out.println("\n");

        ok = sourceUpdater.checkForeignKeys(targetUpdater);

        System.exit(1);
      }

      if (ok) {
        int deletedFromSource = 0;
        for (int i=0 ; i < 1000 ; i++)
          if (sourceBitmap[i] & !newSourceBitmap[i])
            deletedFromSource++;

        int insertedIntoSource = 0;
        for (int i=0 ; i < 1000 ; i++)
          if (!sourceBitmap[i] & newSourceBitmap[i])
            insertedIntoSource++;

        storeUpdater.apply();
        sourceUpdater.apply();
        targetUpdater.apply();
        sourceUpdater.finish();
        targetUpdater.finish();
        if (deletedFromSource > 0)
          okCountDeleted++;
        if (insertedIntoSource > 0)
          okCountInserted++;
        if (deletedFromSource > 0 | insertedIntoSource > 0)
          okCount++;
        sourceBitmap = newSourceBitmap;
        targetBitmap = newTargetBitmap;
      }
      else {
        notOkCount++;
      }

      storeUpdater.reset();
      sourceUpdater.reset();
      targetUpdater.reset();

      check(target.size() == store.count());
    }

    System.out.printf("ok: %d - %d - %d, not ok: %d\n", okCount, okCountDeleted, okCountInserted, notOkCount);
    System.out.printf("Size: %d -> %d\n", source.size(), target.size());
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}