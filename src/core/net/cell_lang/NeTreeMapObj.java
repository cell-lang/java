package net.cell_lang;

import java.util.Random;
import java.io.Writer;


class NeTreeMapObj extends Obj {
  Node rootNode;
  NeBinRelObj packedRepr;

  //////////////////////////////////////////////////////////////////////////////

  private NeTreeMapObj(int size, Node rootNode) {
    data = binRelObjData(size);
    extraData = neBinRelObjExtraData();
    this.rootNode = rootNode;
  }

  public NeTreeMapObj(Obj key, Obj value) {
    data = binRelObjData(1);
    extraData = neBinRelObjExtraData();
    rootNode = new StdNode(key, value, key.hashcode());
  }

  // public NeTreeMapObj(Obj[] keys, Obj[] values, int first, int count) {
  //   data = binRelObjData(count);
  //   extraData = neBinRelObjExtraData();
  //   rootNode = newNode(keys, values, first, count);
  // }

  //////////////////////////////////////////////////////////////////////////////

  public Obj setKeyValue(Obj key, Obj value) {
    if (rootNode != null) {
      int hashcode = key.hashcode();
      Obj currValue = rootNode.lookup(key, hashcode);
      if (currValue != null) {
        if (currValue.isEq(value))
          return this;
        StdNode node = rootNode.insert(key, value, hashcode);
        return new NeTreeMapObj(getSize(), node);
      }
      else {
        StdNode node = rootNode.insert(key, value, hashcode);
        return new NeTreeMapObj(getSize() + 1, node);
      }
    }
    else
      return packedRepr.setKeyValue(key, value);
  }

  public Obj removeKey(Obj key) {
    if (rootNode != null) {
      int hashcode = key.hashcode();
      Obj currValue = rootNode.lookup(key, hashcode);
      if (currValue != null) {
        Node node = rootNode.remove(key, hashcode);
        return node != null ? new NeTreeMapObj(getSize()-1, node) : EmptyRelObj.singleton;
      }
      else
        return this;
    }
    else
      return packedRepr.removeKey(key);
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean isNeMap() {
    return true;
  }

  public boolean isNeRecord() {
    return rootNode != null ? rootNode.keysAreSymbols() : packedRepr.isNeRecord();
  }

  //////////////////////////////////////////////////////////////////////////////

  public boolean hasKey(Obj key) {
    return rootNode != null ? rootNode.lookup(key, key.hashcode()) != null : packedRepr.hasKey(key);
  }

  public boolean hasField(int symbId) {
    return hasKey(SymbObj.get(symbId));
  }

  public boolean hasPair(Obj key, Obj value) {
    if (rootNode != null) {
      Obj currValue = rootNode.lookup(key, key.hashcode());
      return currValue != null && currValue.isEq(value);
    }
    else
      return packedRepr.hasPair(key, value);
  }

  //////////////////////////////////////////////////////////////////////////////

  public BinRelIter getBinRelIter() {
    pack();
    return packedRepr.getBinRelIter();
  }

  public BinRelIter getBinRelIterByCol1(Obj key) {
    if (rootNode != null) {
      Obj value = rootNode.lookup(key, key.hashcode());
      if (value != null)
        return new BinRelIter(new Obj[] {key}, new Obj[] {value});
      else
        return new BinRelIter(Miscellanea.emptyObjArray, Miscellanea.emptyObjArray);
    }
    else
      return packedRepr.getBinRelIterByCol1(key);
  }

  public BinRelIter getBinRelIterByCol2(Obj obj) {
    pack();
    return packedRepr.getBinRelIterByCol2(obj);
  }

  //////////////////////////////////////////////////////////////////////////////

  public Obj lookup(Obj key) {
    if (rootNode != null) {
      Obj value = rootNode.lookup(key, key.hashcode());
      if (value == null)
        throw Miscellanea.softFail("Key not found:", "collection", this, "key", key);
      return value;
    }
    else
      return packedRepr.lookup(key);
  }

  public Obj lookupField(int symbId) {
    return lookup(SymbObj.get(symbId));
  }

  //////////////////////////////////////////////////////////////////////////////

  public int internalOrder(Obj other) {
    pack();
    return packedRepr.internalOrder(other);
  }

  public TypeCode getTypeCode() {
    return TypeCode.NE_BIN_REL;
  }

  //////////////////////////////////////////////////////////////////////////////

  public void print(Writer writer, int maxLineLen, boolean newLine, int indentLevel) {
    pack();
    packedRepr.print(writer, maxLineLen, newLine, indentLevel);
  }

  public int minPrintedSize() {
    pack();
    return packedRepr.minPrintedSize();
  }

  public ValueBase getValue() {
    pack();
    return packedRepr.getValue();
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private void pack() {
    if (rootNode != null) {
      Miscellanea._assert(packedRepr == null);
      int size = getSize();
      Obj[] keys = new Obj[size];
      Obj[] values = new Obj[size];
      int count = rootNode.traverse(keys, values, 0);
      Miscellanea._assert(count == size);
      // packedRepr = new NeBinRelObj(keys, values, true);
      packedRepr = (NeBinRelObj) Builder.createMap(keys, values); //## BAD BAD BAD
      rootNode = null;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private interface Node {
    Obj lookup(Obj key, int hashcode);

    StdNode insert(Obj key, Obj value, int hashcode);
    Node remove(Obj key, int hashcode);

    Node merge(Node right);

    int traverse(Obj[] keys, Obj[] values, int offset);
    boolean keysAreSymbols();

    String[] toStrings();
  }


  // private static Node newNode(Obj[] keys, Obj[] values, int first, int count) {
  //   Miscellanea._assert(count > 0);
  //   if (count > 1)
  //     return newNode(keys, values, first, count);
  //   else
  //     return new StdNode(keys[first], values[first], random.nextInt());
  // }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  private static final class StdNode implements Node {
    Obj key;
    Obj value;
    int hashcode;
    Node left, right;


    private StdNode(Obj key, Obj value, int hashcode, Node left, Node right) {
      this.key = key;
      this.value = value;
      this.hashcode = hashcode;
      this.left = left;
      this.right = right;
    }

    private StdNode(Obj key, Obj value, int hashcode) {
      this.key = key;
      this.value = value;
      this.hashcode = hashcode;
    }

    ////////////////////////////////////////////////////////////////////////////

    public Obj lookup(Obj aKey, int aHashcode) {
      int ord = order(aKey, aHashcode);

      if (ord > 0) // search key < node key, searching the left node
        return left != null ? left.lookup(aKey, aHashcode) : null;
      else if (ord < 0) // node key < search key, searching the right node
        return right != null ? right.lookup(aKey, aHashcode) : null;
      else
        return value;
    }

    ////////////////////////////////////////////////////////////////////////////

    public StdNode insert(Obj aKey, Obj aValue, int aHashcode) {
      int ord = order(aKey, aHashcode);

      if (ord > 0) {
        Node node = left != null ? left.insert(aKey, aValue, aHashcode) : new StdNode(aKey, aValue, aHashcode);
        return new StdNode(key, value, hashcode, node, right);
      }
      else if (ord < 0) {
        Node node = right != null ? right.insert(aKey, aValue, aHashcode) : new StdNode(aKey, aValue, aHashcode);
        return new StdNode(key, value, hashcode, left, node);
      }
      else {
        return new StdNode(key, aValue, hashcode, left, right);
      }
    }

    public Node remove(Obj aKey, int aHashcode) {
      int ord = order(aKey, aHashcode);

      if (ord > 0) {
        // search key < node key
        Node node = left.remove(aKey, aHashcode);
        return new StdNode(key, value, hashcode, node, right);
      }
      else if (ord < 0) {
        // node key < search key
        Node node = right.remove(aKey, aHashcode);
        return new StdNode(key, value, hashcode, left, node);
      }
      else {
        if (left == null)
          return right;
        else if (right == null)
          return left;
        else
          return left.merge(right);
      }
    }

    ////////////////////////////////////////////////////////////////////////////

    private int order(Obj aKey, int aHashcode) {
      return aHashcode < hashcode ? 1 : (aHashcode > hashcode ? -1 : key.quickOrder(aKey));
    }

    public Node merge(Node _other) {
      StdNode other = (StdNode) _other;
      if (this.right == null)
        return new StdNode(key, value, hashcode, left, other);
      else if (other.left == null)
        return new StdNode(other.key, other.value, other.hashcode, this, other.right);
      else
        return new StdNode(key, value, hashcode, left, right.merge(other));
    }

    ////////////////////////////////////////////////////////////////////////////

    public int traverse(Obj[] keys, Obj[] values, int offset) {
      if (left != null)
        offset = left.traverse(keys, values, offset);
      keys[offset] = key;
      values[offset++] = value;
      if (right != null)
        offset = right.traverse(keys, values, offset);
      return offset;
    }

    public boolean keysAreSymbols() {
      return (left == null || left.keysAreSymbols()) && key.isSymb() && (right == null || right.keysAreSymbols());
    }

    ////////////////////////////////////////////////////////////////////////////

    public String toString() {
      String[] strs = toStrings();
      String str = "";
      for (int i=0 ; i < strs.length ; i++) {
        if (i > 0)
          str += "\n";
        str += strs[i];
      }
      return str;
    }

    public String[] toStrings() {
      String[] leftStrs = left != null ? left.toStrings() : new String[] {"null"};
      String[] rightStrs = right != null ? right.toStrings() : new String[] {"null"};
      int count = 1 + leftStrs.length + rightStrs.length;
      String[] strs = new String[count];
      strs[0] = String.format("StdNode: key = %s, value = %s, hashcode = %d", key, value, hashcode);
      for (int i=0 ; i < leftStrs.length ; i++)
        strs[i+1] = "  " + leftStrs[i];
      for (int i=0 ; i < rightStrs.length ; i++)
        strs[i+1+leftStrs.length] = "  " + rightStrs[i];
      return strs;
    }
  }

  //////////////////////////////////////////////////////////////////////////////
  //////////////////////////////////////////////////////////////////////////////

  // private static final class ArraysNode implements Node {
  //   Obj[] keys;
  //   Obj[] values;
  //   int first;
  //   int count;

  //   public String[] toStrings() {
  //     String[] strs = new String[count+1];
  //     strs[0] = "ArraysNode";
  //     for (int i=0 ; i < count ; i++)
  //       strs[i+1] = "  " + keys[first+i].toString() + " -> " + values[first+i].toString();
  //     return strs;
  //   }


  //   ArraysNode(Obj[] keys, Obj[] values, int first, int count) {
  //     Miscellanea._assert(count >= 2);
  //     this.keys = keys;
  //     this.values = values;
  //     this.first = first;
  //     this.count = count;
  //   }

  //   public Obj lookup(Obj key) {
  //     int idx = Algs.binSearch(keys, first, count, key);
  //     return idx != -1 ? values[idx] : null;
  //   }

  //   ////////////////////////////////////////////////////////////////////////////

  //   public int traverse(Obj[] keys, Obj[] values, int offset) {
  //     for (int i=0 ; i < count ; i++) {
  //       keys[offset] = this.keys[first+i];
  //       values[offset++] = this.values[first+i];
  //     }
  //     return offset;
  //   }

  //   public boolean keysAreSymbols() {
  //     for (int i=0 ; i < count ; i++)
  //       if (!keys[first+i].isSymb())
  //         return false;
  //     return true;
  //   }

  //   ////////////////////////////////////////////////////////////////////////////

  //   public StdNode insert(Obj key, Obj value, int priority) {
  //     Node right = null, left = null;

  //     int lastIdx = first + count - 1;
  //     int idx = Algs.binSearchEx(keys, first, count, key);

  //     if (idx >= 0) {
  //       // <key> was found
  //       int leftCount = idx - first;
  //       if (leftCount > 1)
  //         left = newNode(keys, values, first, leftCount);
  //       else if (leftCount == 1)
  //         left = new StdNode(keys[first], values[first], priority); //## BAD BAD BAD: REUSING priority

  //       int rightCount = lastIdx - idx;
  //       if (rightCount > 1)
  //         right = newNode(keys, values, idx + 1, rightCount);
  //       else if (rightCount == 1)
  //         right = new StdNode(keys[lastIdx], values[lastIdx], priority); //## BAD BAD BAD: REUSING priority
  //     }
  //     else {
  //       // <key> was not found
  //       int insIdx = -idx - 1;

  //       int leftCount = insIdx - first;
  //       if (leftCount > 1)
  //         left = newNode(keys, values, first, leftCount);
  //       else if (leftCount == 1)
  //         left = new StdNode(keys[first], values[first], priority); //## BAD BAD BAD: REUSING priority

  //       int rightCount = lastIdx - insIdx + 1;
  //       if (rightCount > 1)
  //         right = newNode(keys, values, insIdx, rightCount);
  //       else if (rightCount == 1)
  //         right = new StdNode(keys[insIdx], values[insIdx], priority); //## BAD BAD BAD: REUSING priority
  //     }

  //     return new StdNode(key, value, priority, left, right);
  //   }

  //   ////////////////////////////////////////////////////////////////////////////

  //   public Node remove(Obj key) {
  //     int idx = Algs.binSearch(keys, first, count, key);
  //     if (idx == -1)
  //       return this;

  //     int lastIdx = first + count - 1;

  //     int countl = idx - first;
  //     int countr = lastIdx - idx;

  //     if (countl > 1) {
  //       if (countr > 1) {
  //         if (countl > countr) { // countl > countr >= 2  =>  countl >= 3
  //           Node left = newNode(keys, values, first, countl-1);
  //           Node right = newNode(keys, values, idx+1, countr);
  //           return new StdNode(keys[idx-1], values[idx-1], random.nextInt(), left, right);
  //         }
  //         else { // countr >= countl >= 2
  //           Node left = newNode(keys, values, first, countl);
  //           Node right = newNode(keys, values, idx+2, countr-1);
  //           return new StdNode(keys[idx+1], values[idx+1], random.nextInt(), left, right);
  //         }
  //       }
  //       else if (countr == 1) {
  //         Node left = newNode(keys, values, first, countl);
  //         return new StdNode(keys[lastIdx], values[lastIdx], random.nextInt(), left, null);
  //       }
  //       else {
  //         return newNode(keys, values, first, countl);
  //       }
  //     }
  //     else if (countl == 1) {
  //       if (countr > 1) {
  //         Node right = newNode(keys, values, idx+1, countr);
  //         return new StdNode(keys[first], values[first], random.nextInt(), null, right);
  //       }
  //       else if (countr == 1) {
  //         Obj[] remKeys = new Obj[2];
  //         Obj[] remValues = new Obj[2];
  //         remKeys[0] = keys[first];
  //         remKeys[1] = keys[lastIdx];
  //         remValues[0] = values[first];
  //         remValues[1] = values[lastIdx];
  //         return newNode(remKeys, remValues, 0, 2);
  //       }
  //       else {
  //         return new StdNode(keys[first], values[first], random.nextInt());
  //       }
  //     }
  //     else {
  //       if (countr > 1) {
  //         return newNode(keys, values, idx+1, countr);
  //       }
  //       else if (countr == 1) {
  //         return new StdNode(keys[lastIdx], values[lastIdx], random.nextInt());
  //       }
  //       else {
  //         return null;
  //       }
  //     }
  //   }

  //   ////////////////////////////////////////////////////////////////////////////

  //   public Node merge(Node aNode) {
  //     if (aNode instanceof StdNode) {
  //       StdNode node = (StdNode) aNode;
  //       Node newLeft = node.left != null ? merge(node.left) : this;
  //       return new StdNode(node.key, node.value, node.priority, newLeft, node.right);
  //     }
  //     else {
  //       ArraysNode node = (ArraysNode) aNode;
  //       if (count > node.count) { // count > node.count >= 2  =>  count >= 3
  //         Node left = newNode(keys, values, first, count-1);
  //         int idx = first + count - 1;
  //         return new StdNode(keys[idx], values[idx], random.nextInt(), left, node);

  //       }
  //       else { // node.count >= count >= 2  =>  count >= 2
  //         Node right = newNode(node.keys, node.values, node.first+1, node.count-1);
  //         return new StdNode(node.keys[node.first], values[node.first], random.nextInt(), this, right);
  //       }
  //     }
  //   }
  // }
}