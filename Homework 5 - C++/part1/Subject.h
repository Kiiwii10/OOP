#ifndef PART1_OOP5SUBJECTH
#define PART1_OOP5SUBJECTH

#include "Observer.h"
#include "OOP5EventException.h"
#include <vector>
#include <algorithm>

template<class T>
class Subject {
    private:
        std::vector<Observer<T>*> observers;

    public:

        Subject() =default;
        ~Subject() = default;

        void notify(const T& param){
            for (Observer<T>* obs : observers){ // TODO - use iterators?
                obs->handleEvent(param);
            }
        }
        
        void addObserver(Observer<T> &obs) {
            if (std::find(observers.begin(), observers.end(), &obs) != observers.end()){
                throw ObserverAlreadyKnownToSubject();
            }
            observers.push_back(&obs);  
        }
        
        void removeObserver(Observer<T>& obs) {
            auto iterator = std::find(observers.begin(), observers.end(), &obs);
            if(observers.empty() || iterator == observers.end()){
                throw ObserverUnknownToSubject();
            }
            observers.erase(iterator); // Needs the iterator to the observer to remove
        }

        Subject<T>& operator+=(Observer<T>& obs){
            addObserver(obs);
            return *this;
        }

        Subject<T>& operator-=(Observer<T>& obs){
            removeObserver(obs);
            return *this;
        }

        Subject<T>& operator()(const T& param){
            notify(param);
            return *this;
        
        }
};

#endif //PART1_OOP5SUBJECTH