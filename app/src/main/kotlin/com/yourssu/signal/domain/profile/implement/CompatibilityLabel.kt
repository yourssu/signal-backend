package com.yourssu.signal.domain.profile.implement

enum class CompatibilityLabel(val message: String?) {
    PERFECT_MATCH("이 사람 운명일지도..? 시그널이 추천하는 환상의 궁합"),
    MBTI_AGE_MATCH("이 사람 운명일지도..? 생각의 주파수가 같은 사이"),
    ANIMAL_AGE_MATCH("이 사람 운명일지도..? 외적인 주파수가 맞는 사이"),
    MBTI_MATCH(null),
    ANIMAL_MATCH(null),
    AGE_MATCH(null),
}
