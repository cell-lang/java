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
  public ValueStore store;

  int eqCount = 0;


  public SymBinaryTable(ValueStore store) {
    this.store = store;
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

    int next = 0;
    for (int iT=0 ; iT < tables.length ; iT++) {
      SymBinaryTable table = tables[iT];
      int[] column = table.table.column;
      ValueStore store = table.store;
      for (int iS=0 ; iS < column.length ; iS++) {
        int code = table.table.column[iS];
        if (code != OverflowTable.EmptyMarker) {
          if (code >> 29 == 0) {
            if (iS <= code) {
              objs1[next] = store.getValue(iS);
              objs2[next++] = store.getValue(code);
            }
          }
          else {
            OverflowTable.Iter it = table.table.overflowTable.getIter(code);
            Obj val1 = null;
            while (!it.done()) {
              int arg2 = it.get();
              if (iS <= arg2) {
                if (val1 == null)
                  val1 = store.getValue(iS);
                objs1[next] = val1;
                objs2[next++] = store.getValue(arg2);
              }
              it.next();
            }
          }
        }
      }
    }
    Miscellanea._assert(next == size);

    return Builder.createBinRel(objs1, objs2, size); //## THIS COULD BE MADE MORE EFFICIENT
  }
}
