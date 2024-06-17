#ifndef PART2_OOP5UTILITIESH
#define PART2_OOP5UTILITIESH

//=============================================
//=================Conditional=================
//=============================================
template<bool b, typename T, typename F>
struct Conditional{
    typedef T value;
};

template<typename T, typename F>
struct Conditional<false, T, F> {
    typedef F value;
};

//==============================================
//==============ConditionalInteger==============
//==============================================
template<bool b, int T, int F>
struct ConditionalInteger{
    constexpr static int value = T;
};

template<int T, int F>
struct ConditionalInteger<false, T, F> {
    constexpr static int value = F;
};

//==============================================
//===============ConditionalUtils===============
//==============================================

template<bool, typename T, typename F>
struct ConditionalAndValue {
    typedef typename T::value value;
};
template<typename T, typename F>
struct ConditionalAndValue<false, T, F> {
    typedef typename F::value value;
};

template <bool B>
struct BoolValueWrapper {
    typedef BoolValueWrapper<B> value;
    constexpr static bool result = B;
};

#endif // PART2_OOP5UTILITIESH