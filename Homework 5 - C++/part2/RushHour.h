#ifndef PART2_OOP5RUSHHOURH
#define PART2_OOP5RUSHHOURH

#include "BoardCell.h"
#include "CellType.h"
#include "Direction.h"
#include "GameBoard.h"
#include "MoveVehicle.h"
#include "List.h"
#include "Utilities.h"


//=======================================
//================FindCar================
//=======================================

// Search for the car in current row.
template<typename G, int R, int C, CellType Car>
struct FindCar {
    typedef typename GetCell<R, C, G>::cell cell;
    constexpr static bool found = cell::type == Car;
    typedef typename ConditionalAndValue<found, Position<R, C>, FindCar<G, R, C - 1, Car>>::value value;
};

// Finished searching the current row, move to the next row.
template<typename G, int R, CellType Car>
struct FindCar<G, R, -1, Car> {
    typedef typename FindCar<G, R - 1, G::width - 1, Car>::value value;
};

// Car not found on the board - shouldn't get here.
template<typename G, CellType Car>
struct FindCar<G, 0, -1, Car> {
    static_assert(Car > R, "Car not found on the board.");
};

//=======================================
//===========CheckWinCondition===========
//=======================================

template<typename G, int R, int C>
struct CheckWinCondition {
    typedef typename GetCell<R, C, G>::cell cell;
    constexpr static bool can_move = (cell::type == X || cell::type == EMPTY);
    constexpr static bool reached_end = C + 1 >= G::width;

    typedef typename ConditionalAndValue<can_move && reached_end,                               // if red car reached the end
                                        BoolValueWrapper<true>,                                 // return true
                                        ConditionalAndValue<can_move,                           // else if red car can move, but is not at the end yet
                                                            CheckWinCondition<G, R, C + 1> ,    // go check the next cell
                                                            BoolValueWrapper<false>             // else - red car is still blocked return false
                                        >
                    >::value value;
};

//======================================
//===============CheckWin===============
//======================================

template<typename G>
struct CheckWin {
    typedef typename FindCar<G, G::length - 1, G::width - 1, X>::value red_car_location;                // Find red car
    typedef typename CheckWinCondition<G, red_car_location::row, red_car_location::col>::value value;   // check if it can go to the exit
    constexpr static bool result = value::result;
};

//=======================================
//===============ExecMoves===============
//=======================================

template <typename Board, typename MoveList>
struct ExecMoves;

// Recursive case: Apply the current move and proceed to the next.
template <typename Board, typename Move, typename... Rest>
struct ExecMoves<Board, List<Move, Rest...>> {
    typedef typename FindCar<Board, Board::length - 1, Board::width - 1, Move::type>::value car_location;                       // Find car that needs to move
    typedef typename MoveVehicle<Board, car_location::row, car_location::col, Move::direction, Move::amount>::board next_board; // Move car in given direction  
    typedef typename ExecMoves<next_board, List<Rest...>>::board board;                                                         // Do rest of moves
};

// Base case: No more moves to apply.
template <typename Board>
struct ExecMoves<Board, List<>> {
    typedef Board board;
};

//=======================================
//=============CheckSolution=============
//=======================================


template<typename G, typename Moves>
struct CheckSolution {
    typedef typename ExecMoves<G, Moves>::board final_board;
    typedef typename CheckWin<final_board>::value result_wrapper;
    constexpr static bool result = result_wrapper::result;
};

#endif // PART2_OOP5RUSHHOURH

