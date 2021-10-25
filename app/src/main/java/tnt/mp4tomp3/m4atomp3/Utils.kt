package tnt.mp4tomp3.m4atomp3

import java.security.SecureRandom

class Utils {

    private val charList = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz"

    private val rnd = SecureRandom()

    fun randomString(len: Int): String {
        val sb = StringBuilder(len)
        for (i in 0 until len) {
            sb.append(charList[(rnd.nextInt(charList.length))])
        }
        return sb.toString()
    }


}