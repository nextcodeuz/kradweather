cmake_minimum_required(VERSION 3.22.1)

project("kradnative")

add_library(
    kradnative
    SHARED
    kradnative.cpp
)

find_library(
    log-lib
    log
)

target_link_libraries(
    kradnative
    ${log-lib}
)

target_compile_options(kradnative PRIVATE -O2 -fno-exceptions)
