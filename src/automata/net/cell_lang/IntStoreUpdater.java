package net.cell_lang;


class IntStoreUpdater extends ValueStoreUpdater {
  static final int INIT_SIZE = 32;

  private long[] values     = new long[INIT_SIZE];
  private int[]  surrogates = new int[INIT_SIZE];

  private int[] hashtable  = new int[INIT_SIZE];
  private int[] buckets    = new int[INIT_SIZE];

  private int count = 0;
  private int hashRange = 0;
  private int lastSurrogate = -1;

  private IntStore store;

  //////////////////////////////////////////////////////////////////////////////

  public IntStoreUpdater(IntStore store) {
    Miscellanea.arrayFill(hashtable, -1);
    this.store = store;
  }

  public void apply() {
    if (count == 0)
      return;

    int storeCapacity = store.capacity();
    int reqCapacity = store.count() + count;

    if (storeCapacity < reqCapacity)
      store.resize(reqCapacity);

    for (int i=0 ; i < count ; i++)
      store.insert(values[i], surrogates[i]);
  }

  public void reset() {
    if (hashRange != 0)
      Miscellanea.arrayFill(hashtable, hashRange, -1);

    count = 0;
    hashRange = 0;
    lastSurrogate = -1;
  }

  public int lookupOrInsertValue(long value) {
    int surr = valueToSurr(value);
    if (surr != -1)
      return surr;
    return insert(value);
  }

  // Inefficient, but used only for debugging
  public Obj surrToValue(int surr) {
    for (int i=0 ; i < count ; i++)
      if (surrogates[i] == surr)
        return IntObj.get(values[i]);
    return IntObj.get(store.surrToValue(surr));
  }

  //////////////////////////////////////////////////////////////////////////////

  private int hashcode(long value) {
    return (int) (value ^ (value >> 32));
  }

  private int insert(long value) {
    Miscellanea._assert(count <= values.length);

    lastSurrogate = store.nextFreeIdx(lastSurrogate);
    int hashcode = hashcode(value);

    if (count == values.length)
      resize();

    values[count]     = value;
    surrogates[count] = lastSurrogate;

    if (count >= 16) {
      if (count >= hashRange) {
        if (hashRange != 0) {
          Miscellanea.arrayFill(hashtable, hashRange, -1);
          hashRange *= 2;
        }
        else
          hashRange = 16;

        for (int i=0 ; i < count ; i++)
          insertIntoHashtable(i, hashcode(values[i]));
      }
      insertIntoHashtable(count, hashcode);
    }
    count++;

    return lastSurrogate;
  }

  private int valueToSurr(long value) {
    int surrogate = store.valueToSurr(value);
    if (surrogate != -1)
      return surrogate;

    if (count > 0) {
      if (hashRange == 0) {
        for (int i=0 ; i < count ; i++)
          if (values[i] == value)
            return surrogates[i];
      }
      else {
        int hashIdx = Integer.remainderUnsigned(hashcode(value), hashRange);
        for (int i = hashtable[hashIdx] ; i != -1 ; i = buckets[i])
          if (values[i] == value)
            return surrogates[i];
      }
    }

    return -1;
  }

  private void resize() {
    Miscellanea._assert(hashRange == values.length);

    int currCapacity = values.length;
    int newCapacity = 2 * currCapacity;

    long[] currValues     = values;
    int[]  currSurrogates = surrogates;

    values     = new long[newCapacity];
    hashtable  = new int[newCapacity];
    buckets    = new int[newCapacity];
    surrogates = new int[newCapacity];
    hashRange  = newCapacity;

    Miscellanea.arrayCopy(currValues, values, currCapacity);
    Miscellanea.arrayCopy(currSurrogates, surrogates, currCapacity);
    Miscellanea.arrayFill(hashtable, -1);

    for (int i=0 ; i < count ; i++)
      insertIntoHashtable(i, hashcode(values[i]));
  }

  private void insertIntoHashtable(int index, int hashcode) {
    int hashIdx = Integer.remainderUnsigned(hashcode, hashRange);
    buckets[index] = hashtable[hashIdx];
    hashtable[hashIdx] = index;
  }
}
