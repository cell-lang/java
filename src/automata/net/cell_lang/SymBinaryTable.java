package net.cell_lang;


class SymBinaryTable {
  public static class Iter {
    int[] entries;
    int next;
    int end;
    boolean singleCol;

    public Iter(int[] entries, boolean singleCol) {
      this.entries = entries;
      this.singleCol = singleCol;
      next = 0;
      end = entries.length;
    }

    public boolean done() {
      return next >= end;
    }

    public int get1() {
      return entries[next];
    }

    public int get2() {
      Miscellanea._assert(!singleCol);
      return entries[next+1];
    }

    public void next() {
      next += singleCol ? 1 : 2;
    }
  }


  OneWayBinTable table = new OneWayBinTable();
  public SurrObjMapper mapper;

  int eqCount = 0;


  public SymBinaryTable(SurrObjMapper mapper) {
    this.mapper = mapper;
    check();
  }

  public void check() {
    // table.check();
  }

  public int size() {
    return (table.count + eqCount) / 2;
  }

  public boolean contains(int surr1, int surr2) {
    return table.contains(surr1, surr2);
  }

  public boolean contains(int surr) {
    return table.containsKey(surr);
  }

  public int count(int surr12) {
    return table.count(surr12);
  }

  public int[] restrict(int surr) {
    return table.restrict(surr);
  }

  public int lookup(int surr) {
    return table.lookup(surr);
  }

  public Iter getIter() {
    return new Iter(rawCopy(), false);
  }

  public Iter getIter(int surr) {
    return new Iter(table.restrict(surr), true);
  }

  public void insert(int surr1, int surr2) {
    table.insert(surr1, surr2);
    if (surr1 != surr2)
      table.insert(surr2, surr1);
    else
      eqCount++;
    check();
  }

  public void clear() {
    table = new OneWayBinTable();
    eqCount = 0;
    check();
  }

  public void delete(int surr1, int surr2) {
    table.delete(surr1, surr2);
    if (surr1 != surr2)
      table.delete(surr2, surr1);
    else
      eqCount--;
    check();
  }

  public Obj copy() {
    return copy(new SymBinaryTable[] {this});
  }

  public int[] rawCopy() {
    return table.copySym(eqCount);
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(SymBinaryTable[] tables) {
    int size = 0;
    for (int i=0 ; i < tables.length ; i++)
      size += tables[i].size();

    if (size == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[size];
    Obj[] objs2 = new Obj[size];

    int[] buffer = new int[32];

    int next = 0;

    for (int iT=0 ; iT < tables.length ; iT++) {
      SymBinaryTable table = tables[iT];
      OneWayBinTable oneWayTable = table.table;
      SurrObjMapper mapper = table.mapper;

      int len = oneWayTable.column.length;
      for (int iS=0 ; iS < len ; iS++) {
        int count1 = oneWayTable.count(iS);
        if (count1 != 0) {
          if (count1 > buffer.length)
            buffer = new int[Array.capacity(buffer.length, count1)];
          Obj obj1 = mapper.surrToObj(iS);
          int _count1 = oneWayTable.restrict(iS, buffer);
          Miscellanea._assert(_count1 == count1);
          for (int i=0 ; i < count1 ; i++) {
            int surr2 = buffer[i];
            if (iS <= surr2) {
              objs1[next] = obj1;
              objs2[next++] = mapper.surrToObj(surr2);
            }
          }
        }
      }
    }
    Miscellanea._assert(next == size);

    return Builder.createBinRel(objs1, objs2, size); //## THIS COULD BE MADE MORE EFFICIENT
  }
}
