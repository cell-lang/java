package net.cell_lang;

import java.io.Writer;



class NeSetObj extends Obj {
  Obj[] elts;
  int minPrintedSize = -1;

  public NeSetObj(Obj[] elts) {
    Miscellanea.Assert(elts.length > 0);
    this.elts = elts;
  }

  public boolean IsSet() {
    return true;
  }

  public boolean IsNeSet() {
    return true;
  }

  public boolean HasElem(Obj obj) {
    return Algs.BinSearch(elts, obj) != -1;
  }

  public int GetSize() {
    return elts.length;
  }

  public SeqOrSetIter GetSeqOrSetIter() {
    return new SeqOrSetIter(elts, 0, elts.length-1);
  }

  public Obj InternalSort() {
    return new MasterSeqObj(elts);
  }

  public int hashCode() {
    int hashcodesSum = 0;
    for (int i=0 ; i < elts.length ; i++)
      hashcodesSum += elts[i].hashCode();
    return hashcodesSum ^ (int) elts.length;
  }

  public void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    try {
      int len = elts.length;
      boolean breakLine = MinPrintedSize() > maxLineLen;

      writer.write('[');

      if (breakLine) {
        // If we are on a fresh line, we start writing the first element
        // after the opening bracket, with just a space in between
        // Otherwise we start on the next line
        if (newLine)
          writer.write(' ');
        else
          Miscellanea.WriteIndentedNewLine(writer, indentLevel + 1);
      }

      for (int i=0 ; i < len ; i++) {
        if (i > 0) {
          writer.write(',');
          if (breakLine)
            Miscellanea.WriteIndentedNewLine(writer, indentLevel + 1);
          else
            writer.write(' ');
        }
        elts[i].Print(writer, maxLineLen, breakLine & !newLine, indentLevel + 1);
      }

      if (breakLine)
        Miscellanea.WriteIndentedNewLine(writer, indentLevel);

      writer.write(']');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public int MinPrintedSize() {
    if (minPrintedSize == -1) {
      int len = elts.length;
      minPrintedSize = 2 * len;
      for (int i=0 ; i < len ; i++)
        minPrintedSize += elts[i].MinPrintedSize();
    }
    return minPrintedSize;
  }

  public ValueBase GetValue() {
    int size = elts.length;
    ValueBase[] values = new ValueBase[size];
    for (int i=0 ; i < size ; i++)
      values[i] = elts[i].GetValue();
    return new NeSetValue(values);
  }

  protected int TypeId() {
    return 5;
  }

  protected int InternalCmp(Obj other) {
    return other.CmpNeSet(elts);
  }

  public int CmpNeSet(Obj[] other_elts) {
    int len = elts.length;
    int other_len = other_elts.length;
    if (other_len != len)
      return other_len < len ? 1 : -1;
    for (int i=0 ; i < len ; i++) {
      int res = other_elts[i].Cmp(elts[i]);
      if (res != 0)
        return res;
    }
    return 0;
  }

  public Obj RandElem() {
    return elts[0];
  }
}
