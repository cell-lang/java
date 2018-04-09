SRC-FILES=$(shell ls src/cell/*.cell src/cell/code-gen/*.cell src/cell/code-gen/wrappers/*.cell)
RUNTIME-FILES=$(shell ls src/core/net/cell_lang/*.java src/automata/net/cell_lang/*.java)

################################################################################
###################### Level 3 AST -> Java code generator ######################

tmp/codegen.cpp: $(SRC-FILES)
	cellc projects/codegen.txt
	rm -rf tmp/*
	# ../build/bin/ren-fns < generated.cpp > tmp/cellc-java.cpp
	# echo >> tmp/cellc-java.cpp
	# echo >> tmp/cellc-java.cpp
	# cat ../build/src/hacks.cpp >> tmp/cellc-java.cpp
	mv generated.cpp tmp/codegen.cpp

codegen: tmp/codegen.cpp
	g++ -O1 tmp/codegen.cpp -o codegen

codegen-dbg: tmp/codegen.cpp
	g++ -ggdb -DNDEBUG tmp/codegen.cpp -o codegen

tmp/codegen.cs: $(SRC-FILES)
	cellc-cs.exe projects/codegen.txt
	rm -rf tmp/*
	mv generated.cs tmp/codegen.cs

codegen.exe: tmp/codegen.cs
	mcs -nowarn:219 tmp/codegen.cs -out:codegen.exe

codegen.jar: $(SRC-FILES) $(RUNTIME-FILES)
	java -jar bin/cellc-java.jar projects/codegen.txt
	javac -d tmp/codegen/ Generated.java
	mv Generated.java tmp/codegen.java
	jar cfe codegen.jar net.cell_lang.Generated -C tmp/codegen/ net/

cellc-java: $(SRC-FILES) $(RUNTIME-FILES)
	cellc projects/compiler-no-runtime.txt
	../build/bin/ren-fns < generated.cpp > tmp/cellc-java.cpp
	mv generated.cpp tmp/
	echo >> tmp/cellc-java.cpp
	echo >> tmp/cellc-java.cpp
	cat ../build/src/hacks.cpp >> tmp/cellc-java.cpp
	g++ -O3 -DNDEBUG tmp/cellc-java.cpp -o cellc-java

cellcd-java: $(SRC-FILES) $(RUNTIME-FILES)
	cellc projects/compiler-no-runtime.txt
	../build/bin/ren-fns < generated.cpp > tmp/cellc-java.cpp
	mv generated.cpp tmp/
	echo >> tmp/cellc-java.cpp
	echo >> tmp/cellc-java.cpp
	cat ../build/src/hacks.cpp >> tmp/cellc-java.cpp
	g++ -ggdb -DNDEBUG tmp/cellc-java.cpp -o cellcd-java

cellc-java.jar: $(SRC-FILES) $(RUNTIME-FILES)
	# java -jar bin/cellc-java.jar projects/compiler-no-runtime.txt
	bin/cellc-java projects/compiler-no-runtime.txt
	bin/apply-hacks < Generated.java > tmp/cellc-java.java
	mv Generated.java tmp/
	javac -d tmp/ tmp/cellc-java.java
	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/

bin/cellc-java.jar: cellc-java.jar
	rm -f bin/cellc-java.jar
	mv cellc-java.jar bin/

bin/cellcd-java.jar: $(SRC-FILES) $(RUNTIME-FILES)
	bin/cellc-java -d projects/compiler-no-runtime.txt
	bin/apply-hacks < Generated.java > tmp/cellcd-java.java
	mv Generated.java tmp/
	javac -g -d tmp/ tmp/cellcd-java.java
	jar cfe cellcd-java.jar net.cell_lang.Generated -C tmp net/
	rm -f bin/cellcd-java.jar
	mv cellcd-java.jar bin/

inputs/tests.txt: tests.cell
	cellc-cs.exe -p projects/tests.txt
	mv dump-opt-code.txt inputs/tests.txt

tests.jar: codegen.exe inputs/tests.txt
	./codegen.exe inputs/tests.txt
	javac -g -d tmp/ Generated.java
	cp Generated.java src/java/net/cell_lang/
	mv Generated.java tmp/tests-by-codegen.exe.java

run-unit-tests:
	javac -g -d tmp/ src/testcases/net/cell_lang/UnitTests.java
	jar cfe tmp/unit-tests.jar net.cell_lang.UnitTests -C tmp net/
	java -jar tmp/unit-tests.jar

################################################################################
################################################################################

clean:
	@rm -f codegen.exe codegen codegen-dbg
	@make -s soft-clean

soft-clean:
	@rm -f generated.cpp generated.cs Generated.java
	@rm -f *.class
	@rm -f src/core/net/cell_lang/*.class
	@rm -f src/automata/net/cell_lang/*.class
	@rm -f src/misc/net/cell_lang/*.class
	@rm -f cellc-java cellcd-java cellc-java.jar cellcd-java.jar
	@rm -rf tmp
	@mkdir tmp
	@mkdir tmp/codegen
