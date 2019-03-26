package net.cell_lang;

import java.util.HashMap;


public class SurrObjMap {
  private static final int INIT_SIZE = 32;

  int count = 0;
  int hashRange = 0;

  long[] keychains = new long[INIT_SIZE];
  Obj[]  values    = new Obj[INIT_SIZE];
  int[]  hashtable = new int[INIT_SIZE / 2];

  //////////////////////////////////////////////////////////////////////////////

  private long filledSlot(int value, int next) {
    long slot = (((long) next) << 32) | (long) value;
    Miscellanea._assert(slotValue(slot) == value);
    Miscellanea._assert(nextSlot(slot) == next);
    return slot;
  }

  private int slotValue(long slot) {
    return (int) slot;
  }

  private int nextSlot(long slot) {
    return (int) (slot >>> 32);
  }

  //////////////////////////////////////////////////////////////////////////////

  public SurrObjMap() {
    Miscellanea.arrayFill(hashtable, -1);
  }

  public void insert(int key, Obj value) {
    Miscellanea._assert(!hasKey(key));

    if (count == keychains.length)
      resize();

    values[count] = value;

    if (count < 16) {
      Miscellanea._assert(hashRange == 0);
      keychains[count++] = filledSlot(key, 0);
    }
    else {
      if (count == 16 | count == 2 * hashRange) {
        hashRange = count;
        for (int i=0 ; i < count ; i++)
          insert(slotValue(keychains[i]), i);
      }
      insert(key, count++);
    }
  }

  public void clear() {
    //## TODO: RESET IF THE THING HAS BECOME TOO LARGE
    if (hashRange != 0)
      Miscellanea.arrayFill(hashtable, -1, hashRange);
    count = 0;
    hashRange = 0;
  }

  public boolean hasKey(int key) {
    if (hashRange == 0) {
      for (int i=0 ; i < count ; i++)
        if (keychains[i] == key)
          return true;
    }
    else {
      int hashIdx = Integer.remainderUnsigned(key, hashRange);
      int idx = hashtable[hashIdx];
      while (idx != -1) {
        long slot = keychains[idx];
        if (slotValue(slot) == key)
          return true;
        idx = nextSlot(slot);
      }
    }
    return false;
  }

  //////////////////////////////////////////////////////////////////////////////

  private void insert(int key, int index) {
    Miscellanea._assert(hashRange != 0);
    int hashIdx = Integer.remainderUnsigned(key, hashRange);
    int head = hashtable[hashIdx];
    hashtable[hashIdx] = index;
    keychains[index] = filledSlot(key, head);
  }

  private void resize() {
    Miscellanea._assert(count == keychains.length);

    int newSize = 2 * count;
    long[] currKeychains = keychains;
    Obj[]  currValues    = values;

    keychains = new long[newSize];
    values    = new Obj[newSize];
    hashtable = new int[newSize / 2];

    Miscellanea.arrayCopy(currValues, values, count);
    Miscellanea.arrayFill(hashtable, -1);

    hashRange = count;
    for (int i=0 ; i < count ; i++)
      insert(slotValue(currKeychains[i]), i);
  }

  //////////////////////////////////////////////////////////////////////////////

  public int count() {
    return count;
  }

  public int keyAt(int idx) {
    return slotValue(keychains[idx]);
  }

  public Obj valueAt(int idx) {
    return values[idx];
  }
}