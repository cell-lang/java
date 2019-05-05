package net.cell_lang;

import java.util.Arrays;
import java.io.Writer;


final class NeSetObj extends Obj {
  Obj[] elts;
  int[] hashcodes;
  int minPrintedSize = -1;

  public NeSetObj(Obj[] elts, int[] hashcodes) {
    Miscellanea._assert(elts.length > 0);

    data = setObjData(elts.length);
    extraData = neSetObjExtraData();
    this.elts = elts;
    this.hashcodes = hashcodes;
  }

  public Obj insert(Obj obj) {
    if (!hasElem(obj))
      return this;

    NeTreeSetObj treeSet = new NeTreeSetObj(elts, hashcodes, 0, elts.length);
    return treeSet.insert(obj);
  }

  public Obj remove(Obj obj) {
    if (!hasElem(obj))
      return this;

    NeTreeSetObj treeSet = new NeTreeSetObj(elts, hashcodes, 0, elts.length);
    return treeSet.remove(obj);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean hasElem(Obj obj) {
    int hashcode = obj.hashcode();
    int idx = Arrays.binarySearch(hashcodes, hashcode);
    if (idx >= 0) {
      for (int i=idx ; i < elts.length && hashcodes[i] == hashcode ; i++)
        if (elts[i].isEq(obj))
          return true;
      for (int i=idx-1 ; i >= 0 && hashcodes[i] == hashcode ; i--)
        if (elts[i].isEq(obj))
          return true;
    }
    return false;
  }

  public SetIter getSetIter() {
    return new SetIter(elts, 0, elts.length-1);
  }

  public Obj[] getArray(Obj[] buffer) {
    return elts;
  }

  public SeqObj internalSort() {
    Obj[] sortedElts = Arrays.copyOf(elts, elts.length);
    Arrays.sort(sortedElts, (a, b) -> Canonical.order(a, b));
    return ArrayObjs.create(sortedElts);
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

  // public int hashcode() {
  //   if (hashcode == Integer.MIN_VALUE) {
  //     int len = getSize();
  //     long hashcode64 = 0;
  //     for (int i=0 ; i < len ; i++)
  //       hashcode64 = 31 * hashcode64 + getLongAt(i);
  //       // hashcode64 += getLongAt(i);
  //     hashcode = (int) (hashcode64 ^ (hashcode64 >> 32));
  //   }
  //   return hashcode;
  // }

  public TypeCode getTypeCode() {
    return TypeCode.NE_SET;
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
