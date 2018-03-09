package net.cell_lang;

import java.util.ArrayList;
import java.util.ListIterator;


class TernaryTableUpdater {
  ArrayList<TernaryTable.Tuple> deleteList = new ArrayList<TernaryTable.Tuple>();
  ArrayList<TernaryTable.Tuple> insertList = new ArrayList<TernaryTable.Tuple>();

  TernaryTable table;
  ValueStoreUpdater store1, store2, store3;

  public TernaryTableUpdater(TernaryTable table, ValueStoreUpdater store1, ValueStoreUpdater store2, ValueStoreUpdater store3) {
    this.table = table;
    this.store1 = store1;
    this.store2 = store2;
    this.store3 = store3;
  }

  public void clear() {
    deleteList.clear();
    TernaryTable.Iter it = table.getIter();
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void set(Obj value, int idx1, int idx2, int idx3) {
    Miscellanea._assert(deleteList.size() == 0 || deleteList.size() == table.count);
    Miscellanea._assert(insertList.size() == 0);

    clear();
    TernRelIter it = value.getTernRelIter();
    while (!it.done()) {
      Obj val1 = idx1 == 0 ? it.get1() : (idx1 == 1 ? it.get2() : it.get3());
      Obj val2 = idx2 == 0 ? it.get1() : (idx2 == 1 ? it.get2() : it.get3());
      Obj val3 = idx3 == 0 ? it.get1() : (idx3 == 1 ? it.get2() : it.get3());
      int surr1 = store1.lookupValueEx(val1);
      if (surr1 == -1)
        surr1 = store1.insert(val1);
      int surr2 = store2.lookupValueEx(val2);
      if (surr2 == -1)
        surr2 = store2.insert(val2);
      int surr3 = store3.lookupValueEx(val3);
      if (surr3 == -1)
        surr3 = store3.insert(val3);
      insertList.add(new TernaryTable.Tuple(surr1, surr2, surr3));
      it.next();
    }
  }

  public void insert(long value1, long value2, long value3) {
    insertList.add(new TernaryTable.Tuple(value1, value2, value3));
  }

  public void delete(long value1, long value2, long value3) {
    if (table.contains((int) value1, (int) value2, (int) value3))
      deleteList.add(new TernaryTable.Tuple(value1, value2, value3));
  }

  public void delete12(long value1, long value2) {
    TernaryTable.Iter it = table.getIter12((int) value1, (int) value2);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete13(long value1, long value3) {
    TernaryTable.Iter it = table.getIter13((int) value1, (int) value3);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete23(long value2, long value3) {
    TernaryTable.Iter it = table.getIter23((int) value2, (int) value3);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete1(long value1) {
    TernaryTable.Iter it = table.getIter1((int) value1);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete2(long value2) {
    TernaryTable.Iter it = table.getIter2((int) value2);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public void delete3(long value3) {
    TernaryTable.Iter it = table.getIter3((int) value3);
    while (!it.done()) {
      deleteList.add(it.get());
      it.next();
    }
  }

  public boolean CheckUpdates_12() {
    deleteList.sort(TernaryTableUpdater::compare123);
    insertList.sort(TernaryTableUpdater::compare123);

    int count = insertList.size();
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList.get(0);
    if (!contains12(deleteList, prev.field1OrNext, prev.field2OrEmptyMarker))
      if (table.contains12(prev.field1OrNext, prev.field2OrEmptyMarker))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList.get(i);
      if ( curr.field1OrNext == prev.field1OrNext &
           curr.field2OrEmptyMarker == prev.field2OrEmptyMarker &
           curr.field3 != prev.field3
         )
        return false;
      if (!contains12(deleteList, curr.field1OrNext, curr.field2OrEmptyMarker))
        if (table.contains12(curr.field1OrNext, curr.field2OrEmptyMarker))
          return false;
      prev = curr;
    }

    return true;
  }

  public boolean CheckUpdates_12_3() {
    if (!CheckUpdates_12())
      return false;

    deleteList.sort(TernaryTableUpdater::compare312);
    insertList.sort(TernaryTableUpdater::compare312);

    int count = insertList.size();
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList.get(0);
    if (!contains3(deleteList, prev.field3))
      if (table.contains3(prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList.get(i);
      if ( curr.field3 == prev.field3 &
           (curr.field1OrNext != prev.field1OrNext | curr.field2OrEmptyMarker != prev.field2OrEmptyMarker)
         )
        return false;
      if (!contains3(deleteList, prev.field3))
        if (table.contains3(prev.field3))
      prev = curr;
    }

    return true;
  }

  public boolean CheckUpdates_12_23() {
    if (!CheckUpdates_12())
      return false;

    deleteList.sort(TernaryTableUpdater::compare231);
    insertList.sort(TernaryTableUpdater::compare231);

    int count = insertList.size();
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList.get(0);
    if (!contains23(deleteList, prev.field2OrEmptyMarker, prev.field3))
      if (table.contains23(prev.field2OrEmptyMarker, prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList.get(i);
      if ( curr.field2OrEmptyMarker == prev.field2OrEmptyMarker &
           curr.field3 == prev.field3 &
           curr.field1OrNext != prev.field1OrNext
         )
        return false;
      if (!contains23(deleteList, curr.field2OrEmptyMarker, curr.field3))
        if (table.contains23(curr.field2OrEmptyMarker, curr.field3))
          return false;
      prev = curr;
    }

    return true;
  }

  public boolean CheckUpdates_12_23_31() {
    if (!CheckUpdates_12_23())
      return false;

    deleteList.sort(TernaryTableUpdater::compare312);
    insertList.sort(TernaryTableUpdater::compare312);

    int count = insertList.size();
    if (count == 0)
      return true;

    TernaryTable.Tuple prev = insertList.get(0);
    if (!contains31(deleteList, prev.field3, prev.field1OrNext))
      if (table.contains13(prev.field1OrNext, prev.field3))
        return false;

    for (int i=1 ; i < count ; i++) {
      TernaryTable.Tuple curr = insertList.get(i);
      if ( curr.field3 == prev.field3 &
           curr.field1OrNext == prev.field1OrNext &
           curr.field2OrEmptyMarker != prev.field2OrEmptyMarker
         )
        return false;
      if (!contains31(deleteList, curr.field3, curr.field1OrNext))
        if (table.contains13(curr.field1OrNext, curr.field3))
          return false;
      prev = curr;
    }

    return true;
  }

  public void apply() {
    for (int i=0 ; i < deleteList.size() ; i++) {
      TernaryTable.Tuple tuple = deleteList.get(i);
      if (table.contains(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3))
        table.delete(tuple.field1OrNext, tuple.field2OrEmptyMarker, tuple.field3);
      else
        deleteList.set(i, new TernaryTable.Tuple(0xFFFFFFFF, 0xFFFFFFFF, 0xFFFFFFFF));
    }

    ListIterator<TernaryTable.Tuple> it = insertList.listIterator();
    while (it.hasNext()) {
      TernaryTable.Tuple curr = it.next();
      if (!table.contains(curr.field1OrNext, curr.field2OrEmptyMarker, curr.field3)) {
        table.insert(curr.field1OrNext, curr.field2OrEmptyMarker, curr.field3);
        table.store1.addRef(curr.field1OrNext);
        table.store2.addRef(curr.field2OrEmptyMarker);
        table.store3.addRef(curr.field3);
      }
    }
  }

  public void finish() {
    ListIterator<TernaryTable.Tuple> it = deleteList.listIterator();
    while (it.hasNext()) {
      TernaryTable.Tuple tuple = it.next();
      if (tuple.field1OrNext != 0xFFFFFFFF) {
        table.store1.release(tuple.field1OrNext);
        table.store2.release(tuple.field2OrEmptyMarker);
        table.store3.release(tuple.field3);
      }
    }
  }

  public void reset() {
    deleteList.clear();
    insertList.clear();
  }


  static boolean contains12(ArrayList<TernaryTable.Tuple> tuples, int field1, int field2) {
    int low = 0;
    int high = tuples.size() - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      TernaryTable.Tuple tuple = tuples.get(mid);
      if (tuple.field1OrNext > field1)
        high = mid - 1;
      else if (tuple.field1OrNext < field1)
        low = mid + 1;
      else if (tuple.field2OrEmptyMarker > field2)
        high = mid - 1;
      else if (tuple.field2OrEmptyMarker < field2)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static boolean contains23(ArrayList<TernaryTable.Tuple> tuples, int field2, int field3) {
    int low = 0;
    int high = tuples.size() - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      TernaryTable.Tuple tuple = tuples.get(mid);
      if (tuple.field2OrEmptyMarker > field2)
        high = mid - 1;
      else if (tuple.field2OrEmptyMarker < field2)
        low = mid + 1;
      else if (tuple.field3 > field3)
        high = mid - 1;
      else if (tuple.field3 < field3)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static boolean contains31(ArrayList<TernaryTable.Tuple> tuples, int field3, int field1) {
    int low = 0;
    int high = tuples.size() - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      TernaryTable.Tuple tuple = tuples.get(mid);
      if (tuple.field3 > field3)
        high = mid - 1;
      else if (tuple.field3 < field3)
        low = mid + 1;
      else if (tuple.field1OrNext > field1)
        high = mid - 1;
      else if (tuple.field1OrNext < field1)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static boolean contains3(ArrayList<TernaryTable.Tuple> tuples, int field3) {
    int low = 0;
    int high = tuples.size() - 1;

    while (low <= high) {
      int mid = (int) (((long) low + (long) high) / 2);
      int midField3 = tuples.get(mid).field3;
      if (midField3 > field3)
        high = mid - 1;
      else if (midField3 < field3)
        low = mid + 1;
      else
        return true;
    }

    return false;
  }

  static int compare123(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
    if (t1.field1OrNext != t2.field1OrNext)
      return t1.field1OrNext - t2.field1OrNext;
    else if (t1.field2OrEmptyMarker != t2.field2OrEmptyMarker)
      return t1.field2OrEmptyMarker - t2.field2OrEmptyMarker;
    else
      return t1.field3 - t2.field3;
  }

  static int compare231(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
    if (t1.field2OrEmptyMarker != t2.field2OrEmptyMarker)
      return t1.field2OrEmptyMarker - t2.field2OrEmptyMarker;
    else if (t1.field3 != t2.field3)
      return t1.field3 - t2.field3;
    else
      return t1.field1OrNext - t2.field1OrNext;
  }

  static int compare312(TernaryTable.Tuple t1, TernaryTable.Tuple t2) {
    if (t1.field3 != t2.field3)
      return t1.field3 - t2.field3;
    if (t1.field1OrNext != t2.field1OrNext)
      return t1.field1OrNext - t2.field1OrNext;
    else
      return t1.field2OrEmptyMarker - t2.field2OrEmptyMarker;
  }
}
