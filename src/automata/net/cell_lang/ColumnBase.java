package net.cell_lang;


abstract class ColumnBase {
  int count = 0;

  SurrObjMapper mapper;

  protected ColumnBase(SurrObjMapper mapper) {
    this.mapper = mapper;
  }

  //////////////////////////////////////////////////////////////////////////////

  public int size() {
    return count;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(ColumnBase[] columns, boolean flipCols) {
    int totalSize = 0;
    for (int i=0 ; i < columns.length ; i++)
      totalSize += columns[i].count;

    if (totalSize == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[totalSize];
    Obj[] objs2 = new Obj[totalSize];

    int next = 0;
    for (int i=0 ; i < columns.length ; i++) {
      ColumnBase col = columns[i];
      if (col instanceof IntColumn) {
        IntColumn intCol = (IntColumn) col;
        IntColumn.Iter it = intCol.getIter();
        while (!it.done()) {
          objs1[next] = intCol.mapper.surrToObj(it.getIdx());
          objs2[next] = IntObj.get(it.getValue());
          next++;
          it.next();
        }

      }
      else if (col instanceof FloatColumn) {
        FloatColumn floatCol = (FloatColumn) col;
        FloatColumn.Iter it = floatCol.getIter();
        while (!it.done()) {
          objs1[next] = floatCol.mapper.surrToObj(it.getIdx());
          objs2[next] = new FloatObj(it.getValue());
          next++;
          it.next();
        }
      }
      else {
        ObjColumn objCol = (ObjColumn) col;
        ObjColumn.Iter it = objCol.getIter();
        while (!it.done()) {
          objs1[next] = objCol.mapper.surrToObj(it.getIdx());
          objs2[next] = it.getValue();
          next++;
          it.next();
        }
      }
    }
    Miscellanea._assert(next == totalSize);

    if (flipCols) {
      Obj[] tmp = objs1;
      objs1 = objs2;
      objs2 = tmp;
    }

    return Builder.createBinRel(objs1, objs2);
  }

  public static Obj copy(ColumnBase[] columns, MasterBinaryTable[] masters, int idx1, int idx2, int idx3) {
    int count = 0;
    for (int i=0 ; i < columns.length ; i++)
      count += columns[i].count;

    if (count == 0)
      return EmptyRelObj.singleton;

    Obj[] objs1 = new Obj[count];
    Obj[] objs2 = new Obj[count];
    Obj[] objs3 = new Obj[count];

    int next = 0;
    for (int i=0 ; i < columns.length ; i++) {
      ColumnBase column = columns[i];
      MasterBinaryTable master = masters[i];

      SurrObjMapper mapper1 = master.mapper1;
      SurrObjMapper mapper2 = master.mapper2;

      if (column instanceof IntColumn) {
        IntColumn intCol = (IntColumn) column;
        IntColumn.Iter it = intCol.getIter();
        while (!it.done()) {
          int idx = it.getIdx();
          int arg1 = master.arg1(idx);
          int arg2 = master.arg2(idx);
          objs1[next] = mapper1.surrToObj(arg1);
          objs2[next] = mapper2.surrToObj(arg2);
          objs3[next] = IntObj.get(it.getValue());
          next++;
          it.next();
        }

      }
      else if (column instanceof FloatColumn) {
        FloatColumn floatCol = (FloatColumn) column;
        FloatColumn.Iter it = floatCol.getIter();
        while (!it.done()) {
          int idx = it.getIdx();
          int arg1 = master.arg1(idx);
          int arg2 = master.arg2(idx);
          objs1[next] = mapper1.surrToObj(arg1);
          objs2[next] = mapper2.surrToObj(arg2);
          objs3[next] = new FloatObj(it.getValue());
          next++;
          it.next();
        }
      }
      else {
        ObjColumn objCol = (ObjColumn) column;
        ObjColumn.Iter it = objCol.getIter();
        while (!it.done()) {
          int idx = it.getIdx();
          int arg1 = master.arg1(idx);
          int arg2 = master.arg2(idx);
          objs1[next] = mapper1.surrToObj(arg1);
          objs2[next] = mapper2.surrToObj(arg2);
          objs3[next] = it.getValue();
          next++;
          it.next();
        }
      }
    }
    Miscellanea._assert(next == count);

    Obj[][] cols = new Obj[3][];
    cols[idx1] = objs1;
    cols[idx2] = objs2;
    cols[idx3] = objs3;

    return Builder.createTernRel(cols[0], cols[1], cols[2], count);
  }
}