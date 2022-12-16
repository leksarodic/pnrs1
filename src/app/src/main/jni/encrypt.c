//
// Created by aleksa on 30.5.18..
//

// INFO
// https://www3.ntu.edu.sg/home/ehchua/programming/java/JavaNativeInterface.html

#include "encrypt.h"

JNIEXPORT jstring JNICALL Java_rodic_aleksa_miberchatapplication_EncryptMessage_encryptDecrypt (JNIEnv *env, jobject jo, jstring input_string, jint input_len)
{

    int i;
    char KEY[] = "This is the key";
    int KEL_LEN = 15;

    // Input string -> char*
    const jchar *in_message = (*env)->GetStringChars(env, input_string, NULL);
    if (NULL == in_message) return NULL;

    // Output char*
    jchar out_message[input_len];

    // Crypt/decrypt
    for(i = 0; i < input_len + 1; i++){
        out_message[i] = in_message[i] ^ KEY[i % KEL_LEN];
    }

    //(*env)->ReleaseStringUTFChars(env, input_string, in_message);

    return (*env)->NewString(env, out_message, input_len);

}