#include <jni.h>
#include "com_example_Main.h"  // убедитесь, что путь к заголовку правильный

#include <vector>
#include <string>

// Реализация инициализации библиотеки
extern "C" {

JNIEXPORT void JNICALL Java_com_example_Main_initNativeLib(JNIEnv * env, jobject obj) {
    // Здесь можно разместить код инициализации, если нужно
    // Пока оставим пустым
}

// Реализация расчета среднего
JNIEXPORT jdouble JNICALL Java_com_example_Main_calcAverage(JNIEnv * env, jobject obj, jdoubleArray dataArray) {

}

// Реализация расчета максимума
JNIEXPORT jdouble JNICALL Java_com_example_Main_calcMax(JNIEnv * env, jobject obj, jdoubleArray dataArray) {

}

// Реализация подсчета уникальных строк
JNIEXPORT jint JNICALL Java_com_example_Main_countUnique(JNIEnv * env, jobject obj, jobjectArray stringArray) {

}

} // extern "C"