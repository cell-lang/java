package net.cell_lang;

import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;


abstract class Obj /*implements Comparable<Obj>*/ {
  public long data;

  public final boolean isBlankObj() {
    return this == BlankObj.singleton;
  }

  public final boolean isNullObj() {
    return this == NullObj.singleton;
  }

  public final boolean isSymb() {
    return this instanceof SymbObj;
  }

  public final boolean IsBool() {
    if (isSymb()) {
      int symbId = getSymbId();
      return symbId == SymbTable.FalseSymbId | symbId == SymbTable.TrueSymbId;
    }
    else
      return false;
  }

  public final boolean isInt() {
    return this instanceof IntObj;
  }

  public final boolean isFloat() {
    return this instanceof FloatObj;
  }

  public final boolean isSeq() {
    return this instanceof SeqObj;
  }

  public final boolean isEmptySeq() {
    return this == EmptySeqObj.singleton;
  }

  public final boolean isNeSeq() {
    return this instanceof NeSeqObj;
  }

  public final boolean isEmptyRel() {
    return this == EmptyRelObj.singleton;
  }

  public final boolean isSet() {
    return isNeSet() | isEmptyRel();
  }

  public final boolean isNeSet() {
    return this instanceof NeSetObj;
  }

  public final boolean isBinRel() {
    return isNeBinRel() | isEmptyRel();
  }

  public final boolean isNeBinRel() {
    return this instanceof NeBinRelObj;
  }

  public final boolean isTernRel() {
    return isNeTernRel() | isEmptyRel();
  }

  public final boolean isNeTernRel() {
    return this instanceof NeTernRelObj;
  }

  public final boolean isTagged() {
    return this instanceof TaggedObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  public final boolean isSymb(int id) {
    return isSymb() && getSymb() == id;
  }

  public final boolean isInt(long n) {
    return isInt() && getInt() == n;
  }

  public final boolean isFloat(double x) {
    return isFloat() && getFloat() == x;
  }

  //////////////////////////////////////////////////////////////////////////////

  public final int getSymbId() {
    return data - Long.MIN_VALUE;
  }

  public final boolean getBool() {
    return getSymbId() == SymbTable.TrueSymbId;
  }

  public final long getLong() {
    return data;
  }

  public final double getDouble() {
    return Double.longBitsToDouble(data);
  }

  public final int getSize() {
    return (int) (data & 0x7FFFFFFF);
  }

  public final int getTagId() {
    return
  }

  public final Obj getTag() {
    return SymbTable.get(getTagId());
  }

  //////////////////////////////////////////////////////////////////////////////

  public final boolean isEq(Obj other) {
    if (this == other)
      return true;

    if (data != other.data)
      return false;

    int extraData = extraData();
    if (isInlineObj(extraData))
      return true;

    int otherExtraData = other.extraData();
    if (extraData != otherExtraData)
      return false;

    return internalOrder(other) == 0;
  }

  public final boolean quickOrder(Obj other) {
    if (this == other)
      return 0;

    long otherData = other.data;
    if (data != otherData)
      return data - otherData;

    int extraData = extraData();
    int otherExtraData = other.extraData();
    if (extraData != otherExtraData)
      return extraData - otherExtraData;

    if (isInlineObj(extraData))
      return 0;

    return internalOrder(other);
  }

  // Can be negative
  public final int internalHashcode() {
    return (int) ((data >>> 32) ^ (data & 0xFFFFFFFFL));
  }

  // Called only when data != other.data and extraData() == other.extraData()
  public abstract int internalOrder(Obj other);

  public abstract int extraData();

  //////////////////////////////////////////////////////////////////////////////

  protected static long seqObjData(int length, long eltsData) {

  }

  //////////////////////////////////////////////////////////////////////////////

  private static final int blankObjId       = 0;
  private static final int nullObjId        = 1;
  private static final int symbObjId        = 2;
  private static final int intObjId         = 3;
  private static final int floatObjId       = 4;
  private static final int emptySeqObjId    = 5;
  private static final int emptyRelObjId    = 6;
  private static final int inlineTagObjId   = 7;

  private static final int neSeqObjId       = 16;
  private static final int neSetObjId       = 17;
  private static final int neBinRelObjId    = 18;
  private static final int neTernRelObjId   = 19;
  private static final int refTagObjId      = 20;

  static boolean isInlineObj(int extraData) {
    return extraData < 16;
  }

  protected static int blankObjExtra()        {return blankObjId;     }
  protected static int nullObjExtra()         {return nullObjId;      }
  protected static int symbObjExtra()         {return symbObjId;      }
  protected static int intObjExtra()          {return intObjId;       }
  protected static int floatObjExtra()        {return floatObjId;     }
  protected static int emptySeqObjExtra()     {return emptySeqObjId;  }
  protected static int emptyRelObjExtra()     {return emptyRelObjId;  }
  protected static int inlineTagObjExtra()    {return inlineTagObjId; }
  protected static int neSeqObjExtra()        {return neSeqObjId;     }
  protected static int neSetObjExtra()        {return neSetObjId;     }
  protected static int neBinRelObjExtra()     {return neBinRelObjId;  }
  protected static int neTernRelObjExtra()    {return neTernRelObjId; }
  protected static int refTagObjExtra()       {return refTagObjId;    }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Sequence operations ////////////////////////////

  public Obj     getObjAt(long idx)             {throw Miscellanea.internalFail(this);}
  public boolean getBoolAt(long idx)            {throw Miscellanea.internalFail(this);}
  public long    getLongAt(long idx)            {throw Miscellanea.internalFail(this);}
  public double  getDoubleAt(long idx)          {throw Miscellanea.internalFail(this);}

  public SeqObj getSlice(long first, long len)  {throw Miscellanea.internalFail(this);}

  public boolean[] getArray(boolean[] buffer)   {throw Miscellanea.internalFail(this);}
  public long[]    getArray(long[] buffer)      {throw Miscellanea.internalFail(this);}
  public double[]  getArray(double[] buffer)    {throw Miscellanea.internalFail(this);}
  public Obj[]     getArray(Obj[] buffer)       {throw Miscellanea.internalFail(this);}

  public byte[] getUnsignedByteArray()          {throw Miscellanea.internalFail(this);}

  public SeqObj reverse()                       {throw Miscellanea.internalFail(this);}
  public SeqObj concat(Obj seq)                 {throw Miscellanea.internalFail(this);}

  public SeqObj append(Obj obj)                 {throw Miscellanea.internalFail(this);}
  public SeqObj append(boolean value)           {throw Miscellanea.internalFail(this);}
  public SeqObj append(long value)              {throw Miscellanea.internalFail(this);}
  public SeqObj append(double value)            {throw Miscellanea.internalFail(this);}

  // Copy-on-write update
  public SeqObj updatedAt(long idx, Obj value)  {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// Set operations ///////////////////////////////

  public boolean hasElem(Obj obj)   {throw Miscellanea.internalFail(this);}
  public SetIter getSetIter()       {throw Miscellanea.internalFail(this);}
  public SeqObj  internalSort()     {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  ////////////////////////// OBSOLETE STUFF TO REMOVE //////////////////////////

  public long[] getLongArray() {
    return getArray((long[]) null);
  }

  public byte[] getByteArray() {
    return getUnsignedByteArray();
  }

  public void initAt(long i, Obj v) {throw Miscellanea.internalFail(this);}

  public Obj negate() {
    if (id == SymbTable.FalseSymbId)
      return SymbObj.get(SymbTable.TrueSymbId);
    if (id == SymbTable.TrueSymbId)
      return SymbObj.get(SymbTable.FalseSymbId);
    throw new UnsupportedOperationException();
  }

  public Obj getItem(long i) {
    return getObjAt(i);
  }

  public long mantissa() {throw Miscellanea.internalFail(this);}
  public long decExp()   {throw Miscellanea.internalFail(this);}

  public SeqOrSetIter getSeqOrSetIter() {throw Miscellanea.internalFail(this);}

  public void copyItems(Obj[] items, int offset)        {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeMap()                              {return false;}
  public boolean isNeRecord()                           {return false;}
  public boolean isSyntacticSugaredString()             {return false;}

  public boolean hasKey(Obj o)                          {throw Miscellanea.internalFail(this);}
  public boolean hasField(int id)                       {throw Miscellanea.internalFail(this);}
  public boolean hasPair(Obj o1, Obj o2)                {throw Miscellanea.internalFail(this);}
  public boolean hasTriple(Obj o1, Obj o2, Obj o3)      {throw Miscellanea.internalFail(this);}

  public Obj  getInnerObj()                             {throw Miscellanea.internalFail(this);}
  public long getInnerLong()                            {throw Miscellanea.internalFail(this);}

  public BinRelIter  getBinRelIter()                    {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIter()                   {throw Miscellanea.internalFail(this);}

  public BinRelIter getBinRelIterByCol1(Obj obj)        {throw Miscellanea.internalFail(this);}
  public BinRelIter getBinRelIterByCol2(Obj obj)        {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIterByCol1(Obj val)      {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol2(Obj val)      {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol3(Obj val)      {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIterByCol12(Obj val1, Obj val2)  {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol13(Obj val1, Obj val3)  {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol23(Obj val2, Obj val3)  {throw Miscellanea.internalFail(this);}

  public String getString()                             {throw Miscellanea.internalFail(this);}

  public Obj lookup(Obj key)                            {throw Miscellanea.internalFail(this);}
  public Obj lookupField(int id)                        {throw Miscellanea.internalFail(this);}

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

  public abstract void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel);
  public abstract int minPrintedSize();

  // public abstract int hashCode();
  // protected abstract int typeId();
  // protected abstract int internalCmp(Obj o);

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public Obj randElem() {throw Miscellanea.internalFail(this);}

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////





  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  // public int compareTo(Obj other) {
  //   return -internalOrder(other);
  // }

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
