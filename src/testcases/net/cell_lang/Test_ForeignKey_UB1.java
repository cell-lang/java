package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


class Test_ForeignKey_UB1 {
  static Random rand = new Random(0);

  public static void run() {
    for (int r1=1 ; r1 < 40 ; r1++)
      for (int r2=1 ; r2 < 40 ; r2++)
        new Test_ForeignKey_UB1().run(r1, r2, 10000);

    for (int r1=1 ; r1 < 40 ; r1++)
      new Test_ForeignKey_UB1().run(r1, 1000, 10000);
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  ValueStore store1;
  ValueStore store2;
  BinaryTable source;
  UnaryTable target;

  ValueStoreUpdater storeUpdater1;
  ValueStoreUpdater storeUpdater2;
  BinaryTableUpdater sourceUpdater;
  UnaryTableUpdater targetUpdater;

  int[] values1;
  String[] values2;

  boolean[][] sourceBitmap;
  boolean[] targetBitmap;

  boolean[][] newSourceBitmap;
  boolean[] newTargetBitmap;

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  void run(int r1, int r2, int testCount) {
    store1 = new ValueStore();
    store2 = new ValueStore();
    source = new BinaryTable(store1, store2);
    target = new UnaryTable(store1);

    storeUpdater1 = new ValueStoreUpdater(store1);
    storeUpdater2 = new ValueStoreUpdater(store2);
    sourceUpdater = new BinaryTableUpdater(source, storeUpdater1, storeUpdater2);
    targetUpdater = new UnaryTableUpdater(target, storeUpdater1);

    values1 = new int[r1];
    values2 = new String[r2];

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

    sourceBitmap = new boolean[r1][];
    for (int i=0 ; i < r1 ; i++)
      sourceBitmap[i] = new boolean[r2];
    targetBitmap = new boolean[r1];

    int okCount = 0;
    int okCountDeleted = 0;
    int okCountInserted = 0;
    int notOkCount = 0;

    for (int _t=0 ; _t < testCount ; _t++) {
      newSourceBitmap = new boolean[r1][];
      for (int i=0 ; i < r1 ; i++)
        newSourceBitmap[i] = Arrays.copyOf(sourceBitmap[i], sourceBitmap[i].length);
      newTargetBitmap = Arrays.copyOf(targetBitmap, targetBitmap.length);

      // Inserting and removing a few values at random in or from source
      for (int i=0 ; i < rand.nextInt(20) ; i++) {
        int idx1 = rand.nextInt(r1);
        int idx2 = rand.nextInt(r2);
        int surr1 = storeUpdater1.lookupOrInsertValue(IntObj.get(values1[idx1]));
        int surr2 = storeUpdater2.lookupOrInsertValue(Conversions.stringToObj(values2[idx2]));
        if (sourceBitmap[idx1][idx2])
          sourceUpdater.delete(surr1, surr2);
        else
          sourceUpdater.insert(surr1, surr2);
        newSourceBitmap[idx1][idx2] = !sourceBitmap[idx1][idx2];
      }

      // Inserting and removing a few values at random in or from source
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
          for (int i2=0 ; i2 < r2 ; i2++)
            if (newSourceBitmap[i1][i2]) {
              eltOk = true;
              break;
            }
          if (!eltOk) {
            ok = false;
            break;
          }
        }

      boolean debugIt = targetUpdater.checkForeignKeys_1(sourceUpdater) != ok;

      if (debugIt) {
        System.out.printf("ERROR: _t = %d, r1 = %d, r2 = %d, ok = %s, okCount = %d, notOkCount = %d\n",
          _t, r1, r2, ok ? "true" : "false", okCount, notOkCount);
        // dump(r1, r2, _t, i1, i2, surr1, surr2, inSource, inTarget);
        dump(r1, r2, _t, -100, -100, -100, -100, false, false);
        dumpDiff(r1, r2);
        targetUpdater.checkForeignKeys_1(sourceUpdater);
        System.exit(1);
      }

      if (ok) {
        int deletedFromSource = 0;
        int insertedIntoSource = 0;
        for (int i1=0 ; i1 < r1 ; i1++)
          for (int i2=0 ; i2 < r2 ; i2++) {
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

        storeUpdater1.apply();
        storeUpdater2.apply();

        sourceUpdater.apply();
        targetUpdater.apply();

        sourceUpdater.finish();
        targetUpdater.finish();

        for (int i1=0 ; i1 < r1 ; i1++)
          for (int i2=0 ; i2 < r2 ; i2++) {
            boolean inSource = sourceBitmap[i1][i2];
            boolean inTarget = targetBitmap[i1];

            int surr1 = store1.lookupValue(IntObj.get(values1[i1]));
            int surr2 = store2.lookupValue(Conversions.stringToObj(values2[i2]));

            if (surr1 == -1) {
              // If the first arguments is not in the corresponding value store,
              // then neither table can contain it
              if (!(!inSource & !inTarget))
                dump(r1, r2, _t, i1, i2, surr1, surr2, inSource, inTarget);
              check(!inSource & !inTarget);
              continue;
            }

            // Since the first argument is in the value store, it has to appear in
            // either the first column of the source relation, or in the target one.
            // Since presence in the latter implies presence in the former, the
            // source relation must always contain it.
            check(source.contains1(surr1));

            if (surr2 == -1) {
              // If the first argument is not in the corresponding value store,
              // then the entry cannot appear in the source relation
              check(!inSource);
              continue;
            }

            check(source.contains(surr1, surr2) == inSource);
            check(target.contains(surr1) == inTarget);
          }
      }
      else {
        notOkCount++;
      }

      storeUpdater1.reset();
      storeUpdater2.reset();
      sourceUpdater.reset();
      targetUpdater.reset();

      // check(target.size() == store2.count());
    }

    System.out.printf(
      "r1 = %d, r2 = %d, ok: %d - %d - %d, not ok: %d, size: %d -> %d\n\n",
      r1, r2, okCount, okCountDeleted, okCountInserted, notOkCount, source.size(), target.size()
    );
  }


  void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  void dump(int r1, int r2, int _t, int i1, int i2, int surr1, int surr2, boolean inSource, boolean inTarget) {
    System.out.printf(
      "r1 = %d, r2 = %d, _t = %d, i1 = %d, i2 = %d, surr1 = %d, surr2 = %d, inSource = %s, inTarget = %s\n",
      r1, r2, _t, i1, i2, surr1, surr2, inSource ? "true" : "false", inTarget ? "true" : "false"
    );

    System.out.printf("source.size() = %d, target.size() = %d\n", source.size(), target.size());

    System.out.print("\n\n     ");
    for (int j=0 ; j < r2 ; j++)
      System.out.printf(" %3d", store2.lookupValue(Conversions.stringToObj(values2[j])));
    System.out.println();

    for (int j1=0 ; j1 < r1 ; j1++) {
      System.out.printf("%3d  ", store1.lookupValue(IntObj.get(values1[j1])));
      for (int j2=0 ; j2 < r2 ; j2++) {
        boolean refIn = sourceBitmap[j1][j2];
        int surrJ1 = store1.lookupValue(IntObj.get(values1[j1]));
        int surrJ2 = store2.lookupValue(Conversions.stringToObj(values2[j2]));
        boolean in = source.contains(surrJ1, surrJ2);
        System.out.printf("  %s%s", in != refIn ? "*" : " ", in ? "1" : "0");
      }
      System.out.println();
    }
    System.out.println();
    System.out.print("     ");
    for (int j2=0 ; j2 < r2 ; j2++)
      System.out.print(targetBitmap[j2] ? "   1" : "   0");
    System.out.println("\n\n---------------------------------\n");

    // System.out.print("     ");
    // for (int j=0 ; j < r2 ; j++)
    //   System.out.printf(" %3d", store2.lookupValue(Conversions.stringToObj(values2[j])));
    // System.out.println();

    // for (int j1=0 ; j1 < r1 ; j1++) {
    //   System.out.printf("%3d  ", store1.lookupValue(IntObj.get(values1[j1])));
    //   for (int j2=0 ; j2 < r2 ; j2++) {
    //     int surrJ1 = store1.lookupValue(IntObj.get(values1[j1]));
    //     int surrJ2 = store2.lookupValue(Conversions.stringToObj(values2[j2]));
    //     System.out.print(source.contains(surrJ1, surrJ2) ? "   1" : "   0");
    //   }
    //   System.out.println();
    // }
    // System.out.println();
    // System.out.print("     ");
    // for (int j2=0 ; j2 < r2 ; j2++) {
    //   int surrJ2 = store2.lookupValue(Conversions.stringToObj(values2[j2]));
    //   System.out.print(target.contains(surrJ2) ? "   1" : "   0");
    // }
    // System.out.println("\n\n---------------------------------\n");

    for (int j=0 ; j < r1 ; j++)
      System.out.printf(" %3d", store1.lookupValue(IntObj.get(values1[j])));
    System.out.println();
    for (int j=0 ; j < r1 ; j++)
      System.out.printf(" %3d", store2.lookupValue(IntObj.get(values1[j])));
    System.out.println("\n\n---------------------------------\n");

    for (int j=0 ; j < r2 ; j++)
      System.out.printf(" %3d", store1.lookupValue(Conversions.stringToObj(values2[j])));
    System.out.println();
    for (int j=0 ; j < r2 ; j++)
      System.out.printf(" %3d", store2.lookupValue(Conversions.stringToObj(values2[j])));
    System.out.println("\n\n==================================================\n");
  }

  void dumpDiff(int r1, int r2) {
    System.out.println("\nDeleted from source:");
    for (int i=0 ; i < r1 ; i++)
      for (int j=0 ; j < r2 ; j++)
        if (sourceBitmap[i][j] & !newSourceBitmap[i][j])
          System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
            storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
            storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
          );

    System.out.println("\nInserted into source:");
    for (int i=0 ; i < r1 ; i++)
      for (int j=0 ; j < r2 ; j++)
        if (!sourceBitmap[i][j] & newSourceBitmap[i][j])
          System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
            storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
            storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
          );

    System.out.println("\nDeleted from target:");
    for (int i=0 ; i < r2 ; i++)
      if (targetBitmap[i] & !newTargetBitmap[i])
        System.out.printf("%03d: %4d -> %3d\n", i, values1[i],
          storeUpdater1.lookupValueEx(IntObj.get(values1[i]))
        );

    System.out.println("\nInserted into target:");
    for (int i=0 ; i < r2 ; i++)
      if (!targetBitmap[i] & newTargetBitmap[i])
        System.out.printf("%03d: %4d -> %3d\n", i, values1[i],
          storeUpdater1.lookupValueEx(IntObj.get(values1[i]))
        );

    System.out.println("\n\n**************************************************\n");
  }
}

// System.out.println("\nTarget only:");
// for (int i=0 ; i < r2 ; i++)
//   if (newTargetBitmap[i]) {
//     boolean found = false;
//     for (int j=0 ; j < r1 ; j++)
//       if (newSourceBitmap[j][i]) {
//         found = true;
//         break;
//       }
//     if (!found)
//       System.out.printf("%03d: %4d -> %3d\n", i, values1[i],
//         storeUpdater1.lookupValueEx(IntObj.get(values1[i]))
//       );
//   }

// System.out.println("\nSource only:");
// for (int i=0 ; i < 1000 ; i++)
//   for (int j=0 ; j < r2 ; j++)
//     if (newSourceBitmap[i][j] & !newTargetBitmap[i])
//       System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
//         storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
//         storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
//       );

// System.out.println("\nBoth:");
// for (int i=0 ; i < 1000 ; i++)
//   for (int j=0 ; j < r2 ; j++)
//     if (newSourceBitmap[i][j] & newTargetBitmap[i])
//       System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
//         storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
//         storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
//       );


// System.out.println("\n--------------------------------------\n");

// boolean refOk = true;
// for (int i=0 ; i < 1000 ; i++)
//   for (int j=0 ; j < r2 ; j++)
//     if (newSourceBitmap[i][j] & !newTargetBitmap[i])
//       refOk = false;





// if (source.contains(surr1, surr2) != inSource) {
//   System.out.printf("_t = %d, r2 = %d, inSource = %s\n", _t, r2, inSource ? "true" : "false");

//   System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i1, i2, values1[i1], values2[i2],
//     storeUpdater1.lookupValueEx(IntObj.get(values1[i1])),
//     storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[i2]))
//   );

// }
