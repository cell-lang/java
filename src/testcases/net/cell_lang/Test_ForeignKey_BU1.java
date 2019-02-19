package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.math.BigInteger;


class Test_ForeignKey_BU1 {
  static Random rand = new Random(0);

  public static void run() {
    for (int r2=1 ; r2 < 40 ; r2++) {
      ValueStore store1 = new ValueStore();
      ValueStore store2 = new ValueStore();
      BinaryTable source = new BinaryTable(store1, store2);
      UnaryTable target = new UnaryTable(store1);

      ValueStoreUpdater  storeUpdater1 = new ValueStoreUpdater(store1);
      ValueStoreUpdater  storeUpdater2 = new ValueStoreUpdater(store2);
      BinaryTableUpdater sourceUpdater = new BinaryTableUpdater(source, storeUpdater1, storeUpdater2);
      UnaryTableUpdater  targetUpdater = new UnaryTableUpdater(target, storeUpdater1);

      int[] values1 = new int[1000];
      String[] values2 = new String[r2];

      for (int i=0 ; i < 1000 ; i++)
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

      String charset = "abcdefghijklmnopqrstuvwxyz0123456789";

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

      for (int i=0 ; i < r2 ; i++) {
        String value = values2[i];
        Obj obj = Conversions.stringToObj(value);
        String repr = obj.toString();
        check(repr.equals('"' + value + '"'));
      }


      boolean[][] sourceBitmap = new boolean[1000][];
      for (int i=0 ; i < 1000 ; i++)
        sourceBitmap[i] = new boolean[r2];
      boolean[] targetBitmap = new boolean[1000];

      int okCount = 0;
      int okCountDeleted = 0;
      int okCountInserted = 0;
      int notOkCount = 0;

      for (int _t=0 ; _t < 1000000 ; _t++) {
        boolean[][] newSourceBitmap = new boolean[1000][];
        for (int i=0 ; i < 1000 ; i++)
          newSourceBitmap[i] = Arrays.copyOf(sourceBitmap[i], sourceBitmap[i].length);
        boolean[] newTargetBitmap = Arrays.copyOf(targetBitmap, targetBitmap.length);

        // Inserting and removing a few values at random in or from source
        for (int i=0 ; i < rand.nextInt(20) ; i++) {
          int idx1 = rand.nextInt(1000);
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
          int idx = rand.nextInt(1000);
          int surr = storeUpdater1.lookupOrInsertValue(IntObj.get(values1[idx]));
          if (targetBitmap[idx])
            targetUpdater.delete(surr);
          else
            targetUpdater.insert(surr);
          newTargetBitmap[idx] = !targetBitmap[idx];
        }

        boolean ok = true;
        for (int i=0 ; i < 1000 ; i++)
          for (int j=0 ; j < r2 ; j++)
            if (newSourceBitmap[i][j] & !newTargetBitmap[i]) {
              ok = false;
              break;
            }

        boolean debugIt = sourceUpdater.checkForeignKeys_1(targetUpdater) != ok;

        if (debugIt) {
          System.out.printf("ERROR: _t = %d, r2 = %d, ok = %s, okCount = %d, notOkCount = %d\n",
            _t, r2, ok ? "true" : "false", okCount, notOkCount);

          System.out.println("\nTarget only:");
          for (int i=0 ; i < 1000 ; i++)
            if (newTargetBitmap[i]) {
              boolean found = false;
              for (int j=0 ; j < r2 ; j++)
                if (newSourceBitmap[i][j]) {
                  found = true;
                  break;
                }
              if (!found)
                System.out.printf("%03d: %4d -> %3d\n", i, values1[i],
                  storeUpdater1.lookupValueEx(IntObj.get(values1[i]))
                );
            }

          System.out.println("\nSource only:");
          for (int i=0 ; i < 1000 ; i++)
            for (int j=0 ; j < r2 ; j++)
              if (newSourceBitmap[i][j] & !newTargetBitmap[i])
                System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
                  storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
                  storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
                );

          System.out.println("\nBoth:");
          for (int i=0 ; i < 1000 ; i++)
            for (int j=0 ; j < r2 ; j++)
              if (newSourceBitmap[i][j] & newTargetBitmap[i])
                System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
                  storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
                  storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
                );

          System.out.println("\nDeleted from source:");
          for (int i=0 ; i < 1000 ; i++)
            for (int j=0 ; j < r2 ; j++)
              if (sourceBitmap[i][j] & !newSourceBitmap[i][j])
                System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
                  storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
                  storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
                );

          System.out.println("\nInserted into source:");
          for (int i=0 ; i < 1000 ; i++)
            for (int j=0 ; j < r2 ; j++)
              if (!sourceBitmap[i][j] & newSourceBitmap[i][j])
                System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i, j, values1[i], values2[j],
                  storeUpdater1.lookupValueEx(IntObj.get(values1[i])),
                  storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[j]))
                );

          System.out.println("\nDeleted from target:");
          for (int i=0 ; i < 1000 ; i++)
            if (targetBitmap[i] & !newTargetBitmap[i])
              System.out.printf("%03d: %4d -> %3d\n", i, values1[i],
                storeUpdater1.lookupValueEx(IntObj.get(values1[i]))
              );

          System.out.println("\nInserted into target:");
          for (int i=0 ; i < 1000 ; i++)
            if (!targetBitmap[i] & newTargetBitmap[i])
              System.out.printf("%03d: %4d -> %3d\n", i, values1[i],
                storeUpdater1.lookupValueEx(IntObj.get(values1[i]))
              );

          System.out.println("\n--------------------------------------\n");

          // boolean refOk = true;
          // for (int i=0 ; i < 1000 ; i++)
          //   for (int j=0 ; j < r2 ; j++)
          //     if (newSourceBitmap[i][j] & !newTargetBitmap[i])
          //       refOk = false;

          // ok = sourceUpdater.checkForeignKeys_1(targetUpdater);

          // System.exit(1);
        }

        if (ok) {
          int deletedFromSource = 0;
          int insertedIntoSource = 0;
          for (int i=0 ; i < 1000 ; i++)
            for (int j=0 ; j < r2 ; j++) {
              if (sourceBitmap[i][j] & !newSourceBitmap[i][j])
                deletedFromSource++;
              if (!sourceBitmap[i][j] & newSourceBitmap[i][j])
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

          storeUpdater1.apply();
          storeUpdater2.apply();

          sourceUpdater.apply();
          targetUpdater.apply();

          sourceUpdater.finish();
          targetUpdater.finish();

          for (int i1=0 ; i1 < 1000 ; i1++)
            for (int i2=0 ; i2 < r2 ; i2++) {
              boolean inSource = sourceBitmap[i1][i2];
              boolean inTarget = targetBitmap[i1];

              int surr1 = store1.lookupValue(IntObj.get(values1[i1]));
              int surr2 = store2.lookupValue(Conversions.stringToObj(values2[i2]));

              if (surr1 == -1) {
                check(!inSource & !inTarget);
                continue;
              }

              check(target.contains(surr1) == inTarget);

              if (surr2 == -1) {
                check(!inSource);
                continue;
              }

              if (source.contains(surr1, surr2) != inSource) {
                System.out.printf("_t = %d, r2 = %d, inSource = %s\n", _t, r2, inSource ? "true" : "false");

                System.out.printf("%03d %03d: (%4d, %10s) -> (%3d, %2d)\n", i1, i2, values1[i1], values2[i2],
                  storeUpdater1.lookupValueEx(IntObj.get(values1[i1])),
                  storeUpdater2.lookupValueEx(Conversions.stringToObj(values2[i2]))
                );

              }

              check(source.contains(surr1, surr2) == inSource);
            }
        }
        else {
          notOkCount++;
        }

        storeUpdater1.reset();
        storeUpdater2.reset();
        sourceUpdater.reset();
        targetUpdater.reset();

        check(target.size() == store1.count());
      }

      System.out.printf("ok: %d - %d - %d, not ok: %d\n", okCount, okCountDeleted, okCountInserted, notOkCount);
      System.out.printf("Size: %d -> %d\n", source.size(), target.size());
      System.out.println();
    }
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}