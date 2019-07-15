package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


class Test_ForeignKey_BT12S {
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
              new Test_ForeignKey_BT12S().run(r12, r3, 10000);
              done[r12][r3] = true;
            }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  ValueStore store12;
  ValueStore store3;
  SymBinaryTable source;
  Sym12TernaryTable target;

  ValueStoreUpdater storeUpdater12;
  ValueStoreUpdater storeUpdater3;
  SymBinaryTableUpdater sourceUpdater;
  Sym12TernaryTableUpdater targetUpdater;

  String[] values12;
  String[] values3;

  boolean[][] sourceBitmap;
  boolean[][][] targetBitmap;

  boolean[][] newSourceBitmap;
  boolean[][][] newTargetBitmap;

  int counter = 0;

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  void run(int r12, int r3, int testCount) {
    store12 = new ValueStore();
    store3 = new ValueStore();
    source = new SymBinaryTable(store12);
    target = new Sym12TernaryTable(store12, store3);

    storeUpdater12 = new ValueStoreUpdater(store12);
    storeUpdater3 = new ValueStoreUpdater(store3);
    sourceUpdater = new SymBinaryTableUpdater(source, storeUpdater12);
    targetUpdater = new Sym12TernaryTableUpdater(target, storeUpdater12, storeUpdater3);

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

    sourceBitmap = new boolean[r12][];
    for (int i=0 ; i < r12 ; i++)
      sourceBitmap[i] = new boolean[r12];

    targetBitmap = new boolean[r12][][];
    for (int i1=0 ; i1 < r12 ; i1++) {
      targetBitmap[i1] = new boolean[r12][];
      for (int i2=0 ; i2 < r12 ; i2++)
        targetBitmap[i1][i2] = new boolean[r3];
    }

    int okCount = 0;
    int okCountDeleted = 0;
    int okCountInserted = 0;
    int notOkCount = 0;

    for (int _t=0 ; _t < testCount ; _t++) {

boolean dbgOn = false;

if (dbgOn)
  System.out.printf("A - sourceUpdater.prepared = %s, targetUpdater.prepared = %s\n",
    sourceUpdater.prepared ? "true" : "false", targetUpdater.prepared ? "true" : "false");


      newSourceBitmap = new boolean[r12][];
      for (int i=0 ; i < r12 ; i++)
        newSourceBitmap[i] = Arrays.copyOf(sourceBitmap[i], sourceBitmap[i].length);

      newTargetBitmap = new boolean[r12][][];
      for (int i1=0 ; i1 < r12 ; i1++) {
        newTargetBitmap[i1] = new boolean[r12][];
        for (int i2=0 ; i2 < r12 ; i2++)
          newTargetBitmap[i1][i2] = Arrays.copyOf(targetBitmap[i1][i2], targetBitmap[i1][i2].length);
      }

      for (int i1=0 ; i1 < r12 ; i1++)
        for (int i2=0 ; i2 < r12 ; i2++)
          for (int i3=0 ; i3 < r3 ; i3++) {
            check(newSourceBitmap[i1][i2] == sourceBitmap[i1][i2]);
            check(newTargetBitmap[i1][i2][i3] == targetBitmap[i1][i2][i3]);
          }



if (dbgOn)
  System.out.printf("-------------- %d ----------------\n", counter);

counter++;

if (dbgOn) {
  System.out.printf("B - sourceUpdater.prepared = %s, targetUpdater.prepared = %s\n",
    sourceUpdater.prepared ? "true" : "false", targetUpdater.prepared ? "true" : "false");

  for (int i1=0 ; i1 < r12 ; i1++)
    for (int i2=0 ; i2 < r12 ; i2++)
      if (sourceBitmap[i1][i2])
        if (newSourceBitmap[i1][i2])
          System.out.printf("%d %d\n", i1, i2);
        else
          System.out.printf("%d %d -\n", i1, i2);
      else if (newSourceBitmap[i1][i2])
        System.out.printf("%d %d +\n", i1, i2);

  System.out.println();

  for (int i1=0 ; i1 < r12 ; i1++)
    for (int i2=0 ; i2 < r12 ; i2++)
      for (int i3=0 ; i3 < r3 ; i3++)
        if (targetBitmap[i1][i2][i3])
          if (newTargetBitmap[i1][i2][i3])
            System.out.printf("%d %d %d\n", i1, i2, i3);
          else
            System.out.printf("%d %d %d -\n", i1, i2, i3);
        else if (newTargetBitmap[i1][i2][i3])
          System.out.printf("%d %d %d +\n", i1, i2, i3);

  System.out.println();
  System.out.println();
}

      // Inserting and removing a few values at random in or from source
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx1 = rand.nextInt(r12);
        int idx2 = rand.nextInt(r12);
        int surr1 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx1]));
        int surr2 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx2]));
        if (sourceBitmap[idx1][idx2]) {
          sourceUpdater.delete(surr1, surr2);
if (dbgOn)
  System.out.printf("S+ %d %d\n", idx1, idx2);
        }
        else {
          sourceUpdater.insert(surr1, surr2);
if (dbgOn)
  System.out.printf("S- %d %d\n", idx1, idx2);
        }
        newSourceBitmap[idx1][idx2] = !sourceBitmap[idx1][idx2];
        if (idx1 != idx2)
          newSourceBitmap[idx2][idx1] = newSourceBitmap[idx1][idx2];
      }

      // Inserting and removing a few values at random in or from target
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx1 = rand.nextInt(r12);
        int idx2 = rand.nextInt(r12);
        int idx3 = rand.nextInt(r3);
        int surr1 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx1]));
        int surr2 = storeUpdater12.lookupOrInsertValue(Conversions.stringToObj(values12[idx2]));
        int surr3 = storeUpdater3.lookupOrInsertValue(Conversions.convertText(values3[idx3]));
        if (targetBitmap[idx1][idx2][idx3]) {
          targetUpdater.delete(surr1, surr2, surr3);
if (dbgOn)
  System.out.printf("T+ %d %d %d\n", idx1, idx2, idx3);
        }
        else {
          targetUpdater.insert(surr1, surr2, surr3);
if (dbgOn)
  System.out.printf("T- %d %d %d\n", idx1, idx2, idx3);
        }
        newTargetBitmap[idx1][idx2][idx3] = !targetBitmap[idx1][idx2][idx3];
        if (idx1 != idx2)
          newTargetBitmap[idx2][idx1][idx3] = newTargetBitmap[idx1][idx2][idx3];
      }

      boolean ok = true;
      for (int i1=0 ; i1 < r12 ; i1++)
        for (int i2=0 ; i2 < r12 ; i2++)
          if (newSourceBitmap[i1][i2]) {
            boolean found = false;
            for (int i3=0 ; i3 < r3 ; i3++)
              if (newTargetBitmap[i1][i2][i3]) {
                found = true;
                break;
              }
            if (!found) {
              ok = false;
              break;
            }
          }

if (dbgOn)
  System.out.printf("C - sourceUpdater.prepared = %s, targetUpdater.prepared = %s\n",
    sourceUpdater.prepared ? "true" : "false", targetUpdater.prepared ? "true" : "false");

      boolean debugIt = sourceUpdater.checkForeignKeys_12(targetUpdater) != ok;

      if (debugIt) {
        System.out.printf("ERROR: _t = %d, counter = %d, r12 = %d, r3 = %d, ok = %s, okCount = %d, notOkCount = %d\n",
          _t, counter, r12, r3, ok ? "true" : "false", okCount, notOkCount);

        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++)
            if (sourceBitmap[i1][i2])
              if (newSourceBitmap[i1][i2])
                System.out.printf("%d %d\n", i1, i2);
              else
                System.out.printf("%d %d -\n", i1, i2);
            else if (newSourceBitmap[i1][i2])
              System.out.printf("%d %d +\n", i1, i2);

        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++)
            for (int i3=0 ; i3 < r3 ; i3++)
              if (targetBitmap[i1][i2][i3])
                if (newTargetBitmap[i1][i2][i3])
                  System.out.printf("%d %d %d\n", i1, i2, i3);
                else
                  System.out.printf("%d %d %d -\n", i1, i2, i3);
              else if (newTargetBitmap[i1][i2][i3])
                System.out.printf("%d %d %d +\n", i1, i2, i3);

        ok = sourceUpdater.checkForeignKeys_12(targetUpdater);

        System.exit(1);
      }

      if (ok) {
        int deletedFromSource = 0;
        int insertedIntoSource = 0;
        for (int i1=0 ; i1 < r12 ; i1++)
          for (int i2=0 ; i2 < r12 ; i2++) {
              if (sourceBitmap[i1][i2] & !newSourceBitmap[i1][i2])
                deletedFromSource++;
              if (!sourceBitmap[i1][i2] & newSourceBitmap[i1][i2])
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
              boolean inSource = sourceBitmap[i1][i2];
              boolean inTarget = targetBitmap[i1][i2][i3];

              int surr1 = store12.valueToSurr(Conversions.stringToObj(values12[i1]));
              int surr2 = store12.valueToSurr(Conversions.stringToObj(values12[i2]));
              int surr3 = store3.valueToSurr(Conversions.convertText(values3[i3]));

              if (surr1 == -1 | surr2 == -1) {
                // If either the first or the second argument is not in the
                // corresponding value store, then neither table can contain it
                check(!inSource & !inTarget);
                continue;
              }

              if (surr3 == -1) {
                // If the third argument is not in the corresponding value store,
                // then the entry cannot appear in the target relation
                check(!inTarget);
                continue;
              }

              check(target.contains(surr1, surr2, surr3) == inTarget);
              check(target.contains(surr2, surr1, surr3) == inTarget);
              check(source.contains(surr1, surr2) == inSource);
              check(source.contains(surr2, surr1) == inSource);
            }
      }
      else {
        notOkCount++;
      }

      storeUpdater12.reset();
      storeUpdater3.reset();
      sourceUpdater.reset();
      targetUpdater.reset();

if (dbgOn)
  System.out.printf("C - sourceUpdater.prepared = %s, targetUpdater.prepared = %s\n",
    sourceUpdater.prepared ? "true" : "false", targetUpdater.prepared ? "true" : "false");
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
