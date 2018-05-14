package net.cell_lang;

import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;


abstract class Obj implements Comparable<Obj> {
  public boolean isBlankObj()                           {return false;}
  public boolean isNullObj()                            {return false;}
  public boolean isSymb()                               {return false;}
  // public boolean IsBool()                               {return false;}
  public boolean isInt()                                {return false;}
  public boolean isFloat()                              {return false;}
  public boolean isSeq()                                {return false;}
  public boolean isEmptySeq()                           {return false;}
  public boolean isNeSeq()                              {return false;}
  public boolean isEmptyRel()                           {return false;}
  public boolean isSet()                                {return false;}
  public boolean isNeSet()                              {return false;}
  public boolean isBinRel()                             {return false;}
  public boolean isNeBinRel()                           {return false;}
  public boolean isNeMap()                              {return false;}
  public boolean isNeRecord()                           {return false;}
  public boolean isTernRel()                            {return false;}
  public boolean isNeTernRel()                          {return false;}
  public boolean isTagged()                             {return false;}
  public boolean isSyntacticSugaredString()             {return false;}

  public boolean isSymb(int id)                         {return false;}
  public boolean isInt(long n)                          {return false;}
  public boolean isFloat(double x)                      {return false;}

  public boolean hasElem(Obj o)                         {throw Miscellanea.internalFail(this);}
  public boolean hasKey(Obj o)                          {throw Miscellanea.internalFail(this);}
  public boolean hasField(int id)                       {throw Miscellanea.internalFail(this);}
  public boolean hasPair(Obj o1, Obj o2)                {throw Miscellanea.internalFail(this);}
  public boolean hasTriple(Obj o1, Obj o2, Obj o3)      {throw Miscellanea.internalFail(this);}

  public int     getSymbId()                            {throw Miscellanea.internalFail(this);}
  public boolean getBool()                              {throw Miscellanea.internalFail(this);}
  public long    getLong()                              {throw Miscellanea.internalFail(this);}
  public double  getDouble()                            {throw Miscellanea.internalFail(this);}
  public int     getSize()                              {throw Miscellanea.internalFail(this);}
  public Obj     getItem(long i)                        {throw Miscellanea.internalFail(this);}
  public int     getTagId()                             {throw Miscellanea.internalFail(this);}
  public Obj     getTag()                               {throw Miscellanea.internalFail(this);}
  public Obj     getInnerObj()                          {throw Miscellanea.internalFail(this);}

  public SeqOrSetIter getSeqOrSetIter()                 {throw Miscellanea.internalFail(this);}
  public BinRelIter   getBinRelIter()                   {throw Miscellanea.internalFail(this);}
  public TernRelIter  getTernRelIter()                  {throw Miscellanea.internalFail(this);}

  // Copy-on-write update
  public Obj updatedAt(long i, Obj v)                   {throw Miscellanea.internalFail(this);}

  public BinRelIter getBinRelIterByCol1(Obj obj)        {throw Miscellanea.internalFail(this);}
  public BinRelIter getBinRelIterByCol2(Obj obj)        {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIterByCol1(Obj val)      {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol2(Obj val)      {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol3(Obj val)      {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIterByCol12(Obj val1, Obj val2)  {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol13(Obj val1, Obj val3)  {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol23(Obj val2, Obj val3)  {throw Miscellanea.internalFail(this);}

  public long mantissa()                                {throw Miscellanea.internalFail(this);}
  public long decExp()                                  {throw Miscellanea.internalFail(this);}

  //## REMOVE REMOVE REMOVE
  public Obj negate()                                   {throw Miscellanea.internalFail(this);}
  public Obj reverse()                                  {throw Miscellanea.internalFail(this);}
  public void initAt(long i, Obj v)                     {throw Miscellanea.internalFail(this);}

  public Obj internalSort()                             {throw Miscellanea.internalFail(this);}
  public Obj getSlice(long first, long len)             {throw Miscellanea.internalFail(this);}

  public long[] getLongArray()                          {throw Miscellanea.internalFail(this);}
  public byte[] getByteArray()                          {throw Miscellanea.internalFail(this);}
  public String getString()                             {throw Miscellanea.internalFail(this);}

  public Obj lookup(Obj key)                            {throw Miscellanea.internalFail(this);}
  public Obj lookupField(int id)                        {throw Miscellanea.internalFail(this);}

  public Obj append(Obj obj)                            {throw Miscellanea.internalFail(this);}
  public Obj concat(Obj seq)                            {throw Miscellanea.internalFail(this);}

  public void copyItems(Obj[] items, int offset)        {throw Miscellanea.internalFail(this);}

  public int cmpSeq(Obj[] es, int o, int l)             {throw Miscellanea.internalFail(this);}
  public int cmpNeSet(Obj[] es)                         {throw Miscellanea.internalFail(this);}
  public int cmpNeBinRel(Obj[] c1, Obj[] c2)            {throw Miscellanea.internalFail(this);}
  public int cmpNeTernRel(Obj[] c1, Obj[] c2, Obj[] c3) {throw Miscellanea.internalFail(this);}
  public int cmpTaggedObj(int tag, Obj obj)             {throw Miscellanea.internalFail(this);}
  public int cmpRecord(int[] ls, Obj[] vs)              {throw Miscellanea.internalFail(this);}

  public ValueBase getValue()                           {throw Miscellanea.internalFail(this);}

  public void dump() {
    System.out.println("ERROR: NO DUMP AVAILABLE");
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public abstract int hashCode();
  public abstract void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel);
  public abstract int minPrintedSize();

  protected abstract int typeId();
  protected abstract int internalCmp(Obj o);

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public Obj randElem() {throw Miscellanea.internalFail(this);}

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public boolean getBoolAt(long idx) {
    return getItem(idx).getBool();
  }

  public long getLongAt(long idx) {
    return getItem(idx).getLong();
  }

  public double getDoubleAt(long idx) {
    return getItem(idx).getDouble();
  }

  public boolean[] getArray(boolean[] buffer) {
    for (int i=0 ; i < buffer.length ; i++)
      buffer[i] = getBoolAt(i);
    return buffer;
  }

  public long[] getArray(long[] buffer) {
    for (int i=0 ; i < buffer.length ; i++)
      buffer[i] = getLongAt(i);
    return buffer;
  }

  public double[] getArray(double[] buffer) {
    for (int i=0 ; i < buffer.length ; i++)
      buffer[i] = getDoubleAt(i);
    return buffer;
  }

  public Obj[] getArray(Obj[] buffer) {
    for (int i=0 ; i < buffer.length ; i++)
      buffer[i] = getItem(i);
    return buffer;
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public boolean isEq(Obj o) {
    return cmp(o) == 0;
  }

  public int compareTo(Obj other) {
    return -cmp(other);
  }

  public int cmp(Obj o) {
    if (this == o)
      return 0;
    int id1 = typeId();
    int id2 = o.typeId();
    if (id1 == id2)
      return internalCmp(o);
    return id1 < id2 ? 1 : -1;
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    print(writer, 90, true, 0);
    return writer.toString();
  }

  public void print() {
    Writer writer = new OutputStreamWriter(System.out);
    try {
      print(writer, 90, true, 0);
      writer.write('\n');
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }
    finally {
      try {
        writer.flush();
      }
      catch (Exception e) {

      }
    }
  }

  public Obj printed() {
    return Miscellanea.strToObj(toString());
  }
}
