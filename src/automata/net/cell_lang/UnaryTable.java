package net.cell_lang;


class UnaryTable {
  public static class Iter {
    int index;
    UnaryTable table;

    public Iter(int index, UnaryTable table) {
      this.table = table;
      if (table.count == 0)
        this.index = 64 * table.bitmap.length;
      else {
        this.index = index;
        if (!table.contains(0))
          next();
      }
    }

    public int get() {
      return index;
    }

    public boolean done() {
      return index >= 64 * table.bitmap.length;
    }

    public void next() {
      int size = 64 * table.bitmap.length;
      do {
        index++;
      } while (index < size && !table.contains(index));
    }
  }


  final int InitSize = 4;

  long[] bitmap = new long[InitSize];
  int count = 0;

  public SurrObjMapper mapper;

  public UnaryTable(SurrObjMapper mapper) {
    this.mapper = mapper;
  }

  public int size() {
    return count;
  }

  public boolean contains(int surr) {
    int widx = surr / 64;
    return widx < bitmap.length && ((bitmap[widx] >>> (int) (surr % 64) & 1) != 0);
  }

  public Iter getIter() {
    return new Iter(0, this);
  }

  int liveCount() {
    int liveCount = 0;
    for (int i=0 ; i < bitmap.length ; i++) {
      long mask = bitmap[i];
      for (int j=0 ; j < 64 ; j++)
        if (((mask >>> j) & 1) != 0)
          liveCount++;
    }
    return liveCount;
  }

  public void insert(int surr) {
    int widx = surr / 64;
    int bidx = (int) (surr % 64);

    int len = bitmap.length;
    if (widx >= len) {
      int newLen = 2 * len;
      while (widx >= newLen)
        newLen *= 2;
      long[] newBitmap = new long[newLen];
      Array.copy(bitmap, newBitmap, len);
      bitmap = newBitmap;
    }

    long mask = bitmap[widx];
    if (((mask >>> bidx) & 1) == 0) {
      bitmap[widx] = mask | (1L << bidx);
      count++;
    }
    // Miscellanea._assert(count == LiveCount());
  }

  public void delete(int surr) {
    Miscellanea._assert(surr < 64 * bitmap.length);

    int widx = surr / 64;
    if (widx < bitmap.length) {
      long mask = bitmap[widx];
      int bidx = (int) surr % 64;
      if (((mask >>> bidx) & 1) == 1) {
        bitmap[widx] = mask & ~(1L << bidx);
        count--;
      }
    }
    // Miscellanea._assert(count == LiveCount());
  }

  public long[] clear(int minCapacity) {
    count = 0;
    int size = InitSize;
    while (64 * size < minCapacity)
      size *= 2;
    long[] bitmapCopy = bitmap;
    bitmap = new long[size];
    return bitmapCopy;
  }

  public Obj copy() {
    return copy(new UnaryTable[] {this});
  }

//    public static String IntToBinaryString(int number) {
//      String binStr = "";
//      while (number != 0) {
//        binStr = (number & 1) + binStr;
//        number = number >>> 1;
//      }
//      if (binStr == "")
//        binStr = "0";
//      return binStr;
//    }
//
//    public static String IntToBinaryString(long number) {
//      String binStr = "";
//      while (number > 0) {
//        binStr = (number & 1) + binStr;
//        number = number >>> 1;
//      }
//      if (binStr == "")
//        binStr = "0";
//      return binStr;
//    }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(UnaryTable[] tables) {
    int count = 0;
    for (int i=0 ; i < tables.length ; i++)
      count += tables[i].count;
    if (count == 0)
      return EmptyRelObj.singleton;
    Obj[] objs = new Obj[count];
    int next = 0;
    for (int i=0 ; i < tables.length ; i++) {
      UnaryTable table = tables[i];
      SurrObjMapper mapper = table.mapper;
      long[] bitmap = table.bitmap;
      for (int j=0 ; j < bitmap.length ; j++) {
        long mask = bitmap[j];
        for (int k=0 ; k < 64 ; k++)
          if (((mask >>> k) & 1) != 0)
            objs[next++] = mapper.surrToObj(k + 64 * j);
      }
    }
    Miscellanea._assert(next == count);
    return Builder.createSet(objs, objs.length);
  }
}
