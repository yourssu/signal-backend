package com.yourssu.signal.domain.profile.implement

import com.yourssu.signal.domain.profile.implement.exception.AnimalNotFoundException

enum class Animal {
    CAT, DOG, HAMSTER, BEAR, DINOSAUR, RABBIT, DEER, TURTLE, FOX;

    companion object {
        fun of(value: String): Animal {
            return entries.find { it.name == value.uppercase() }
                ?: throw AnimalNotFoundException()
        }
    }
}
