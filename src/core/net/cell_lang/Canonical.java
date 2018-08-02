package net.cell_lang;


class Canonical {
  public static int order(Obj obj1, Obj obj2) {
    Obj.TypeCode code1 = obj1.getTypeCode();
    Obj.TypeCode code2 = obj2.getTypeCode();

    if (code1 != code2)
      return code1.ordinal() < code2.ordinal() ? -1 : 1;

    switch (code1) {
      case SYMBOL:
        return symbolOrder((SymbObj) obj1, (SymbObj) obj2);

      case INTEGER:
        return integerOrder(obj1, obj2);

      case FLOAT:
        return floatOrder(obj1, obj2);

      case EMPTY_SEQ:
        return 0;

      case EMPTY_REL:
        return 0;

      case NE_SEQ:
        return seqOrder(obj1, obj2);

      case NE_SET:
        return setOrder((NeSetObj) obj1, (NeSetObj) obj2);

      case NE_BIN_REL:
        return binRelOrder((NeBinRelObj) obj1, (NeBinRelObj) obj2);

      case NE_TERN_REL:
        return ternRelOrder((NeTernRelObj) obj1, (NeTernRelObj) obj2);

      case TAGGED_VALUE:
        return taggedValueOrder(obj1, obj2);
    }

    throw Miscellanea.internalFail();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  static int order(Obj[] objs1, Obj[] objs2) {
    int len = objs1.length;
    for (int i=0 ; i < len ; i++) {
      int ord = order(objs1[i], objs2[i]);
      if (ord != 0)
        return ord;
    }
    return 0;
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  static int symbolOrder(SymbObj obj1, SymbObj obj2) {
    String str1 = obj1.string;
    String str2 = obj2.string;
    int len1 = str1.length();
    int len2 = str2.length();

    if (len1 != len2)
      return len1 < len2 ? -1 : 1;

    for (int i=0 ; i < len1 ; i++) {
      char ch1 = str1.charAt(i);
      char ch2 = str2.charAt(i);
      if (ch1 != ch2)
        return ch1 < ch2 ? -1 : 1;
    }

    return 0;
  }

  static int integerOrder(Obj obj1, Obj obj2) {
    long value1 = obj1.getLong();
    long value2 = obj2.getLong();
    return value1 != value2 ? (value1 < value2 ? -1 : 1) : 0;
  }

  static int floatOrder(Obj obj1, Obj obj2) {
    double value1 = obj1.getDouble();
    double value2 = obj2.getDouble();
    return value1 != value2 ? (value1 < value2 ? -1 : 1) : 0;
  }

  static int seqOrder(Obj obj1, Obj obj2) {
    int len1 = obj1.getSize();
    int len2 = obj2.getSize();

    if (len1 != len2)
      return len1 < len2 ? -1 : 1;

    for (int i=0 ; i < len1 ; i++) {
      int ord = order(obj1.getObjAt(i), obj2.getObjAt(i));
      if (ord != 0)
        return ord;
    }

    return 0;
  }

  static int setOrder(NeSetObj obj1, NeSetObj obj2) {
    int size1 = obj1.getSize();
    int size2 = obj2.getSize();

    if (size1 != size2)
      return size1 < size2 ? -1 : 1;
    else
      return order(obj1.elts, obj2.elts);
  }

  static int binRelOrder(NeBinRelObj obj1, NeBinRelObj obj2) {
    int size1 = obj1.getSize();
    int size2 = obj2.getSize();

    if (size1 != size2)
      return size1 < size2 ? -1 : 1;

    int ord = order(obj1.getCol1(), obj2.getCol1());
    if (ord == 0)
      ord = order(obj1.col2, obj2.col2);
    return ord;
  }

  static int ternRelOrder(NeTernRelObj obj1, NeTernRelObj obj2) {
    int size1 = obj1.getSize();
    int size2 = obj2.getSize();

    if (size1 != size2)
      return size1 < size2 ? -1 : 1;

    int ord = order(obj1.col1, obj2.col1);
    if (ord != 0)
      return ord;

    ord = order(obj1.col2, obj2.col2);
    if (ord != 0)
      return ord;

    return order(obj1.col3, obj2.col3);
  }

  static int taggedValueOrder(Obj obj1, Obj obj2) {
    int ord = order(obj1.getTag(), obj2.getTag());
    if (ord != 0)
      return ord;
    return order(obj1.getInnerObj(), obj2.getInnerObj());
  }
}
