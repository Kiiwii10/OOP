#ifndef PART2_OOP5MOVEVEHICLEH
#define PART2_OOP5MOVEVEHICLEH

#include "Direction.h"
#include "CellType.h"
#include "GameBoard.h"
#include "Utilities.h"
#include "TransposeList.h"

//======================================
//=================Move=================
//======================================

template<CellType Type, Direction Dir, int Amount>
struct Move {
    static_assert(Type != EMPTY, "Cell is empty - nothing to move");
    constexpr static CellType type = Type;
    constexpr static Direction direction = Dir;
    constexpr static int amount = Amount;
};


//=======================================
//===============Utilities===============
//=======================================

template<int R, int C, typename G>
struct GetCell{
    typedef typename GetAtIndex<C,typename GetAtIndex<R,typename G::board>::value>::value cell;
};

template<int R, int C>
struct Position{
    constexpr static int row = R;
    constexpr static int col = C;
    typedef Position<R, C> value;
};


template<typename G, int R, int C, Direction Dir>
struct GetFront {
    // After transposition, treat all movements as horizontal.
    constexpr static int col = C + (Dir == RIGHT) - (Dir == LEFT);

    // check we are in board bounds!
    static_assert(R >= 0, "Out of bounds: R < 0 ");
    static_assert(col >= 0, "Out of bounds: col < 0 ");
    static_assert(col < G::width, "Out of bounds: col >= G::width ");
    static_assert(R < G::length, "Out of bounds: R >= G::length ");

    typedef typename GetCell<R, C, G>::cell old_cell;
    typedef typename GetCell<R, col, G>::cell new_cell;
    // Can we go there?
    static_assert(old_cell::type == new_cell::type || new_cell::type == EMPTY, "Path blocked!");

    constexpr static bool found_front = new_cell::type == EMPTY;
    typedef Position<R, col> curr_pos;
    // Return the first empty cell in going direction.
    typedef typename ConditionalAndValue<found_front, curr_pos, GetFront<G, R, col, Dir>>::value value;
};

template<typename G, int R, int C, Direction Dir>
struct GetBack {
    // After transposition, treat all movements as horizontal.
    constexpr static int try_col = C - (Dir == RIGHT) + (Dir == LEFT);
    constexpr static int col = (try_col < 0 || try_col >= G::width) ? C : try_col;

    // check we are in board bounds!
    static_assert(R >= 0, "Out of bounds: R < 0 ");
    static_assert(col >= 0, "Out of bounds: col < 0 ");
    static_assert(col < G::width, "Out of bounds: col >= G::width ");
    static_assert(R < G::length, "Out of bounds: R >= G::length ");
    
    typedef typename GetCell<R, C, G>::cell old_cell;
    typedef typename GetCell<R, col, G>::cell new_cell;

    constexpr static bool found_back = (new_cell::type != old_cell::type) || (col == C);
    typedef Position<R, C> curr_pos;
    // Return the rear cell of the car.
    typedef typename ConditionalAndValue<found_back, curr_pos, GetBack<G, R, col, Dir>>::value value;
};

template<typename G, int R, int C, typename BoardCell>
struct SetCell {
    typedef typename GetAtIndex<R, typename G::board>::value line_to_fix;           // Get line to fix
    typedef typename SetAtIndex<C, BoardCell, line_to_fix>::list line;              // Set the cell in the line
    typedef typename SetAtIndex<R, line, typename G::board>::list updated_lists;    // Set the line in the board
    typedef GameBoard<updated_lists> game_board;                                    // Create the new board 
};


template<typename G, typename front, typename back, typename cell>
struct SetCells {
    typedef typename SetCell<G, front::row, front::col, cell>::game_board new_board;                              // Set the front cell
    typedef typename SetCell<new_board, back::row, back::col, BoardCell<EMPTY, UP, 0>>::game_board game_board;    // Set the back cell
};

template<typename G, int R, int C, Direction Dir>
struct SingleStep {
    typedef typename GetFront<G, R, C, Dir>::value front;
    typedef typename GetBack<G, R, C, Dir>::value back;
    typedef typename GetCell<R, C, G>::cell cell;

    // Fill the cells with EMPTY and the new car.
    typedef typename SetCells<G, front, back, cell>::game_board game_board;
};


template<typename G, int R, int C, Direction Dir, int A>
struct MoveOnBoard {
    
    typedef typename GetFront<G, R, C, Dir>::value next_step;
    typedef typename Position<R, C>::value current_step;
    typedef typename SingleStep<G, current_step::row, current_step::col, Dir>::game_board new_game_board;
    // Recursively move the car.
    typedef typename MoveOnBoard<new_game_board, next_step::row, next_step::col, Dir, A - 1>::Game_board Game_board;
};

template<typename G, int R, int C, Direction Dir>
struct MoveOnBoard<G, R, C, Dir, 0> {
    typedef G Game_board;
};

//=======================================
//==============MoveVehicle==============
//=======================================

template<typename G, int R, int C, Direction Dir, int A>
struct MoveVehicle {
    // Check legal cell
    static_assert(R <= G::length, "Row out of board bounds at the start");
    static_assert(C <= G::width, "Column out of board bounds at the start");
    typedef typename GetCell<R, C, G>::cell cell;
    static_assert(cell::type != EMPTY, "No car to move");

    // Check legal direction
    constexpr static bool legal_direction = (Dir == UP || Dir == DOWN) ?
                                            (cell::direction == UP || cell::direction == DOWN) :
                                            (cell::direction == LEFT || cell::direction == RIGHT);
    static_assert(legal_direction, "Invalid direction for car movement");

    // Check move direction and transpose if necessary
    constexpr static bool needs_transpose = (Dir == UP || Dir == DOWN);
    // Adjust row/column indices and direction for transposed board movement.
    constexpr static int new_R = needs_transpose ? C : R;
    constexpr static int new_C = needs_transpose ? R : C;
    constexpr static Direction new_Dir = (Dir == UP) ? LEFT : ((Dir == DOWN) ? RIGHT : Dir);
    // Transpose board if moving vertically, then treat UP/DOWN as LEFT/RIGHT.
    typedef typename Transpose<typename G::board>::matrix tranposed_board;
    typedef typename Conditional<needs_transpose, tranposed_board, typename G::board>::value new_board;
    typedef GameBoard<new_board> new_G;

    // Do the moves
    typedef typename MoveOnBoard<new_G, new_R, new_C, new_Dir, A>::Game_board moved_board;

    // Transpose back if we transposed initially.
    typedef typename Transpose<typename moved_board::board>::matrix tranposed_back;
    typedef typename Conditional<needs_transpose, GameBoard<tranposed_back>, moved_board>::value board;
};

#endif // PART2_OOP5MOVEVEHICLEH