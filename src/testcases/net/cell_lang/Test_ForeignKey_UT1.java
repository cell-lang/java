package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


class Test_ForeignKey_UT1 {
  static Random rand = new Random(0);

  public static void run() {
    boolean[][][] done = new boolean[40][][];
    for (int i=0 ; i < 40 ; i++) {
      done[i] = new boolean[40][];
      for (int j=0 ; j < 40 ; j++)
        done[i][j] = new boolean[40];
    }

    for (int r=2 ; r < 40 ; r++)
      for (int r1=1 ; r1 < r ; r1++)
        for (int r2=1 ; r2 < r ; r2++)
          for (int r3=1 ; r3 < r ; r3++)
            if (!done[r1][r2][r3]) {
              new Test_ForeignKey_UT1().run(r1, r2, r3, 10000);
              done[r1][r2][r3] = true;
            }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  ValueStore store1;
  ValueStore store2;
  ValueStore store3;
  TernaryTable source;
  UnaryTable target;

  ValueStoreUpdater storeUpdater1;
  ValueStoreUpdater storeUpdater2;
  ValueStoreUpdater storeUpdater3;
  TernaryTableUpdater sourceUpdater;
  UnaryTableUpdater targetUpdater;

  int[] values1;
  String[] values2;
  String[] values3;

  boolean[][][] sourceBitmap;
  boolean[] targetBitmap;

  boolean[][][] newSourceBitmap;
  boolean[] newTargetBitmap;

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  void run(int r1, int r2, int r3, int testCount) {
    store1 = new ValueStore();
    store2 = new ValueStore();
    store3 = new ValueStore();
    source = new TernaryTable(store1, store2, store3);
    target = new UnaryTable(store1);

    storeUpdater1 = new ValueStoreUpdater(store1);
    storeUpdater2 = new ValueStoreUpdater(store2);
    storeUpdater3 = new ValueStoreUpdater(store3);
    sourceUpdater = new TernaryTableUpdater(source, storeUpdater1, storeUpdater2, storeUpdater3);
    targetUpdater = new UnaryTableUpdater(target, storeUpdater1);

    values1 = new int[r1];
    values2 = new String[r2];
    values3 = new String[r3];

    for (int i=0 ; i < r1 ; i++)
      for ( ; ; ) {
        int value = rand.nextInt(10000);
        boolean found = false;
        for (int j=0 ; j < i ; j++)
          if (values1[j] == value) {
            found = true;
            break;
          }
        if (!found) {
          values1[i] = value;
          break;
        }
      }

    String charset = "abcdefghijklmnopqrstuvwxyz";

    for (int i=0 ; i < r2 ; i++)
      for ( ; ; ) {
        int len = rand.nextInt(10);
        char[] chars = new char[len];
        for (int j=0 ; j < len ; j++)
          chars[j] = charset.charAt(rand.nextInt(charset.length()));
        String str = new String(chars);
        boolean found = false;
        for (int j=0 ; j < i ; j++)
          if (str.equals(values2[j])) {
            found = true;
            break;
          }
        if (!found) {
          values2[i] = str;
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

    sourceBitmap = new boolean[r1][][];
    for (int i1=0 ; i1 < r1 ; i1++) {
      sourceBitmap[i1] = new boolean[r2][];
      for (int i2=0 ; i2 < r2 ; i2++)
        sourceBitmap[i1][i2] = new boolean[r3];
    }
    targetBitmap = new boolean[r1];

    int okCount = 0;
    int okCountDeleted = 0;
    int okCountInserted = 0;
    int notOkCount = 0;

    for (int _t=0 ; _t < testCount ; _t++) {
      newSourceBitmap = new boolean[r1][][];
      for (int i1=0 ; i1 < r1 ; i1++) {
        newSourceBitmap[i1] = new boolean[r2][];
        for (int i2=0 ; i2 < r2 ; i2++)
          newSourceBitmap[i1][i2] = Arrays.copyOf(sourceBitmap[i1][i2], sourceBitmap[i1][i2].length);
      }
      newTargetBitmap = Arrays.copyOf(targetBitmap, targetBitmap.length);

      // Inserting and removing a few values at random in or from source
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx1 = rand.nextInt(r1);
        int idx2 = rand.nextInt(r2);
        int idx3 = rand.nextInt(r3);
        int surr1 = storeUpdater1.lookupOrInsertValue(IntObj.get(values1[idx1]));
        int surr2 = storeUpdater2.lookupOrInsertValue(Conversions.stringToObj(values2[idx2]));
        int surr3 = storeUpdater3.lookupOrInsertValue(Conversions.convertText(values3[idx3]));
        if (sourceBitmap[idx1][idx2][idx3])
          sourceUpdater.delete(surr1, surr2, surr3);
        else
          sourceUpdater.insert(surr1, surr2, surr3);
        newSourceBitmap[idx1][idx2][idx3] = !sourceBitmap[idx1][idx2][idx3];
      }

      // Inserting and removing a few values at random in or from target
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx = rand.nextInt(r1);
        int surr = storeUpdater1.lookupOrInsertValue(IntObj.get(values1[idx]));
        if (targetBitmap[idx])
          targetUpdater.delete(surr);
        else
          targetUpdater.insert(surr);
        newTargetBitmap[idx] = !targetBitmap[idx];
      }

      boolean ok = true;
      for (int i1=0 ; i1 < r1 ; i1++)
        if (newTargetBitmap[i1]) {
          boolean eltOk = false;
loop1:    for (int i2=0 ; i2 < r2 ; i2++)
            for (int i3=0 ; i3 < r3 ; i3++)
              if (newSourceBitmap[i1][i2][i3]) {
                eltOk = true;
                break loop1;
              }
          if (!eltOk) {
            ok = false;
            break;
          }
        }

      boolean ok2 = targetUpdater.checkForeignKeys_1(sourceUpdater);
      boolean debugIt = ok2 != ok;

      if (debugIt) {
        System.out.printf(
          "ERROR: _t = %d, r1 = %d, r2 = %d, r3 = %d, ok = %s, ok2 = %s, okCount = %d, notOkCount = %d\n",
          _t, r1, r2, r3, ok ? "true" : "false", ok2 ? "true" : "false", okCount, notOkCount);

//         ok = true;
//         for (int i3=0 ; i3 < r3 ; i3++)
//           if (newTargetBitmap[i3]) {
//             boolean eltOk = false;
// loop1:      for (int i1=0 ; i1 < r1 ; i1++)
//               for (int i2=0 ; i2 < r2 ; i2++)
//                 if (newSourceBitmap[i1][i2][i3]) {
//                   eltOk = true;
//                   break loop1;
//                 }
//             if (!eltOk) {
//               ok = false;
//               break;
//             }
//           }

        boolean ok3 = targetUpdater.checkForeignKeys_1(sourceUpdater);
        boolean ok4 = targetUpdater.checkForeignKeys_1(sourceUpdater);

        System.out.printf("ok3 = %d, ok4 = %d\n", ok3 ? 1 : 0, ok4 ? 1 : 0);
        System.exit(1);
      }

      if (ok) {
        int deletedFromSource = 0;
        int insertedIntoSource = 0;
        for (int i1=0 ; i1 < r1 ; i1++)
          for (int i2=0 ; i2 < r2 ; i2++)
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

        storeUpdater1.apply();
        storeUpdater2.apply();
        storeUpdater3.apply();

        sourceUpdater.apply();
        targetUpdater.apply();

        sourceUpdater.finish();
        targetUpdater.finish();

        for (int i1=0 ; i1 < r1 ; i1++)
          for (int i2=0 ; i2 < r2 ; i2++)
            for (int i3=0 ; i3 < r3 ; i3++) {
              boolean inSource = sourceBitmap[i1][i2][i3];
              boolean inTarget = targetBitmap[i1];

              int surr1 = store1.lookupValue(IntObj.get(values1[i1]));
              int surr2 = store2.lookupValue(Conversions.stringToObj(values2[i2]));
              int surr3 = store3.lookupValue(Conversions.convertText(values3[i3]));

              if (surr1 == -1) {
                // If the first argument is not in the corresponding value store,
                // then neither table can contain it
                check(!inSource & !inTarget);
                continue;
              }

              // Since the first argument is in the value store, it has to appear in
              // either the first column of the source relation, or in the target one.
              // Since presence in the latter implies presence in the former, the
              // source relation must always contain it.
              check(source.contains1(surr1));

              if (surr2 == -1 | surr3 == -1) {
                // If either the second or the third argument is not in the corresponding value store,
                // then the entry cannot appear in the source relation
                check(!inSource);
                continue;
              }

              check(source.contains(surr1, surr2, surr3) == inSource);
            }
      }
      else {
        notOkCount++;
      }

      storeUpdater1.reset();
      storeUpdater2.reset();
      storeUpdater3.reset();
      sourceUpdater.reset();
      targetUpdater.reset();
    }

    System.out.printf("r1 = %d, r2 = %d, r3 = %d", r1, r2, r3);
    System.out.printf(", ok: %d - %d - %d, not ok: %d", okCount, okCountDeleted, okCountInserted, notOkCount);
    System.out.printf(", size: %d -> %d\n", source.size(), target.size());
    System.out.println();
  }

  void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}
