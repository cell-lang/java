package net.cell_lang;


class SymBinaryTable {
  public static class Iter {
    int[] entries;
    int next;
    int end;

    public Iter(int[] entries) {
      this.entries = entries;
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
      return entries[next+1];
    }

    public void next() {
      next += 2;
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
    table.check();
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

  public int count1(int surr1) {
    return table.count(surr1);
  }

  public int count2(int surr2) {
    return table.count(surr2);
  }

  public int[] lookup(int surr) {
    return table.lookup(surr);
  }

  public Iter getIter() {
    return new Iter(rawCopy());
  }

  public Iter getIter(int surr) {
    int[] col2 = table.lookup(surr);
    int count = col2.length;
    int[] entries = new int[2 * count];
    for (int i=0 ; i < count ; i++) {
      entries[2 * i] = surr;
      entries[2 * i + 1] = col2[i];
    }
    return new Iter(entries);
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
      table.delete(surr2, surr2);
    else
      eqCount--;
    check();
  }

  public Obj copy() {
    int size = size();

    if (size == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[size];
    Obj[] objs2 = new Obj[size];

    int len = table.column.length;
    int next = 0;
    for (int i=0 ; i < len ; i++) {
      int code = table.column[i];
      if (code != OverflowTable.EmptyMarker) {
        if (code >> 29 == 0) {
          if (i <= code) {
            objs1[next] = store.getValue(i);
            objs2[next++] = store.getValue(code);
          }
        }
        else {
          OverflowTable.Iter it = table.overflowTable.getIter(code);
          Obj val1 = null;
          while (!it.done()) {
            int surr2 = it.get();
            if (i <= surr2) {
              if (val1 == null)
                val1 = store.getValue(i);
              objs1[next] = val1;
              objs2[next++] = store.getValue(surr2);
            }
            it.next();
          }
        }
      }
    }
    Miscellanea._assert(next == size);

    return Builder.createBinRel(objs1, objs2, size); //## THIS COULD BE MADE MORE EFFICIENT
  }

  public int[] rawCopy() {
    return table.copySym();
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(SymBinaryTable[] tables, boolean flipped) {
    throw new RuntimeException();
  }
}
