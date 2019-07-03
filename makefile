SRC-FILES=$(shell ls src/cell/*.cell src/cell/code-gen/*.cell src/cell/code-gen/wrappers/*.cell)
RUNTIME-FILES=$(shell ls src/core/net/cell_lang/*.java src/automata/net/cell_lang/*.java)

################################################################################

codegen.jar: $(SRC-FILES) $(RUNTIME-FILES)
	java -jar bin/cellc-java.jar projects/codegen.txt
	javac -d tmp/codegen/ Generated.java
	mv Generated.java tmp/codegen.java
	jar cfe codegen.jar net.cell_lang.Generated -C tmp/codegen/ net/

################################################################################

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
	mkdir -p tmp/
	rm -rf tmp/*
	mkdir tmp/gen/
	# java -jar bin/cellc-java.jar projects/compiler-no-runtime.txt
	bin/cellc-java projects/compiler-no-runtime.txt tmp/gen/
	mv tmp/gen/Generated.java tmp/
	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
	javac -d tmp/ tmp/gen/*.java
	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/
	rm -f bin/cellc-java.jar
	mv cellc-java.jar bin/

cellcd-java.jar: $(SRC-FILES) $(RUNTIME-FILES)
	mkdir -p tmp/
	rm -rf tmp/*
	mkdir tmp/gen/
	# bin/cellc-java -d projects/compiler-no-runtime.txt tmp/gen/
	java -jar bin/cellc-java.jar projects/compiler-no-runtime.txt tmp/gen/
	mv tmp/gen/Generated.java tmp/
	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
	javac -g -d tmp/ tmp/gen/*.java
	jar cfe cellcd-java.jar net.cell_lang.Generated -C tmp net/
	rm -f bin/cellcd-java.jar
	mv cellcd-java.jar bin/

################################################################################
################################################################################

compiler-test-loop:
	rm -f runtime/*
	bin/build-runtime-src-file.py src/ runtime/runtime-sources.cell runtime/runtime-sources-empty.cell

	rm -rf tmp/
	mkdir tmp
	mkdir tmp/gen/

	# bin/cellc-java projects/compiler.txt tmp/gen/
	java -jar bin/cellc-java.jar projects/compiler.txt tmp/gen/
	mv tmp/gen/Generated.java tmp/
	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
	javac -d tmp/ tmp/gen/*.java
	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/
	rm -rf tmp/*
	mkdir tmp/gen/

# 	java -jar cellc-java.jar projects/compiler.txt tmp/gen/
# 	mv tmp/gen/Generated.java tmp/
# 	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
# 	javac -d tmp/ tmp/gen/*.java
# 	rm cellc-java.jar
# 	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/
# 	rm -rf tmp/*
# 	mkdir tmp/gen/

	java -jar cellc-java.jar projects/compiler.txt tmp/gen/
	mv tmp/gen/Generated.java tmp/
	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
	javac -d tmp/ tmp/gen/*.java
	rm cellc-java.jar
	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/
	rm -rf tmp/gen/* tmp/net/

	java -jar cellc-java.jar projects/compiler.txt tmp/gen/
	cmp tmp/gen/Generated.java tmp/Generated.java

update-cellc-java.jar:
	rm -rf cellc-java.jar tmp/net/
	javac -d tmp/ tmp/gen/*.java
	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/

################################################################################
################################################################################

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
	@rm -f generated.cpp generated.cs Generated.java Generated-A.java
	@rm -f *.class
	@rm -f src/core/net/cell_lang/*.class
	@rm -f src/automata/net/cell_lang/*.class
	@rm -f src/misc/net/cell_lang/*.class
	@rm -f cellc-java cellcd-java cellc-java.jar cellcd-java.jar
	@rm -rf tmp
	@mkdir tmp
	@mkdir tmp/codegen
