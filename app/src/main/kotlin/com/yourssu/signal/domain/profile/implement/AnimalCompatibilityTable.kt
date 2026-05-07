package com.yourssu.signal.domain.profile.implement

object AnimalCompatibilityTable {

    // 여자 동물상 기준: 내 동물 → 매칭되는 남자 동물
    private val femaleTable = mapOf(
        Animal.CAT     to setOf(Animal.DOG, Animal.BEAR),
        Animal.DOG     to setOf(Animal.CAT, Animal.DINOSAUR),
        Animal.HAMSTER to setOf(Animal.BEAR, Animal.HAMSTER),
        Animal.FOX     to setOf(Animal.DINOSAUR, Animal.DEER),
        Animal.RABBIT  to setOf(Animal.DEER, Animal.DOG),
        Animal.TURTLE  to setOf(Animal.HAMSTER, Animal.CAT),
    )

    // 남자 동물상 기준: 내 동물 → 매칭되는 여자 동물
    private val maleTable = mapOf(
        Animal.CAT      to setOf(Animal.DOG, Animal.TURTLE),
        Animal.DOG      to setOf(Animal.CAT, Animal.RABBIT),
        Animal.HAMSTER  to setOf(Animal.TURTLE, Animal.HAMSTER),
        Animal.DINOSAUR to setOf(Animal.FOX, Animal.DOG),
        Animal.BEAR     to setOf(Animal.HAMSTER, Animal.CAT),
        Animal.DEER     to setOf(Animal.RABBIT, Animal.FOX),
    )

    fun isCompatible(myProfile: Profile, targetProfile: Profile): Boolean =
        if (myProfile.gender == Gender.FEMALE)
            femaleTable[myProfile.animal]?.contains(targetProfile.animal) == true
        else
            maleTable[myProfile.animal]?.contains(targetProfile.animal) == true
}
