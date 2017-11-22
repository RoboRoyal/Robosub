#include <iostream>
#include "movable.h"
#include "update.h"
#include "logger.h"
using namespace std;

int main() {
  cout << "!!!Hello World!!!" << endl; // prints !!!Hello World!!!
  logger::info("Hell?");
  return 0;
}
