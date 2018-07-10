package net.cell_lang;

import java.io.Writer;


final class NeHashMapObj extends NeBinRelObj {
  int[] hashtable;
  int[] buckets;

  int[] revIdxs;

  int minPrintedSize = -1;

  static final BinRelIter nullIter = new BinRelIter(new Obj[0], new Obj[0]);


  public NeHashMapObj(Obj[] keys, Obj[] values) {
    Miscellanea._assert(keys.length > 0 & keys.length == values.length);

    int size = keys.length;
    long dataSum = 0;
    for (int i=0 ; i < size ; i++)
      dataSum += keys[i].data + values[i].data;
    data = binRelObjData(size, dataSum);
    extraData = neBinRelObjExtraData();

    col1 = keys;
    col2 = values;
    isMap = true;

    int len = keys.length;

    hashtable = new int[len];
    buckets = new int[len];

    for (int i=0 ; i < len ; i++) {
      Obj key = keys[i];
      int hashcode = Utils.hashcode(key.data, key.extraData);
      int index = hashcode % len;
      if (index < 0)
        index = -index;
      int head = hashtable[index];
      hashtable[index] = i + 1;
      if (head != 0)
        buckets[i] = head;
    }
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean hasKey(Obj key) {
    int hashcode = Utils.hashcode(key.data, key.extraData);
    int index = hashcode % hashtable.length;
    if (index < 0)
      index = -index;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = col1[entryIdx];
      if (key.isEq(entryKey))
        return true;
      entryIdx = buckets[entryIdx] - 1;
    }
    return false;
  }

  public boolean hasPair(Obj key, Obj value) {
    int hashcode = Utils.hashcode(key.data, key.extraData);
    int index = hashcode % hashtable.length;
    if (index < 0)
      index = -index;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = col1[entryIdx];
      if (key.isEq(entryKey))
        return value.isEq(col2[entryIdx]);
      entryIdx = buckets[entryIdx] - 1;
    }
    return false;
  }

  public BinRelIter getBinRelIterByCol1(Obj key) {
    int hashcode = Utils.hashcode(key.data, key.extraData);
    int index = hashcode % hashtable.length;
    if (index < 0)
      index = -index;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = col1[entryIdx];
      if (key.isEq(entryKey))
        return new BinRelIter(col1, col2, entryIdx, entryIdx);
      entryIdx = buckets[entryIdx] - 1;
    }
    return nullIter;
  }

  public Obj lookup(Obj key) {
    int hashcode = Utils.hashcode(key.data, key.extraData);
    int index = hashcode % hashtable.length;
    if (index < 0)
      index = -index;
    int entryIdx = hashtable[index] - 1;
    while (entryIdx != -1) {
      Obj entryKey = col1[entryIdx];
      if (key.isEq(entryKey))
        return col2[entryIdx];
      entryIdx = buckets[entryIdx] - 1;
    }
    throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
  }
}
