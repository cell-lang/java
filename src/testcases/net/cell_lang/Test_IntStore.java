package net.cell_lang;

import java.util.Random;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;


class Test_IntStore {
  final static int NUM_OF_VALS = 10000;

  static Random random = new Random(0);

  private static int nextIdx(int range) {
    int value = random.nextInt();
    value = value >= 0 ? value : -value;
    return value % range;
  }

  public static void run() {
    IntStore store = new IntStore();
    ObjStore refStore = new ObjStore();


    long[] values = new long[NUM_OF_VALS];
    for (int i=0 ; i < NUM_OF_VALS ; i++)
      values[i] = random.nextLong() % (2L * Integer.MAX_VALUE);


    for (int i=0 ; i < 1000000000 ; i++) {
      int idx = nextIdx(NUM_OF_VALS);
      long value = values[idx];
      Obj obj = IntObj.get(value);

      int surr = store.valueToSurr(value);
      check(surr == refStore.valueToSurr(obj));

      if (store.valueToSurr(value) != -1) {
        boolean insert = random.nextBoolean();

        if (insert) {
          surr = store.insertOrAddRef(value);
          int refSurr = refStore.insertOrAddRef(obj);
          check(surr == refSurr);
        }
        else {
          store.release(surr);
          refStore.release(surr);
        }
      }
      else {
        surr = store.insertOrAddRef(value);
        int refSurr = refStore.insertOrAddRef(obj);
        check(surr == refSurr);
      }

      // Checking
      for (int j=0 ; j < NUM_OF_VALS ; j++) {
        value = values[j];
        surr = store.valueToSurr(value);
        check(surr == refStore.valueToSurr(IntObj.get(value)));
        if (surr != -1)
          check(store.refCount(surr) == refStore.refCount(surr));
      }

      if (i > 0 & i % 10000 == 0)
        System.out.print('.');
    }
  }

  static void check(boolean cond) {
    if (!cond)
      throw new RuntimeException();
  }
}
