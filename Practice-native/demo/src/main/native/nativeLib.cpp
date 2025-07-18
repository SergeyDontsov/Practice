#include <jni.h>
#include <string>
#include <vector>
#include <unordered_set>
#include <algorithm>
#include <limits>
#include <future>
#include "nlohmann/json.hpp"
#include <execution> // Для параллельных алгоритмов

using json = nlohmann::json;

// Вспомогательная функция для получения значения как double
double get_value_as_double(const json& item, const std::string& field) {
    if (!item.contains(field))
        return -std::numeric_limits<double>::infinity();

    const auto& val = item.at(field);
    if (val.is_number()) return val.get<double>();
    if (val.is_string()) {
        try { return std::stod(val.get<std::string>()); }
        catch (...) { return -std::numeric_limits<double>::infinity(); }
    }
    return -std::numeric_limits<double>::infinity();
}

// Вспомогательная функция для получения группирующего ключа
std::string build_group_key(const json& item, const std::vector<std::string>& group_fields) {
    std::string key;
    for (const auto& field : group_fields) {
        if (item.contains(field))
            key += item[field].dump() + "|";
        else
            key += "null|";
    }
    return key;
}

// Общая функция группировки и обработки
template<typename Func>
json process_grouped(const json& data, const std::vector<std::string>& group_fields, Func func) {
    std::unordered_map<std::string, std::vector<json>> groups;
    for (const auto& item : data) {
        std::string key = build_group_key(item, group_fields);
        groups[key].push_back(item);
    }

    std::vector<std::future<json>> futures;
    for (const auto& [group_key, items] : groups) {
        futures.push_back(std::async(std::launch::async, [group_key, &items, &func]() {
            json res;
            res["group"] = group_key;
            res["result"] = func(items);
            return res;
        }));
    }

    json results = json::array();
    for (auto& fut : futures) {
        results.push_back(fut.get());
    }
    return results;
}

// Вспомогательная функция для получения групповых полей из Java-объекта
std::vector<std::string> getGroupFields(JNIEnv* env, jobject groupFieldsObj) {
    std::vector<std::string> fields;
    if (groupFieldsObj == nullptr) return fields;

    jclass listClass = env->FindClass("java/util/List");
    jmethodID sizeMethod = env->GetMethodID(listClass, "size", "()I");
    jmethodID getMethod = env->GetMethodID(listClass, "get", "(I)Ljava/lang/Object;");
    jint size = env->CallIntMethod(groupFieldsObj, sizeMethod);
    if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); return fields; }

    for (jint i = 0; i < size; ++i) {
        jobject itemObj = env->CallObjectMethod(groupFieldsObj, getMethod, i);
        if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); continue; }
        if (itemObj == nullptr) continue;

        jclass objClass = env->FindClass("java/lang/Object");
        jmethodID toStringMethod = env->GetMethodID(objClass, "toString", "()Ljava/lang/String;");
        jobject itemStrObj = env->CallObjectMethod(itemObj, toStringMethod);
        if (env->ExceptionCheck()) { env->ExceptionDescribe(); env->ExceptionClear(); env->DeleteLocalRef(itemObj); continue; }

        jstring itemStr = (jstring) itemStrObj;
        const char* cstr = env->GetStringUTFChars(itemStr, nullptr);
        if (cstr != nullptr) {
            fields.emplace_back(cstr);
            env->ReleaseStringUTFChars(itemStr, cstr);
        }
        env->DeleteLocalRef(itemStr);
        env->DeleteLocalRef(itemStrObj);
        env->DeleteLocalRef(itemObj);
    }
    return fields;
}

// Общая функция парсинга JSON
json parseJsonOrEmpty(const std::string& str) {
    try { return json::parse(str); }
    catch (...) { return json(); }
}

// Универсальный обработчик
template<typename Func>
json handleData(const std::string& jsonStr, const std::vector<std::string>& group_fields, Func func) {
    json data = parseJsonOrEmpty(jsonStr);
    if (data.is_null()) return json(); 

    if (data.is_array()) {
        if (group_fields.empty()) {
            return func(data);
        } else {
            return process_grouped(data, group_fields, func);
        }
    }
    return json();
}

// Обертка для получения групповых полей из JNI
std::vector<std::string> getGroupFieldsJNI(JNIEnv* env, jobject groupFieldsObj) {
    return getGroupFields(env, groupFieldsObj);
}

extern "C" {

// countUnique
JNIEXPORT jstring JNICALL Java_com_example_Main_countUnique(JNIEnv* env, jobject, jstring jsonStr, jstring fieldStr, jobject groupFieldsObj) {
    const char* json_cstr = env->GetStringUTFChars(jsonStr, nullptr);
    std::string json_input(json_cstr);
    env->ReleaseStringUTFChars(jsonStr, json_cstr);

    const char* field_cstr = env->GetStringUTFChars(fieldStr, nullptr);
    std::string field_name(field_cstr);
    env->ReleaseStringUTFChars(fieldStr, field_cstr);

    auto group_fields = getGroupFieldsJNI(env, groupFieldsObj);

    auto result = handleData(json_input, group_fields, [&](const json& items) -> json {
        std::unordered_set<std::string> unique_vals;
        for (const auto& item : items) {
            if (item.contains(field_name))
                unique_vals.insert(item[field_name].dump());
        }
        return static_cast<int>(unique_vals.size());
    });

    std::string res_str = result.dump();
    return env->NewStringUTF(res_str.c_str());
}

// calculateAverage
JNIEXPORT jstring JNICALL Java_com_example_Main_calculateAverage(JNIEnv* env, jobject, jstring jsonStr, jstring fieldStr, jobject groupFieldsObj) {
    const char* json_cstr = env->GetStringUTFChars(jsonStr, nullptr);
    std::string json_input(json_cstr);
    env->ReleaseStringUTFChars(jsonStr, json_cstr);

    const char* field_cstr = env->GetStringUTFChars(fieldStr, nullptr);
    std::string field_name(field_cstr);
    env->ReleaseStringUTFChars(fieldStr, field_cstr);

    auto group_fields = getGroupFieldsJNI(env, groupFieldsObj);

    auto result = handleData(json_input, group_fields, [&](const json& items) -> json {
        double sum = 0.0;
        int count = 0;
        for (const auto& item : items) {
            if (item.contains(field_name))
                sum += get_value_as_double(item, field_name);
            ++count;
        }
        return (count > 0) ? (sum / count) : 0.0;
    });

    std::string res_str = result.dump();
    return env->NewStringUTF(res_str.c_str());
}

#include <chrono> // подключаем для измерения времени

// calculateMax
JNIEXPORT jstring JNICALL Java_com_example_Main_calculateMax(JNIEnv* env, jobject, jstring jsonStr, jstring fieldStr, jobject groupFieldsObj) {
    
    auto start_time1 = std::chrono::high_resolution_clock::now(); // старт таймера

    const char* json_cstr = env->GetStringUTFChars(jsonStr, nullptr);
    std::string json_input(json_cstr);
    env->ReleaseStringUTFChars(jsonStr, json_cstr);

    auto end_time1 = std::chrono::high_resolution_clock::now(); // конец таймера
    auto duration1 = std::chrono::duration_cast<std::chrono::microseconds>(end_time1 - start_time1
    ).count();

    double seconds1 = duration1 / 1000000.0;
    // Выводим или логируем время
    printf("Часть 1: %.3f seconds\n", seconds1);

    const char* field_cstr = env->GetStringUTFChars(fieldStr, nullptr);
    std::string field_name(field_cstr);
    env->ReleaseStringUTFChars(fieldStr, field_cstr);


    auto group_fields = getGroupFieldsJNI(env, groupFieldsObj);


    auto start_time3 = std::chrono::high_resolution_clock::now(); // старт таймера

    auto result = handleData(json_input, group_fields, [&](const json& items) -> json {
        std::vector<double> values;
        values.reserve(items.size());
        for (const auto& item : items) {
            if (item.contains(field_name))
                values.push_back(get_value_as_double(item, field_name));
            else
                values.push_back(-std::numeric_limits<double>::infinity());
        }
        if (values.empty()) return -std::numeric_limits<double>::infinity();
        return *std::max_element(std::execution::par, values.begin(), values.end());
    });

    auto end_time3 = std::chrono::high_resolution_clock::now(); // конец таймера
    auto duration3 = std::chrono::duration_cast<std::chrono::microseconds>(end_time3 - start_time3).count();

    double seconds3 = duration3 / 1000000.0;
    // Выводим или логируем время
    printf("Часть 3: %.3f seconds\n", seconds3);

    std::string res_str = result.dump();
    return env->NewStringUTF(res_str.c_str());
}

} // extern "C"