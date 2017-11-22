
driver.out: driver.o
	g++  driver.o -o driver.out
logger.o: logger.cpp
	g++ -c logger.cpp logger.h -o logger.o
update.o: update.cpp
	g++ -c update.cpp update.h -o update.o
movable.o: movable.cpp logger.o update.o 
	g++ -c movable.cpp movable.h -o movable.o
driver.o: driver.cpp movable.o logger.o update.o
	g++ -c driver.cpp -o driver.o

clean:
	$(RM) count *.o *~

run:	driver.out
	./driver.out
