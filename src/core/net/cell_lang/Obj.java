package net.cell_lang;

import java.io.Writer;
import java.io.StringWriter;
import java.io.OutputStreamWriter;


abstract class Obj implements Comparable<Obj> {
  public int extraData;
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

  public final boolean isBool() {
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
    return this instanceof TaggedObj || this instanceof OptTagRecObj;
  }

  //////////////////////////////////////////////////////////////////////////////

  public final boolean isSymb(int id) {
    return isSymb() && getSymbId() == id;
  }

  public final boolean isInt(long n) {
    return isInt() && getLong() == n;
  }

  public final boolean isFloat(double x) {
    return isFloat() && getDouble() == x;
  }

  //////////////////////////////////////////////////////////////////////////////

  public final int getSymbId() {
    return (int) (data - Long.MIN_VALUE);
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
    return (int) (data & 0x3FFFFFFF);
  }

  public final int getTagId() {
    return (int) (data & 0xFFFF);
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

    int otherExtraData = other.extraData;
    if (extraData != otherExtraData)
      return false;

    if (isInlineObj())
      return true;

    return internalOrder(other) == 0;
  }

  public final int quickOrder(Obj other) {
    if (this == other)
      return 0;

    long otherData = other.data;
    if (data != otherData)
      return data < otherData ? -1 : 1;

    int otherExtraData = other.extraData;
    if (extraData != otherExtraData)
      return extraData < otherExtraData ? -1 : 1;

    if (isInlineObj())
      return 0;

    return internalOrder(other);
  }

  // Called only when data == other.data and extraData == other.extraData
  public abstract int internalOrder(Obj other);

  public int hashcode() {
    return Utils.jenkinsHash((int) (data >>> 32), (int) data, extraData);
  }

  //////////////////////////////////////////////////////////////////////////////

  protected static long symbObjData(int id) {
    return Long.MIN_VALUE + id;
  }

  protected static long boolObjData(boolean value) {
    return Long.MIN_VALUE + (value ? SymbTable.TrueSymbId : SymbTable.FalseSymbId);
  }

  protected static long floatObjData(double value) {
    return Double.doubleToRawLongBits(value);
  }

  // 11 - 32 bit hash code - 30 bit size/length
  protected static long seqObjData(int length, long hashcode) {
    return (3L << 62) | (packedHashcode(hashcode) << 30) | length;
  }

  // 11 - 32 bit hash code - 30 bit size
  protected static long setObjData(int size, long hashcode) {
    return (3L << 62) | (packedHashcode(hashcode) << 30) | size;
  }

  // 11 - 32 bit hash code - 30 bit size
  protected static long binRelObjData(int size, long hashcode) {
    return (3L << 62) | (packedHashcode(hashcode) << 30) | size;
  }

  // 11 - 32 bit hash code - 30 bit size
  protected static long ternRelObjData(int size, long hashcode) {
    return (3L << 62) | (packedHashcode(hashcode) << 30) | size;
  }

  // 16 bit 1 padding - 32 bit hash code - 16 bit tag id
  protected static long tagObjData(int tag, long hashcode) {
    return (0xFFFFL << 48) | (packedHashcode(hashcode) << 24) | (tag & 0xFFFF);
  }

  // 16 bit optional field mask - 32 bit hash code - 16 bit tag id
  public static long optTagRecObjData(int tag, long hashcode, int optFieldsMask) {
    return (((long) optFieldsMask) << 48) | (packedHashcode(hashcode) << 16) | (tag & 0xFFFF);
  }

  private static long packedHashcode(long hashcode) {
    return ((hashcode >>> 32) ^ hashcode) & 0xFFFFFFFFL;
  }

  //////////////////////////////////////////////////////////////////////////////

  private static final int blankObjId           = 0;
  private static final int nullObjId            = 1;
  private static final int symbObjId            = 2;
  private static final int intObjId             = 3;
  private static final int floatObjId           = 4;
  private static final int emptySeqObjId        = 5;
  private static final int emptyRelObjId        = 6;
  private static final int inlineTagObjId       = 7;

  private static final int neSeqObjId           = 16;
  private static final int neSetObjId           = 17;
  private static final int neBinRelObjId        = 18;
  private static final int neTernRelObjId       = 19;
  private static final int refTagObjId          = 20;

  private static final int optTagRecObjBaseId   = 21;

  boolean isInlineObj() {
    return extraData < 16;
  }

  protected static int blankObjExtraData()          {return blankObjId;         }
  protected static int nullObjExtraData()           {return nullObjId;          }
  protected static int symbObjExtraData()           {return symbObjId;          }
  protected static int intObjExtraData()            {return intObjId;           }
  protected static int floatObjExtraData()          {return floatObjId;         }
  protected static int emptySeqObjExtraData()       {return emptySeqObjId;      }
  protected static int emptyRelObjExtraData()       {return emptyRelObjId;      }
  protected static int inlineTagObjExtraData()      {return inlineTagObjId;     }
  protected static int neSeqObjExtraData()          {return neSeqObjId;         }
  protected static int neSetObjExtraData()          {return neSetObjId;         }
  protected static int neBinRelObjExtraData()       {return neBinRelObjId;      }
  protected static int neTernRelObjExtraData()      {return neTernRelObjId;     }
  protected static int refTagObjExtraData()         {return refTagObjId;        }

  protected static int optTagRecObjExtraData(int idx) {
    return idx + optTagRecObjBaseId;
  }

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////////// Sequence operations ////////////////////////////

  public Obj     getObjAt(long idx)               {throw Miscellanea.internalFail(this);}
  public boolean getBoolAt(long idx)              {throw Miscellanea.internalFail(this);}
  public long    getLongAt(long idx)              {throw Miscellanea.internalFail(this);}
  public double  getDoubleAt(long idx)            {throw Miscellanea.internalFail(this);}

  public SeqObj getSlice(long first, long len)    {throw Miscellanea.internalFail(this);}

  public boolean[] getArray(boolean[] buffer)     {throw Miscellanea.internalFail(this);}
  public long[]    getArray(long[] buffer)        {throw Miscellanea.internalFail(this);}
  public double[]  getArray(double[] buffer)      {throw Miscellanea.internalFail(this);}
  public Obj[]     getArray(Obj[] buffer)         {throw Miscellanea.internalFail(this);}

  public byte[] getUnsignedByteArray()            {throw Miscellanea.internalFail(this);}

  public SeqIter getSeqIter()                     {throw Miscellanea.internalFail(this);}

  public SeqObj reverse()                         {throw Miscellanea.internalFail(this);}
  public SeqObj concat(Obj seq)                   {throw Miscellanea.internalFail(this);}

  public NeSeqObj append(Obj obj)                 {throw Miscellanea.internalFail(this);}
  public NeSeqObj append(boolean value)           {throw Miscellanea.internalFail(this);}
  public NeSeqObj append(long value)              {throw Miscellanea.internalFail(this);}
  public NeSeqObj append(double value)            {throw Miscellanea.internalFail(this);}

  // Copy-on-write update
  public NeSeqObj updatedAt(long idx, Obj value)  {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  /////////////////////////////// Set operations ///////////////////////////////

  public boolean hasElem(Obj obj)   {throw Miscellanea.internalFail(this);}
  public SetIter getSetIter()       {throw Miscellanea.internalFail(this);}
  public SeqObj  internalSort()     {throw Miscellanea.internalFail(this);}
  public Obj     randElem()         {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////// Binary relation operations /////////////////////////

  public boolean isNeMap()                              {return false;}
  public boolean isNeRecord()                           {return false;}

  public boolean hasKey(Obj o)                          {throw Miscellanea.internalFail(this);}
  public boolean hasField(int id)                       {throw Miscellanea.internalFail(this);}
  public boolean hasPair(Obj o1, Obj o2)                {throw Miscellanea.internalFail(this);}

  public Obj lookup(Obj key)                            {throw Miscellanea.internalFail(this);}
  public Obj lookupField(int id)                        {throw Miscellanea.internalFail(this);}

  public BinRelIter getBinRelIter()                     {throw Miscellanea.internalFail(this);}

  public BinRelIter getBinRelIterByCol1(Obj obj)        {throw Miscellanea.internalFail(this);}
  public BinRelIter getBinRelIterByCol2(Obj obj)        {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  ///////////////////////// Ternary relation operations ////////////////////////

  public boolean hasTriple(Obj o1, Obj o2, Obj o3)              {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIter()                           {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIterByCol1(Obj val)              {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol2(Obj val)              {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol3(Obj val)              {throw Miscellanea.internalFail(this);}

  public TernRelIter getTernRelIterByCol12(Obj val1, Obj val2)  {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol13(Obj val1, Obj val3)  {throw Miscellanea.internalFail(this);}
  public TernRelIter getTernRelIterByCol23(Obj val2, Obj val3)  {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  /////////////////////////// Tagged obj operations ///////////////////////////

  public Obj  getInnerObj()  {throw Miscellanea.internalFail(this);}
  public long getInnerLong() {throw Miscellanea.internalFail(this);}

  public boolean isSyntacticSugaredString() {return false;}
  public String getString() {throw Miscellanea.internalFail(this);}

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

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

  public void dump() {
    System.out.println("ERROR: NO DUMP AVAILABLE");
  }

  ////////////////////////////////////////////////////////////////////////////
  ////////////////////////////////////////////////////////////////////////////

  public abstract void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel);
  public abstract int minPrintedSize();
  public abstract ValueBase getValue();

  //////////////////////////////////////////////////////////////////////////////
  ////////////////////////// OBSOLETE STUFF TO REMOVE //////////////////////////

  public long[] getLongArray() {
    return getArray((long[]) null);
  }

  public byte[] getByteArray() {
    return getUnsignedByteArray();
  }

  public void initAt(long i, Obj v) {throw Miscellanea.internalFail(this);}

  public long mantissa() {throw Miscellanea.internalFail(this);}
  public long decExp()   {throw Miscellanea.internalFail(this);}

  public final int compareTo(Obj other) {
    return quickOrder(other);
  }

  public final int cmp(Obj other) {
    return -quickOrder(other);
  }
}
