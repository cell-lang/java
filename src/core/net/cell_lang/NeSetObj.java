package net.cell_lang;

import java.io.Writer;



class NeSetObj extends Obj {
  Obj[] elts;
  int minPrintedSize = -1;

  public NeSetObj(Obj[] elts) {
    Miscellanea._assert(elts.length > 0);
    this.elts = elts;
  }

  public boolean isSet() {
    return true;
  }

  public boolean isNeSet() {
    return true;
  }

  public boolean hasElem(Obj obj) {
    return Algs.binSearch(elts, obj) != -1;
  }

  public int getSize() {
    return elts.length;
  }

  public SetIter getSetIter() {
    return new SetIter(elts, 0, elts.length-1);
  }

  public Obj internalSort() {
    return new MasterSeqObj(elts);
  }

  public int hashCode() {
    int hashcodesSum = 0;
    for (int i=0 ; i < elts.length ; i++)
      hashcodesSum += elts[i].hashCode();
    return hashcodesSum ^ (int) elts.length;
  }

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

  protected int typeId() {
    return 5;
  }

  protected int internalCmp(Obj other) {
    return other.cmpNeSet(elts);
  }

  public int cmpNeSet(Obj[] otherElts) {
    int len = elts.length;
    int otherLen = otherElts.length;
    if (otherLen != len)
      return otherLen < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = otherElts[i].cmp(elts[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public Obj randElem() {
    return elts[0];
  }
}
