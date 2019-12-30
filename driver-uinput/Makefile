TARGET = networktablet
PREFIX = /usr/local
BINDIR = $(PREFIX)/bin
CFLAGS = $(shell pkg-config --cflags cairo)
LDFLAGS = $(shell pkg-config --libs cairo x11) -lpthread

networktablet : networktablet.o

networktablet.o : networktablet.c

clean :
	rm -f networktablet

install :
	install -D networktablet $(DESTDIR)$(BINDIR)/networktablet
