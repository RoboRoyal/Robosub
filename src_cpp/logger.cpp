/*
 * logger.cpp
 *
 *  Created on: Oct 31, 2017
 *      Author: Dakota
 */

#include "logger.h"

logger::logger() {
	// TODO Auto-generated constructor stub

}
void info(char * tmp){
  printf("Given to logger info");
  printf(tmp);
}
void error(char * tmp){
  printf("Given to logger error");
  printf(tmp);

}
logger::~logger() {
	// TODO Auto-generated destructor stub
}

