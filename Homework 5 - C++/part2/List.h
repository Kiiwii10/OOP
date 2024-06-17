#ifndef PART2_OOP5LISTH
#define PART2_OOP5LISTH

//========================================
//==================List==================
//========================================
template<typename... TT>
struct List{};

template <typename T, typename... TT>
struct List<T, TT...>{
    typedef T head;
    typedef List<TT...> next;
    constexpr static int size = sizeof...(TT) + 1;
};

template<>
struct List<>{
    constexpr static int size = 0;
};

//=========================================
//===============PrependList===============
//=========================================

template<typename V, typename... TT>
struct PrependList{};

template <typename V, typename ...TT>
struct PrependList<V, List<TT...>>{
    typedef List<V, TT...> list;
};

template<typename V>
struct PrependList<V>{
    typedef List<V> list;
};

// for the following structs we assume that the N indecies will be in range.

//=========================================
//===============GetAtIndex================
//=========================================

template<int N, typename T>
struct GetAtIndex{
    // typedef T value;
};

template<int N, typename T, typename... TT>
struct GetAtIndex<N, List<T, TT...>>{
    typedef typename GetAtIndex<N-1, List<TT...>>::value value;
};

template<typename T, typename... TT>
struct GetAtIndex<0, List<T, TT...>> {
    typedef T value;
};

//=========================================
//===============SetAtIndex================
//=========================================


template<int N, typename V, typename TT>
struct SetAtIndex{
    // typedef List<> list;
};

template<int N, typename V, typename T, typename... TT>
struct SetAtIndex<N, V, List<T, TT...>>{
    typedef typename PrependList<T, typename SetAtIndex<N-1, V, List<TT...>>::list>::list list;
};

template<typename V, typename T, typename... TT>
struct SetAtIndex<0, V, List<T, TT...>> {
    typedef List<V, TT...> list;
};

#endif // PART2_OOP5LISTH