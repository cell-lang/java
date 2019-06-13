package net.cell_lang;


abstract class ColumnBase {
  int count = 0;

  SurrObjMapper mapper;


  protected ColumnBase(SurrObjMapper mapper) {
    this.mapper = mapper;
  }

  //////////////////////////////////////////////////////////////////////////////

  public static Obj copy(ColumnBase[] columns) {
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

    return Builder.createBinRel(objs1, objs2);
  }
}