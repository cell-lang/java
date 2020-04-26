SRC-FILES=$(shell ls src/cell/*.cell src/cell/code-gen/*.cell src/cell/code-gen/wrappers/*.cell)
RUNTIME-FILES=$(shell ls src/core/net/cell_lang/*.java src/automata/net/cell_lang/*.java)

################################################################################

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
	java -jar bin/cellc-java.jar -d projects/compiler-no-runtime.txt tmp/gen/
	mv tmp/gen/Generated.java tmp/
	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
	javac -g -d tmp/ tmp/gen/*.java src/hacks/net/cell_lang/Hacks.java
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
	javac -d tmp/ tmp/gen/*.java src/hacks/net/cell_lang/Hacks.java
	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/
	rm -rf tmp/*
	mkdir tmp/gen/

# 	java -jar cellc-java.jar projects/compiler.txt tmp/gen/
# 	mv tmp/gen/Generated.java tmp/
# 	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
# 	javac -d tmp/ tmp/gen/*.java src/hacks/net/cell_lang/Hacks.java
# 	rm cellc-java.jar
# 	jar cfe cellc-java.jar net.cell_lang.Generated -C tmp net/
# 	rm -rf tmp/*
# 	mkdir tmp/gen/

	java -jar cellc-java.jar projects/compiler.txt tmp/gen/
	mv tmp/gen/Generated.java tmp/
	bin/apply-hacks < tmp/Generated.java > tmp/gen/Generated.java
	javac -d tmp/ tmp/gen/*.java src/hacks/net/cell_lang/Hacks.java
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
	@rm -f generated.cpp generated.cs Generated.java Generated-A.java
	@rm -f *.class
	@rm -f src/core/net/cell_lang/*.class
	@rm -f src/automata/net/cell_lang/*.class
	@rm -f src/misc/net/cell_lang/*.class
	@rm -f cellc-java cellcd-java cellc-java.jar cellcd-java.jar
	@rm -f debug/*
	@rm -rf tmp
	@mkdir tmp
	@mkdir tmp/codegen
