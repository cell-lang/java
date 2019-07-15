package net.cell_lang;

import java.util.Random;
import java.util.Arrays;
import java.util.ArrayList;


class Test_ValueStore {
  static Random random = new Random(0);
  static String charset = "abcdefghijklmnopqrstuvwxyz0123456789";

  static final int MAX_NUM_OF_VALS = 10000;

  public static void run() {
    for (int i=1 ; i < MAX_NUM_OF_VALS ; i++) {
      run(i, 10000);
      if (i != 0 & i % 10 == 0)
        System.out.print(".");
    }
    System.out.println();
  }

  public static void run(int numOfVals, int numOfOps) {
    ValueStore store = new ValueStore();
    ValueStoreUpdater updater = new ValueStoreUpdater(store);

    String[] strings = randomStrs(numOfVals, 20);
    Obj[] values = new Obj[numOfVals];
    for (int i=0 ; i < numOfVals ; i++)
      values[i] = Conversions.stringToObj(strings[i]);
    int[] surrogates = new int[numOfVals];
    Arrays.fill(surrogates, -1);
    int[] refCounts = new int[numOfVals];

    for (int _i=0 ; _i < numOfOps ; _i++) {
      // Values to insert
      int addCount = random.nextInt(50);
      int[] newValues = new int[addCount];
      for (int i=0 ; i < addCount ; i++)
        newValues[i] = random.nextInt(numOfVals);

      // Values to release
      int releasedCount = random.nextInt(50);
      int[] releasedValues = new int[releasedCount];
      for (int i=0 ; i < releasedCount ; i++)
        releasedValues[i] = random.nextInt(numOfVals);

      for (int i=0 ; i < addCount ; i++) {
        int idx = newValues[i];
        if (idx >= values.length)
          System.out.printf("idx = %d, values.length = %d\n", idx, values.length);
        Obj value = values[idx];
        int surr = surrogates[idx];
        // check(surr == store.valueToSurr(value));
        if (surr != -1) {
          check(surr == updater.lookupOrInsertValue(value));
        }
        else {
          surr = updater.lookupOrInsertValue(value);
          surrogates[idx] = surr;
        }
      }

      updater.apply();
      for (int i=0 ; i < addCount ; i++) {
        int idx = newValues[i];
        int surr = surrogates[idx];
        store.addRef(surr);
        refCounts[idx]++;
      }

      for (int i=0 ; i < releasedCount ; i++) {
        int idx = releasedValues[i];
        int surr = surrogates[idx];
        int refCount = refCounts[idx];
        if (refCount > 0) {
          store.release(surr);
          refCounts[idx] = refCount - 1;
          if (refCount == 1)
            surrogates[idx] = -1;
        }
      }

      updater.reset();

      check(store, values, surrogates);
    }
  }

  static void check(ValueStore store, Obj[] values, int[] surrogates) {
    int count = 0;
    for (int i=0 ; i < values.length ; i++) {
      check(store.valueToSurr(values[i]) == surrogates[i]);
      if (surrogates[i] != -1)
        count++;
    }
    check(store.count() == count);
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }

  static String[] randomStrs(int count, int maxLen) {
    String[] strs = new String[count];
    for (int i=0 ; i < count ; ) {
      String str = randomStr(maxLen);
      if (!contains(strs, str))
        strs[i++] = str;
    }
    return strs;
  }

  static String randomStr(int maxLen) {
    int len = random.nextInt(maxLen);
    char[] chars = new char[len];
    for (int j=0 ; j < len ; j++)
      chars[j] = charset.charAt(random.nextInt(charset.length()));
    return new String(chars);
  }

  static boolean contains(String[] strings, String string) {
    for (int i=0 ; i < strings.length ; i++)
      if (strings[i] != null && string.equals(strings[i]))
        return true;
    return false;
  }
}


class RefValueStore {

}


class RefValueStoreUpdater {

}


// int[] values1 = new int[1000];

// for (int i=0 ; i < 1000 ; i++)
//   for ( ; ; ) {
//     int value = rand.nextInt(10000);
//     boolean found = false;
//     for (int j=0 ; j < i ; j++)
//       if (values1[j] == value) {
//         found = true;
//         break;
//       }
//     if (!found) {
//       values1[i] = value;
//       break;
//     }
//   }
