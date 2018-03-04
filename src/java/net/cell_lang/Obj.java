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

  public boolean hasElem(Obj o)                         {throw new UnsupportedOperationException();}
  public boolean hasKey(Obj o)                          {throw new UnsupportedOperationException();}
  public boolean hasField(int id)                       {throw new UnsupportedOperationException();}
  public boolean hasPair(Obj o1, Obj o2)                {throw new UnsupportedOperationException();}
  public boolean hasTriple(Obj o1, Obj o2, Obj o3)      {throw new UnsupportedOperationException();}

  public int     getSymbId()                            {throw new UnsupportedOperationException();}
  public boolean getBool()                              {throw new UnsupportedOperationException();}
  public long    getLong()                              {throw new UnsupportedOperationException();}
  public double  getDouble()                            {throw new UnsupportedOperationException();}
  public int     getSize()                              {throw new UnsupportedOperationException();}
  public Obj     getItem(long i)                        {throw new UnsupportedOperationException();}
  public int     getTagId()                             {throw new UnsupportedOperationException();}
  public Obj     getTag()                               {throw new UnsupportedOperationException();}
  public Obj     getInnerObj()                          {throw new UnsupportedOperationException();}

  public SeqOrSetIter getSeqOrSetIter()                 {throw new UnsupportedOperationException();}
  public BinRelIter   getBinRelIter()                   {throw new UnsupportedOperationException();}
  public TernRelIter  getTernRelIter()                  {throw new UnsupportedOperationException();}

  // Copy-on-write update
  public Obj updatedAt(long i, Obj v)                   {throw new UnsupportedOperationException();}

  public BinRelIter getBinRelIterByCol1(Obj obj)        {throw new UnsupportedOperationException();}
  public BinRelIter getBinRelIterByCol2(Obj obj)        {throw new UnsupportedOperationException();}

  public TernRelIter getTernRelIterByCol1(Obj val)      {throw new UnsupportedOperationException();}
  public TernRelIter getTernRelIterByCol2(Obj val)      {throw new UnsupportedOperationException();}
  public TernRelIter getTernRelIterByCol3(Obj val)      {throw new UnsupportedOperationException();}

  public TernRelIter getTernRelIterByCol12(Obj val1, Obj val2)  {throw new UnsupportedOperationException();}
  public TernRelIter getTernRelIterByCol13(Obj val1, Obj val3)  {throw new UnsupportedOperationException();}
  public TernRelIter getTernRelIterByCol23(Obj val2, Obj val3)  {throw new UnsupportedOperationException();}

  public long mantissa()                                {throw new UnsupportedOperationException();}
  public long decExp()                                  {throw new UnsupportedOperationException();}

  public Obj negate()                                   {throw new UnsupportedOperationException();}
  public Obj reverse()                                  {throw new UnsupportedOperationException();}
  public void initAt(long i, Obj v)                     {throw new UnsupportedOperationException();}

  public Obj internalSort()                             {throw new UnsupportedOperationException();}
  public Obj getSlice(long first, long len)             {throw new UnsupportedOperationException();}

  public long[] getLongArray()                          {throw new UnsupportedOperationException();}
  public byte[] getByteArray()                          {throw new UnsupportedOperationException();}
  public String getString()                             {throw new UnsupportedOperationException();}

  public Obj lookup(Obj key)                            {throw new UnsupportedOperationException();}
  public Obj lookupField(int id)                        {throw new UnsupportedOperationException();}

  public Obj append(Obj obj)                            {throw new UnsupportedOperationException();}
  public Obj concat(Obj seq)                            {throw new UnsupportedOperationException();}

  public void copyItems(Obj[] items, int offset)        {throw new UnsupportedOperationException();}

  public int cmpSeq(Obj[] es, int o, int l)             {throw new UnsupportedOperationException();}
  public int cmpNeSet(Obj[] es)                         {throw new UnsupportedOperationException();}
  public int cmpNeBinRel(Obj[] c1, Obj[] c2)            {throw new UnsupportedOperationException();}
  public int cmpNeTernRel(Obj[] c1, Obj[] c2, Obj[] c3) {throw new UnsupportedOperationException();}
  public int cmpTaggedObj(int tag, Obj obj)             {throw new UnsupportedOperationException();}

  public ValueBase getValue()                           {throw new UnsupportedOperationException();}

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

  public Obj randElem() {throw new UnsupportedOperationException();}

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
        writer.close();
      }
      catch (Exception e) {

      }
    }
  }

  public Obj printed() {
    return Miscellanea.strToObj(toString());
  }
}
