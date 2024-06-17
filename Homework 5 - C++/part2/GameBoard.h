#ifndef PART2_OOP5GAMEBOARDH
#define PART2_OOP5GAMEBOARDH

#include "BoardCell.h"
#include "List.h"

template<typename List>
struct GameBoard
{
    typedef List board;
    constexpr static int length = List::size;       // Number of rows
    constexpr static int width = List::head::size;  // Number of cols
};


#endif // PART2_OOP5GAMEBOARDH