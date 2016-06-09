package me.chaopeng.chaos4g.summer.exceptions

/**
 * me.chaopeng.chaos4g.summer.exceptions.SummerException
 *
 * @author chao
 * @version 1.0 - 2016-06-05
 */
class SummerException extends RuntimeException {

    SummerException(String messaege) {
        super(messaege)
    }

    SummerException(Throwable throwable) {
        super(throwable)
    }

    SummerException(String messaege, Throwable throwable) {
        super(messaege, throwable)
    }
}
