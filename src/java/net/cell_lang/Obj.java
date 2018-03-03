package net.cell_lang;

import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;


abstract class Obj implements Comparable<Obj> {
  public boolean IsBlankObj()                              {return false;}
  public boolean IsNullObj()                               {return false;}
  public boolean IsSymb()                                  {return false;}
  // public boolean IsBool()                                  {return false;}
  public boolean IsInt()                                   {return false;}
  public boolean IsFloat()                                 {return false;}
  public boolean IsSeq()                                   {return false;}
  public boolean IsEmptySeq()                              {return false;}
  public boolean IsNeSeq()                                 {return false;}
  public boolean IsEmptyRel()                              {return false;}
  public boolean IsSet()                                   {return false;}
  public boolean IsNeSet()                                 {return false;}
  public boolean IsBinRel()                                {return false;}
  public boolean IsNeBinRel()                              {return false;}
  public boolean IsNeMap()                                 {return false;}
  public boolean IsNeRecord()                              {return false;}
  public boolean IsTernRel()                               {return false;}
  public boolean IsNeTernRel()                             {return false;}
  public boolean IsTagged()                                {return false;}
  public boolean IsSyntacticSugaredString()                {return false;}

  public boolean IsSymb(int id)                            {return false;}
  public boolean IsInt(long n)                             {return false;}
  public boolean IsFloat(double x)                         {return false;}

  public boolean HasElem(Obj o)                            {throw new UnsupportedOperationException();}
  public boolean HasKey(Obj o)                             {throw new UnsupportedOperationException();}
  public boolean HasField(int id)                          {throw new UnsupportedOperationException();}
  public boolean HasPair(Obj o1, Obj o2)                   {throw new UnsupportedOperationException();}
  public boolean HasTriple(Obj o1, Obj o2, Obj o3)         {throw new UnsupportedOperationException();}

  public int    GetSymbId()                             {throw new UnsupportedOperationException();}
  public boolean   GetBool()                               {throw new UnsupportedOperationException();}
  public long   GetLong()                               {throw new UnsupportedOperationException();}
  public double GetDouble()                             {throw new UnsupportedOperationException();}
  public int    GetSize()                               {throw new UnsupportedOperationException();}
  public Obj    GetItem(long i)                         {throw new UnsupportedOperationException();}
  public int    GetTagId()                              {throw new UnsupportedOperationException();}
  public Obj    GetTag()                                {throw new UnsupportedOperationException();}
  public Obj    GetInnerObj()                           {throw new UnsupportedOperationException();}

  public SeqOrSetIter GetSeqOrSetIter()                 {throw new UnsupportedOperationException();}
  public BinRelIter   GetBinRelIter()                   {throw new UnsupportedOperationException();}
  public TernRelIter  GetTernRelIter()                  {throw new UnsupportedOperationException();}

  // Copy-on-write update
  public Obj UpdatedAt(long i, Obj v)                   {throw new UnsupportedOperationException();}

  public BinRelIter GetBinRelIterByCol1(Obj obj)        {throw new UnsupportedOperationException();}
  public BinRelIter GetBinRelIterByCol2(Obj obj)        {throw new UnsupportedOperationException();}

  public TernRelIter GetTernRelIterByCol1(Obj val)      {throw new UnsupportedOperationException();}
  public TernRelIter GetTernRelIterByCol2(Obj val)      {throw new UnsupportedOperationException();}
  public TernRelIter GetTernRelIterByCol3(Obj val)      {throw new UnsupportedOperationException();}

  public TernRelIter GetTernRelIterByCol12(Obj val1, Obj val2)  {throw new UnsupportedOperationException();}
  public TernRelIter GetTernRelIterByCol13(Obj val1, Obj val3)  {throw new UnsupportedOperationException();}
  public TernRelIter GetTernRelIterByCol23(Obj val2, Obj val3)  {throw new UnsupportedOperationException();}

  public long Mantissa()                                {throw new UnsupportedOperationException();}
  public long DecExp()                                  {throw new UnsupportedOperationException();}

  public Obj Negate()                                   {throw new UnsupportedOperationException();}
  public Obj Reverse()                                  {throw new UnsupportedOperationException();}
  public void InitAt(long i, Obj v)                     {throw new UnsupportedOperationException();}

  public Obj InternalSort()                             {throw new UnsupportedOperationException();}
  public Obj GetSlice(long first, long len)             {throw new UnsupportedOperationException();}

  public long[] GetLongArray()                          {throw new UnsupportedOperationException();}
  public byte[] GetByteArray()                          {throw new UnsupportedOperationException();}
  public String GetString()                             {throw new UnsupportedOperationException();}

  public Obj Lookup(Obj key)                            {throw new UnsupportedOperationException();}
  public Obj LookupField(int id)                        {throw new UnsupportedOperationException();}

  public Obj Append(Obj obj)                            {throw new UnsupportedOperationException();}
  public Obj Concat(Obj seq)                            {throw new UnsupportedOperationException();}

  public void CopyItems(Obj[] items, int offset)        {throw new UnsupportedOperationException();}

  public int CmpSeq(Obj[] es, int o, int l)             {throw new UnsupportedOperationException();}
  public int CmpNeSet(Obj[] es)                         {throw new UnsupportedOperationException();}
  public int CmpNeBinRel(Obj[] c1, Obj[] c2)            {throw new UnsupportedOperationException();}
  public int CmpNeTernRel(Obj[] c1, Obj[] c2, Obj[] c3) {throw new UnsupportedOperationException();}
  public int CmpTaggedObj(int tag, Obj obj)             {throw new UnsupportedOperationException();}

  public ValueBase GetValue()                           {throw new UnsupportedOperationException();}

  public void Dump() {
    System.out.println("ERROR: NO DUMP AVAILABLE");
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public abstract int hashCode();
  public abstract void Print(Writer writer, int maxLineLen, boolean newLine, int indentLevel);
  public abstract int MinPrintedSize();

  protected abstract int TypeId();
  protected abstract int InternalCmp(Obj o);

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public Obj RandElem() {throw new UnsupportedOperationException();}

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public boolean IsEq(Obj o) {
    return Cmp(o) == 0;
  }

  public int compareTo(Obj other) {
    return -Cmp(other);
  }

  public int Cmp(Obj o) {
    if (this == o)
      return 0;
    int id1 = TypeId();
    int id2 = o.TypeId();
    if (id1 == id2)
      return InternalCmp(o);
    return id1 < id2 ? 1 : -1;
  }

  public String toString() {
    StringWriter writer = new StringWriter();
    Print(writer, 90, true, 0);
    return writer.toString();
  }

  public void Print() {
    Writer writer = new OutputStreamWriter(System.out);
    try {
      Print(writer, 90, true, 0);
    }
    finally {
      try {
        writer.close();
      }
      catch (Exception e) {

      }
    }
    System.out.println("");
  }

  public Obj Printed() {
    return Miscellanea.StrToObj(toString());
  }
}
