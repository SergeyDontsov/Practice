#include <jni.h>
#include <unordered_set>
#include <string>

extern "C" {

JNIEXPORT void JNICALL Java_com_example_Main_initNativeLib(JNIEnv * env, jobject obj) {
    // Инициализация, если нужна
}

JNIEXPORT jdouble JNICALL Java_com_example_Main_calcAverage(JNIEnv * env, jobject obj, jdoubleArray dataArray) {
    jsize length = env->GetArrayLength(dataArray);
    if (length == 0) return 0;

    jboolean isCopy;
    auto data = reinterpret_cast<jdouble*>(env->GetPrimitiveArrayCritical(dataArray, &isCopy));
    if (data == nullptr) return 0;

    double sum = 0;
    for (jsize i = 0; i < length; ++i) {
        sum += data[i];
    }

    env->ReleasePrimitiveArrayCritical(dataArray, data, 0);
    return sum / length;
}

JNIEXPORT jdouble JNICALL Java_com_example_Main_calcMax(JNIEnv * env, jobject obj, jdoubleArray dataArray) {
    jsize length = env->GetArrayLength(dataArray);
    if (length == 0) return 0;

    jboolean isCopy;
    auto data = reinterpret_cast<jdouble*>(env->GetPrimitiveArrayCritical(dataArray, &isCopy));
    if (data == nullptr) return 0;

    double maxVal = data[0];
    for (jsize i = 1; i < length; ++i) {
        if (data[i] > maxVal) maxVal = data[i];
    }

    env->ReleasePrimitiveArrayCritical(dataArray, data, 0);
    return maxVal;
}

JNIEXPORT jint JNICALL Java_com_example_Main_countUnique(JNIEnv * env, jobject obj, jobjectArray stringArray) {
    std::unordered_set<std::string> set;
    jsize length = env->GetArrayLength(stringArray);

    for (jsize i = 0; i < length; ++i) {
        jstring jstr = (jstring) env->GetObjectArrayElement(stringArray, i);
        if (jstr == nullptr) continue; // Защита от null
        const char* strChars = env->GetStringUTFChars(jstr, NULL);
        if (strChars != nullptr) {
            set.insert(strChars);
            env->ReleaseStringUTFChars(jstr, strChars);
        }
        env->DeleteLocalRef(jstr);
    }

    return static_cast<jint>(set.size());
}

} // extern "C"