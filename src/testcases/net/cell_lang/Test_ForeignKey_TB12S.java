package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


class Test_ForeignKey_TB12S {
  static Random rand = new Random(0);

  public static void run() {
    boolean[][] done = new boolean[40][];
    for (int i=0 ; i < 40 ; i++) {
      done[i] = new boolean[40];
    }

    for (int r=1 ; r < 40 ; r++)
      for (int r12=1 ; r12 < r ; r12++)
          for (int r3=1 ; r3 < r ; r3++)
            if (!done[r12][r3]) {
              new Test_ForeignKey_TB12S().run(r12, r3, 10000);
              done[r12][r3] = true;
            }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  ValueStore store12;
  ValueStore store3;
  Sym12TernaryTable source;
  SymBinaryTable target;

  ValueStoreUpdater storeUpdater12;
  ValueStoreUpdater storeUpdater3;
  Sym12TernaryTableUpdater sourceUpdater;
  SymBinaryTableUpdater targetUpdater;

  String[] values12;
  String[] values3;

  boolean[][][] sourceBitmap;
  boolean[][] targetBitmap;

  boolean[][][] newSourceBitmap;
  boolean[][] newTargetBitmap;

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  void run(int r12, int r3, int testCount) {
    store12 = new ValueStore();
    store3 = new ValueStore();
    source = new Sym12TernaryTable(store12, store3);
    target = new SymBinaryTable(store12);

    storeUpdater12 = new ValueStoreUpdater(store12);
    storeUpdater3 = new ValueStoreUpdater(store3);
    sourceUpdater = new Sym12TernaryTableUpdater(source, storeUpdater12, storeUpdater3);
    targetUpdater = new SymBinaryTableUpdater(target, storeUpdater12);

    values12 = new String[r12];
    values3 = new String[r3];

    String charset = "abcdefghijklmnopqrstuvwxyz";

    for (int i=0 ; i < r12 ; i++)
      for ( ; ; ) {
        int len = rand.nextInt(10);
        char[] chars = new char[len];
        for (int j=0 ; j < len ; j++)
          chars[j] = charset.charAt(rand.nextInt(charset.length()));
        String str = new String(chars);
        boolean found = false;
        for (int j=0 ; j < i ; j++)
          if (str.equals(values12[j])) {
            found = true;
            break;
          }
        if (!found) {
          values12[i] = str;
          break;
        }
      }

    for (int i=0 ; i < r3 ; i++)
      for ( ; ; ) {
        int len = 4 + rand.nextInt(4);
        char[] chars = new char[len];
        for (int j=0 ; j < len ; j++)
          chars[j] = charset.charAt(rand.nextInt(charset.length()));
        String tag = new String(chars);
        String repr = String.format("(%d, %d)", rand.nextInt(1000), rand.nextInt(1000));
        boolean found = false;
        for (int j=0 ; j < i ; j++)
          if (repr.equals(values3[j])) {
            found = true;
            break;
          }
        if (!found) {
          values3[i] = repr;
          break;
        }
      }

    sourceBitmap = new boolean[r12][][];
    for (int i1=0 ; i1 < r12 ; i1++) {
      sourceBitmap[i1] = new boolean[r12][];
      for (int i2=0 ; i2 < r12 ; i2++)
        sourceBitmap[i1][i2] = new boolean[r3];
    }
    targetBitmap = new boolean[r12][];
    for (int i=0 ; i < r12 ; i++)
      targetBitmap[i] = new boolean[r12];

    int okCount = 0;
    int okCountDeleted = 0;
    int okCountInserted = 0;
    int notOkCount = 0;

    for (int _t=0 ; _t < testCount ; _t++) {
      newSourceBitmap = new boolean[r12][][];
      for (int i1=0 ; i1 < r12 ; i1++) {
        newSourceBitmap[i1] = new boolean[r12][];
        for (int i2=0 ; i2 < r12 ; i2++)
          newSourceBitmap[i1][i2] = Arrays.copyOf(sourceBitmap[i1][i2], sourceBitmap[i1][i2].length);
      }
      newTargetBitmap = new boolean[r12][];
      for (int i=0 ; i < r12 ; i++)
        newTargetBitmap[i] = Arrays.copyOf(targetBitmap[i], targetBitmap[i].length);

      // Inserting and removing a few values at random in or from source
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx1 = rand.nextInt(r12);
        int idx2 = rand.nextInt(r12);
        int idx3 = rand.nextInt(r3);
        int surr1 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx1]));
        int surr2 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx2]));
        int surr3 = storeUpdater3.lookupOrInsertValue(Conversions.convertText(values3[idx3]));
        if (sourceBitmap[idx1][idx2][idx3])
          sourceUpdater.delete(surr1, surr2, surr3);
        else
          sourceUpdater.insert(surr1, surr2, surr3);
        newSourceBitmap[idx1][idx2][idx3] = !sourceBitmap[idx1][idx2][idx3];
        if (idx1 != idx2)
          newSourceBitmap[idx2][idx1][idx3] = newSourceBitmap[idx1][idx2][idx3];
      }

      // Inserting and removing a few values at random in or from target
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx1 = rand.nextInt(r12);
        int idx2 = rand.nextInt(r12);
        int surr1 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx1]));
        int surr2 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx2]));
        if (targetBitmap[idx1][idx2])
          targetUpdater.delete(surr1, surr2);
        else
          targetUpdater.insert(surr1, surr2);
        newTargetBitmap[idx1][idx2] = !targetBitmap[idx1][idx2];
        if (idx1 != idx2)
          newTargetBitmap[idx2][idx1] = newTargetBitmap[idx1][idx2];
      }

      boolean ok = true;
      for (int i1=0 ; i1 < r12 ; i1++)
        for (int i2=0 ; i2 < r12 ; i2++)
          for (int i3=0 ; i3 < r3 ; i3++)
          if (newSourceBitmap[i1][i2][i3] & !newTargetBitmap[i1][i2]) {
            ok = false;
            break;
          }

      boolean debugIt = sourceUpdater.checkForeignKeys_12(targetUpdater) != ok;

      if (debugIt) {
        System.out.printf("ERROR: _t = %d, r12 = %d, r3 = %d, ok = %s, okCount = %d, notOkCount = %d\n",
          _t, r12, r3, ok ? "true" : "false", okCount, notOkCount);

        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++)
            for (int i3=0 ; i3 < r3 ; i3++)
              if (sourceBitmap[i1][i2][i3])
                if (newSourceBitmap[i1][i2][i3])
                  System.out.printf("%d %d %d\n", i1, i2, i3);
                else
                  System.out.printf("%d %d %d -\n", i1, i2, i3);
              else if (newSourceBitmap[i1][i2][i3])
                System.out.printf("%d %d %d +\n", i1, i2, i3);

        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++)
            if (targetBitmap[i1][i2])
              if (newTargetBitmap[i1][i2])
                System.out.printf("%d %d\n", i1, i2);
              else
                System.out.printf("%d %d -\n", i1, i2);
            else if (newTargetBitmap[i1][i2])
              System.out.printf("%d %d +\n", i1, i2);

        ok = sourceUpdater.checkForeignKeys_12(targetUpdater);

        System.exit(1);
      }

      if (ok) {
        int deletedFromSource = 0;
        int insertedIntoSource = 0;
        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++)
            for (int i3=0 ; i3 < r3 ; i3++) {
              if (sourceBitmap[i1][i2][i3] & !newSourceBitmap[i1][i2][i3])
                deletedFromSource++;
              if (!sourceBitmap[i1][i2][i3] & newSourceBitmap[i1][i2][i3])
                insertedIntoSource++;
            }

        if (deletedFromSource > 0)
          okCountDeleted++;
        if (insertedIntoSource > 0)
          okCountInserted++;
        if (deletedFromSource > 0 | insertedIntoSource > 0)
          okCount++;

        sourceBitmap = newSourceBitmap;
        targetBitmap = newTargetBitmap;

        newSourceBitmap = null;
        newTargetBitmap = null;

        storeUpdater12.apply();
        storeUpdater3.apply();

        sourceUpdater.apply();
        targetUpdater.apply();

        sourceUpdater.finish();
        targetUpdater.finish();

        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++)
            for (int i3=0 ; i3 < r3 ; i3++) {
              boolean inSource = sourceBitmap[i1][i2][i3];
              boolean inTarget = targetBitmap[i1][i2];

              int surr1 = store12.lookupValue(Conversions.stringToObj(values12[i1]));
              int surr2 = store12.lookupValue(Conversions.stringToObj(values12[i2]));
              int surr3 = store3.lookupValue(Conversions.convertText(values3[i3]));

              if (surr1 == -1 | surr2 == -1) {
                // If either the first or the second argument is not in the
                // corresponding value store, then neither table can contain it
                check(!inSource & !inTarget);
                continue;
              }

              if (surr3 == -1) {
                // If the third argument is not in the corresponding value store,
                // then the entry cannot appear in the source relation
                check(!inSource);
                continue;
              }

              check(target.contains(surr1, surr2) == inTarget);
              check(target.contains(surr2, surr1) == inTarget);
              check(source.contains(surr1, surr2, surr3) == inSource);
              check(source.contains(surr2, surr1, surr3) == inSource);
            }
      }
      else {
        notOkCount++;
      }

      storeUpdater12.reset();
      storeUpdater3.reset();
      sourceUpdater.reset();
      targetUpdater.reset();
    }

    System.out.printf("r12 = %d, r3 = %d", r12, r3);
    System.out.printf(", ok: %d - %d - %d, not ok: %d", okCount, okCountDeleted, okCountInserted, notOkCount);
    System.out.printf(", size: %d -> %d\n", source.size(), target.size());
    System.out.println();
  }


  void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}
