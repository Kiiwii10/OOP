#ifndef PART1_OBSERVER_H
#define PART1_OBSERVER_H

template <class T>
class Observer {
public:
    Observer() {}
    virtual void handleEvent(const T& t) = 0;
    virtual ~Observer() {};
};

#endif //PART1_OBSERVER_H
