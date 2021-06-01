package com.github.se7_kn8.xcontrolplus

import java.util.logging.*

class ConsoleHandler : Handler() {

    private val formatter = SimpleFormatter()

    private val stdoutHandler = StreamHandler(System.out, formatter)
    private val stderrHandler = StreamHandler(System.err, formatter)

    override fun publish(record: LogRecord) {
        if (record.level.intValue() <= Level.INFO.intValue()) {
            stdoutHandler.publish(record)
            stdoutHandler.flush()
        } else {
            stderrHandler.publish(record)
            stderrHandler.flush()
        }
    }

    override fun flush() {
        stdoutHandler.flush()
        stderrHandler.flush()
    }

    override fun close() {
        stdoutHandler.close()
        stderrHandler.close()
    }

}
