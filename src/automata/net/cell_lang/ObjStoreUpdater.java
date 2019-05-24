package net.cell_lang;


class ObjStoreUpdater extends ValueStoreUpdater {
  static final int INIT_SIZE = 32;

  private Obj[] values     = new Obj[INIT_SIZE];
  private int[] hashcodes  = new int[INIT_SIZE];
  private int[] surrogates = new int[INIT_SIZE];

  private int[] hashtable  = new int[INIT_SIZE];
  private int[] buckets    = new int[INIT_SIZE];

  private int count = 0;
  private int hashRange = 0;
  private int lastSurrogate = -1;

  private ObjStore store;

  //////////////////////////////////////////////////////////////////////////////

  public ObjStoreUpdater(ObjStore store) {
    Array.fill(hashtable, -1);
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
      store.insert(values[i], hashcodes[i], surrogates[i]);
  }

  public void reset() {
    if (hashRange != 0)
      Array.fill(hashtable, hashRange, -1);

    count = 0;
    hashRange = 0;
    lastSurrogate = -1;
  }

  public int lookupOrInsertValue(Obj value) {
    int surr = valueToSurr(value);
    if (surr != -1)
      return surr;
    return insert(value);
  }

  // Inefficient, but used only for debugging
  public Obj surrToValue(int surr) {
    for (int i=0 ; i < count ; i++)
      if (surrogates[i] == surr)
        return values[i];
    return store.surrToValue(surr);
  }

  //////////////////////////////////////////////////////////////////////////////

  private int insert(Obj value) {
    Miscellanea._assert(count <= values.length);

    lastSurrogate = store.nextFreeIdx(lastSurrogate);
    int hashcode = value.hashcode();

    if (count == values.length)
      resize();

    values[count]     = value;
    hashcodes[count]  = hashcode;
    surrogates[count] = lastSurrogate;

    if (count >= 16) {
      if (count >= hashRange) {
        if (hashRange != 0) {
          Array.fill(hashtable, hashRange, -1);
          hashRange *= 2;
        }
        else
          hashRange = 16;

        for (int i=0 ; i < count ; i++)
          insertIntoHashtable(i, hashcodes[i]);
      }
      insertIntoHashtable(count, hashcode);
    }
    count++;

    return lastSurrogate;
  }

  private int valueToSurr(Obj value) {
    int surrogate = store.valueToSurr(value);
    if (surrogate != -1)
      return surrogate;

    if (count > 0) {
      int hashcode = value.hashcode(); //## BAD BAD BAD: CALCULATING THE HASHCODE TWICE

      if (hashRange == 0) {
        for (int i=0 ; i < count ; i++)
          if (hashcodes[i] == hashcode && values[i].isEq(value))
            return surrogates[i];
      }
      else {
        int hashIdx = Integer.remainderUnsigned(hashcode, hashRange);
        for (int i = hashtable[hashIdx] ; i != -1 ; i = buckets[i])
          if (hashcodes[i] == hashcode && values[i].isEq(value))
            return surrogates[i];
      }
    }

    return -1;
  }

  private void resize() {
    Miscellanea._assert(hashRange == values.length);

    int currCapacity = values.length;
    int newCapacity = 2 * currCapacity;

    Obj[] currValues     = values;
    int[] currHashcodes  = hashcodes;
    int[] currSurrogates = surrogates;

    values     = new Obj[newCapacity];
    hashcodes  = new int[newCapacity];
    hashtable  = new int[newCapacity];
    buckets    = new int[newCapacity];
    surrogates = new int[newCapacity];
    hashRange  = newCapacity;

    Array.copy(currValues, values, currCapacity);
    Array.copy(currHashcodes, hashcodes, currCapacity);
    Array.copy(currSurrogates, surrogates, currCapacity);
    Array.fill(hashtable, -1);

    for (int i=0 ; i < count ; i++)
      insertIntoHashtable(i, hashcodes[i]);
  }

  private void insertIntoHashtable(int index, int hashcode) {
    int hashIdx = Integer.remainderUnsigned(hashcode, hashRange);
    buckets[index] = hashtable[hashIdx];
    hashtable[hashIdx] = index;
  }
}
