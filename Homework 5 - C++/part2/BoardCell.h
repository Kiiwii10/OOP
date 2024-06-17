#ifndef PART2_OOP5BOARDCELLH
#define PART2_OOP5BOARDCELLH

#include "CellType.h"
#include "Direction.h"

template<CellType Type, Direction Dir, int Length>
struct BoardCell{
    constexpr static CellType type = Type;
    constexpr static Direction direction = Dir; // car direction
    constexpr static int length = Length;       // car length
};

#endif // PART2_OOP5BOARDCELLH