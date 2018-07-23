package net.cell_lang;

import java.io.Writer;


final class NeSetObj extends Obj {
  Obj[] elts;
  int minPrintedSize = -1;

  public NeSetObj(Obj[] elts) {
    Miscellanea._assert(elts.length > 0);

    int size = elts.length;
    long hashcode = 0;
    for (int i=0 ; i < elts.length ; i++)
      hashcode += elts[i].data;
    data = setObjData(size, hashcode);
    extraData = neSetObjExtraData();
    this.elts = elts;
  }

  public boolean hasElem(Obj obj) {
    return Algs.binSearch(elts, obj) != -1;
  }

  public SetIter getSetIter() {
    return new SetIter(elts, 0, elts.length-1);
  }

  public Obj[] getArray(Obj[] buffer) {
    return elts;
  }

  public SeqObj internalSort() {
    return ArrayObjs.create(elts);
  }

  public Obj randElem() {
    return elts[0];
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    Miscellanea._assert(getSize() == other.getSize());

    NeSetObj otherSet = (NeSetObj) other;
    int size = getSize();
    Obj[] otherElts = otherSet.elts;
    for (int i=0 ; i < size ; i++) {
      int ord = elts[i].quickOrder(otherElts[i]);
      if (ord != 0)
        return ord;
    }
    return 0;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = elts.length;
      boolean breakLine = minPrintedSize() > maxLineLen;

      writer.write('[');

      if (breakLine) {
        // If we are on a fresh line, we start writing the first element
        // after the opening bracket, with just a space in between
        // Otherwise we start on the next line
        if (newLine)
          writer.write(' ');
        else
          Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < len ; i++) {
        if (i > 0) {
          writer.write(',');
          if (breakLine)
            Miscellanea.writeIndentedNewLine(writer, indentLevel + 1);
          else
            writer.write(' ');
        }
        elts[i].print(writer, maxLineLen, breakLine & !newLine, indentLevel + 1);
      }

      if (breakLine)
        Miscellanea.writeIndentedNewLine(writer, indentLevel);

      writer.write(']');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int minPrintedSize() {
    if (minPrintedSize == -1) {
      int len = elts.length;
      minPrintedSize = 2 * len;
      for (int i=0 ; i < len ; i++)
        minPrintedSize += elts[i].minPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase getValue() {
    int size = elts.length;
    ValueBase[] values = new ValueBase[size];
    for (int i=0 ; i < size ; i++)
      values[i] = elts[i].getValue();
    return new NeSetValue(values);
  }
}
