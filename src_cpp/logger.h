/*
 * logger.h
 *
 *  Created on: Oct 31, 2017
 *      Author: Dakota
 */

#ifndef LOGGER_H_
#define LOGGER_H_
#include <stdio.h>

class logger {
public:
	logger();
	static void info(char *tmp);
	static void error(char *tmp);
	virtual ~logger();
};

#endif /* LOGGER_H_ */
