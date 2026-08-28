package com.yourssu.signal.domain.profile.implement

object CelebrityTable {
    private val celebrities = mapOf(
        Gender.MALE to mapOf(
            Animal.BEAR to listOf("마동석", "유지태", "안재홍", "하정우", "윤균상"),
            Animal.DOG to listOf("송중기", "남주혁", "박보검", "최우식", "정해인", "임시완", "도경수", "송강", "백현"),
            Animal.DINOSAUR to listOf("김우빈", "공유", "류준열", "탑", "육성재", "김무열", "위하준"),
            Animal.DEER to listOf("차은우", "진", "이동욱", "원빈", "성현", "지창욱", "황민현", "정국"),
            Animal.CAT to listOf("이종석", "서인국", "우도환", "서강준", "이도현"),
            Animal.WOLF to listOf("카이", "허남준", "최산", "손석구", "주지훈", "이민기"),
        ),
        Gender.FEMALE to mapOf(
            Animal.FOX to listOf("한소희", "김지원", "쯔위", "미연", "지연", "경리", "하늘", "오연서", "박규리", "채령"),
            Animal.RABBIT to listOf("나연", "수지", "김유연", "유나", "정채연", "아이유", "민주", "설윤", "고윤정", "장원영", "이안"),
            Animal.TURTLE to listOf("하연수", "이로하", "예리", "조유리", "제나", "홍은채"),
            Animal.DOG to listOf("박보영", "윈터", "안유진", "지예은", "지수", "츄", "백지헌", "김세정", "민지", "우기"),
            Animal.CAT to listOf("해린", "노제", "카리나", "안소희", "예지", "제니", "영서", "류진", "원이", "이성경"),
            Animal.HAMSTER to listOf("레이", "원희", "최예나", "송하영", "사나", "우기", "모카"),
        ),
    )

    fun get(gender: Gender, animal: Animal): List<String> {
        ProfileValidator.validateAnimal(gender, animal)
        return celebrities.getValue(gender).getValue(animal)
    }
}
