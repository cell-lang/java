#!/usr/bin/env python

################################################################################

src_BinRelIter              = 'core/net/cell_lang/BinRelIter.java'
src_BlankObj                = 'core/net/cell_lang/BlankObj.java'
src_Builder                 = 'core/net/cell_lang/Builder.java'
src_EmptyRelObj             = 'core/net/cell_lang/EmptyRelObj.java'
src_EmptyRelValue           = 'core/net/cell_lang/EmptyRelValue.java'
src_FloatObj                = 'core/net/cell_lang/FloatObj.java'
src_FloatValue              = 'core/net/cell_lang/FloatValue.java'
src_Hacks                   = 'core/net/cell_lang/Hacks.java'
src_IntObj                  = 'core/net/cell_lang/IntObj.java'
src_IntValue                = 'core/net/cell_lang/IntValue.java'
src_Lexer                   = 'core/net/cell_lang/Lexer.java'
src_MasterSeqObj            = 'core/net/cell_lang/MasterSeqObj.java'
src_Miscellanea             = 'core/net/cell_lang/Miscellanea.java'
src_NeBinRelObj             = 'core/net/cell_lang/NeBinRelObj.java'
src_NeBinRelValue           = 'core/net/cell_lang/NeBinRelValue.java'
src_NeSetObj                = 'core/net/cell_lang/NeSetObj.java'
src_NeSetValue              = 'core/net/cell_lang/NeSetValue.java'
src_NeTernRelObj            = 'core/net/cell_lang/NeTernRelObj.java'
src_NeTernRelValue          = 'core/net/cell_lang/NeTernRelValue.java'
src_NullObj                 = 'core/net/cell_lang/NullObj.java'
src_Obj                     = 'core/net/cell_lang/Obj.java'
src_Parser                  = 'core/net/cell_lang/Parser.java'
src_ParsingException        = 'core/net/cell_lang/ParsingException.java'
src_Parsing                 = 'core/net/cell_lang/Parsing.java'
src_Procs                   = 'core/net/cell_lang/Procs.java'
src_SeqObj                  = 'core/net/cell_lang/SeqObj.java'
src_SeqOrSetIter            = 'core/net/cell_lang/SeqOrSetIter.java'
src_SeqValue                = 'core/net/cell_lang/SeqValue.java'
src_SliceObj                = 'core/net/cell_lang/SliceObj.java'
src_SymbObj                 = 'core/net/cell_lang/SymbObj.java'
src_SymbTable               = 'core/net/cell_lang/SymbTable.java'
src_SymbValue               = 'core/net/cell_lang/SymbValue.java'
src_TaggedObj               = 'core/net/cell_lang/TaggedObj.java'
src_TaggedValue             = 'core/net/cell_lang/TaggedValue.java'
src_TernRelIter             = 'core/net/cell_lang/TernRelIter.java'
src_Token                   = 'core/net/cell_lang/Token.java'
src_TokenType               = 'core/net/cell_lang/TokenType.java'
src_ValueBase               = 'core/net/cell_lang/ValueBase.java'
src_Value                   = 'core/net/cell_lang/Value.java'

src_Algs                    = 'algorithms/net/cell_lang/Algs.java'
src_Ints123                 = 'algorithms/net/cell_lang/Ints123.java'
src_Ints12                  = 'algorithms/net/cell_lang/Ints12.java'
src_Ints21                  = 'algorithms/net/cell_lang/Ints21.java'
src_Ints231                 = 'algorithms/net/cell_lang/Ints231.java'
src_Ints312                 = 'algorithms/net/cell_lang/Ints312.java'

src_BinaryTable             = 'automata/net/cell_lang/BinaryTable.java'
src_BinaryTableUpdater      = 'automata/net/cell_lang/BinaryTableUpdater.java'
src_Index                   = 'automata/net/cell_lang/Index.java'
src_OneWayBinTable          = 'automata/net/cell_lang/OneWayBinTable.java'
src_OverflowTable           = 'automata/net/cell_lang/OverflowTable.java'
src_TernaryTable            = 'automata/net/cell_lang/TernaryTable.java'
src_TernaryTableUpdater     = 'automata/net/cell_lang/TernaryTableUpdater.java'
src_UnaryTable              = 'automata/net/cell_lang/UnaryTable.java'
src_UnaryTableUpdater       = 'automata/net/cell_lang/UnaryTableUpdater.java'
src_ValueStoreBase          = 'automata/net/cell_lang/ValueStoreBase.java'
src_ValueStore              = 'automata/net/cell_lang/ValueStore.java'
src_ValueStoreUpdater       = 'automata/net/cell_lang/ValueStoreUpdater.java'

src_Conversions             = 'misc/net/cell_lang/Conversions.java'
src_Pair                    = 'misc/net/cell_lang/Pair.java'
src_Triplet                 = 'misc/net/cell_lang/Triplet.java'
src_WrappingUtils           = 'misc/net/cell_lang/WrappingUtils.java'

################################################################################

std_sources = [
  src_BinRelIter,
  src_BlankObj,
  src_Builder,
  src_EmptyRelObj,
  src_EmptyRelValue,
  src_FloatObj,
  src_FloatValue,
  src_Hacks,
  src_IntObj,
  src_IntValue,
  src_Lexer,
  src_MasterSeqObj,
  src_Miscellanea,
  src_NeBinRelObj,
  src_NeBinRelValue,
  src_NeSetObj,
  src_NeSetValue,
  src_NeTernRelObj,
  src_NeTernRelValue,
  src_NullObj,
  src_Obj,
  src_Parser,
  src_ParsingException,
  src_Parsing,
  src_Procs,
  src_SeqObj,
  src_SeqOrSetIter,
  src_SeqValue,
  src_SliceObj,
  src_SymbObj,
  src_SymbTable,
  src_SymbValue,
  src_TaggedObj,
  src_TaggedValue,
  src_TernRelIter,
  src_Token,
  src_TokenType,
  src_ValueBase,
  # src_Value,

  src_Algs,
  src_Ints123,
  src_Ints12,
  src_Ints21,
  src_Ints231,
  src_Ints312
]

table_sources = [
  src_BinaryTable,
  src_BinaryTableUpdater,
  src_Index,
  src_OneWayBinTable,
  src_OverflowTable,
  src_TernaryTable,
  src_TernaryTableUpdater,
  src_UnaryTable,
  src_UnaryTableUpdater,
  src_ValueStoreBase,
  src_ValueStore,
  src_ValueStoreUpdater
]

interface_sources = [
  src_Conversions,
  # src_Pair,
  # src_Triplet,
  src_WrappingUtils
]


################################################################################

num_of_tabs = 0

def escape(ch):
  if ch == ord('\\'):
    return '\\\\'
  elif ch == ord('"'):
    return '\\"'
  elif ch >= ord(' ') or ch <= ord('~'):
    return chr(ch)
  elif ch == ord('\t'):
    global num_of_tabs
    num_of_tabs += 1
    return '\\t'
  else:
    print 'Invalid character: ' + ch
    exit(1);


def convert_file(file_name, keep_all):
  res = []
  f = open(file_name)
  # i = 0
  for l in f:
    l = l.rstrip()
    # if i > 10:
    #   break
    # i = i + 1
    if not keep_all and (l.startswith('package ') or l.startswith('import ')):
      pass
    else:
      el = ''.join([escape(ord(ch)) for ch in l])
      res.append('"' + el + '"')
  return res


# def to_code(bytes):
#   count = len(bytes)
#   ls = []
#   l = ' '
#   for i, b in enumerate(bytes):
#     l += ' ' + str(b) + (',' if i < count-1 else '')
#     if len(l) > 80:
#       ls.append(l)
#       l = ' '
#   if l:
#     ls.append(l)
#   return ls


def convert_files(directory, file_names, keep_all):
  ls = []
  for i, f in enumerate(file_names):
    if i > 0:
      ls.extend(['""', '""'])
    ls.extend(convert_file(directory + '/' + f, keep_all))
  return ['  ' + l for l in ls]


def data_array_def(array_name, directory, file_names, keep_all):
  lines = convert_files(directory, file_names, keep_all)
  # code = to_code(lines)
  if len(lines) <= 500:
    lines = [l + (',' if i < len(lines) - 1 else '') for i, l in enumerate(lines)]
    return ['String* ' + array_name + ' = ('] + lines + [');']
  code = []
  count = (len(lines) + 499) / 500;
  for i in range(count):
    code += ['String* ' + array_name + '_' + str(i) + ' = (']
    chunk = lines[500 * i : 500 * (i + 1)]
    code += [l + (',' if i < len(chunk) - 1 else '') for i, l in enumerate(chunk)]
    code += [');', '', '']
  pieces = [array_name + '_' + str(i) for i in range(count)]
  code += ['String* ' + array_name + ' = ' + ' & '.join(pieces) + ';']
  return code

################################################################################

from sys import argv, exit

if len(argv) != 4:
  print 'Usage: ' + argv[0] + ' <input directory> <output file> <empty output file>'
  exit(0)

_, input_dir, out_fname, empty_out_fname = argv

file_data = [
  data_array_def('core_runtime', input_dir, std_sources, False),
  data_array_def('table_runtime', input_dir, table_sources, False),
  data_array_def('interface_runtime', input_dir, interface_sources, False),
  data_array_def('value_class_def', input_dir, [src_Value], True),
  data_array_def('pair_class_def', input_dir, [src_Pair], True),
  data_array_def('triplet_class_def', input_dir, [src_Triplet], True)
]

out_file = open(out_fname, 'w')
for i, f in enumerate(file_data):
  if i > 0:
    out_file.write('\n\n')
  for l in f:
    out_file.write(l + '\n');

empty_file_data = [
  data_array_def('core_runtime', input_dir, [], False),
  data_array_def('table_runtime', input_dir, [], False),
  data_array_def('interface_runtime', input_dir, [], False),
  data_array_def('value_class_def', input_dir, [], True),
  data_array_def('pair_class_def', input_dir, [], True),
  data_array_def('triplet_class_def', input_dir, [], True)
]

empty_out_file = open(empty_out_fname, 'w')
for i, f in enumerate(empty_file_data):
  if i > 0:
    empty_out_file.write('\n\n')
  for l in f:
    empty_out_file.write(l + '\n')
