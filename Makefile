JFLAGS = -cp "bin;."
PROCESADOR = src/*.java
APP_RUN = Procesador

.PHONY: compile 
compile:
	@javac -d bin $(JFLAGS) $(PROCESADOR)

run:
	@java $(JFLAGS) $(APP_RUN) $(fich)

.PHONY: clean
clean:
	rm -r bin