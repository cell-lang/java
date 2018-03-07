package net.cell_lang;


class UnaryTable {
  public struct Iter {
    uint index;
    UnaryTable table;

    public Iter(uint index, UnaryTable table) {
      this.table = table;
      if (table.count == 0)
        this.index = (uint) (64 * table.bitmap.length);
      else {
        this.index = index;
        if (!table.Contains(0))
          Next();
      }
    }

    public uint Get() {
      return index;
    }

    public bool Done() {
      return index >= 64 * table.bitmap.length;
    }

    public void Next() {
      int size = 64 * table.bitmap.length;
      do {
        index++;
      } while (index < size && !table.Contains(index));
    }
  }


  final int InitSize = 4;

  ulong[] bitmap = new ulong[InitSize];
  uint count = 0;

  public ValueStore store;

  public UnaryTable(ValueStore store) {
    this.store = store;
  }

  public uint Size() {
    return count;
  }

  public bool Contains(uint surr) {
    uint widx = surr / 64;
    return widx < bitmap.length && ((bitmap[widx] >> (int) (surr % 64) & 1) != 0);
  }

  public Iter GetIter() {
    return new Iter(0, this);
  }

  uint LiveCount() {
    uint liveCount = 0;
    for (int i=0 ; i < bitmap.length ; i++) {
      ulong mask = bitmap[i];
      for (int j=0 ; j < 64 ; j++)
        if (((mask >> j) & 1) != 0)
          liveCount++;
    }
    return liveCount;
  }

  public void Insert(uint surr) {
    uint widx = surr / 64;
    int bidx = (int) (surr % 64);

    int len = bitmap.length;
    if (widx >= len) {
      int newLen = 2 * len;
      while (widx >= newLen)
        newLen *= 2;
      ulong[] newBitmap = new ulong[newLen];
      Array.Copy(bitmap, newBitmap, len);
      bitmap = newBitmap;
    }

    ulong mask = bitmap[widx];
    if (((mask >> bidx) & 1) == 0) {
      bitmap[widx] = mask | (1UL << bidx);
      count++;
    }
    // Miscellanea.Assert(count == LiveCount());
  }

  public void Delete(uint surr) {
    Miscellanea.Assert(surr < 64 * bitmap.length);

    uint widx = surr / 64;
    if (widx < bitmap.length) {
      ulong mask = bitmap[widx];
      int bidx = (int) surr % 64;
      if (((mask >> bidx) & 1) == 1) {
        bitmap[widx] = mask & ~(1UL << bidx);
        count--;
      }
    }
    // Miscellanea.Assert(count == LiveCount());
  }

  public Obj Copy() {
    if (count == 0)
      return EmptyRelObj.Singleton();
    Obj[] objs = new Obj[count];
    int next = 0;
    for (uint i=0 ; i < bitmap.length ; i++) {
      ulong mask = bitmap[i];
      for (uint j=0 ; j < 64 ; j++)
        if (((mask >> (int) j) & 1) != 0)
          objs[next++] = store.GetValue(j + 64 * i);
    }
    Miscellanea.Assert(next == count);
    return Builder.CreateSet(objs, objs.length);
  }

//    public static string IntToBinaryString(int number) {
//      string binStr = "";
//      while (number != 0) {
//        binStr = (number & 1) + binStr;
//        number = number >> 1;
//      }
//      if (binStr == "")
//        binStr = "0";
//      return binStr;
//    }
//
//    public static string IntToBinaryString(ulong number) {
//      string binStr = "";
//      while (number > 0) {
//        binStr = (number & 1) + binStr;
//        number = number >> 1;
//      }
//      if (binStr == "")
//        binStr = "0";
//      return binStr;
//    }
}
