package com.skunity.ducky

import com.beust.klaxon.Klaxon
import com.skunity.ducky.commands.CmdDoMath
import net.dv8tion.jda.core.AccountType
import net.dv8tion.jda.core.JDABuilder
import net.dv8tion.jda.core.entities.MessageChannel
import java.io.File
import java.time.LocalDateTime
import kotlin.concurrent.timerTask

object Ducky {
    val startTime = LocalDateTime.now()

    val config = Klaxon().parse<BotConfig>(File("config.json"))!!
    val jda = JDABuilder(AccountType.valueOf(Ducky.config.accountType))
            .setToken(Ducky.config.token)
            .addEventListener(DuckyListener)
            .buildBlocking()

    var typingChannelToEndTime = emptyMap<MessageChannel, Long>()

    init {
        val data = readData<BotData>(null, "ducky")() ?: BotData()
        data.startCount++
        saveData(null, "ducky", data)

        // Here we schedule resending the "typing" messages to Discord,
        // when typing in some channel is not completed within 7 seconds
        timer.scheduleAtFixedRate(timerTask {
            val now = System.currentTimeMillis()
            typingChannelToEndTime.forEach { (channel, endTime) ->
                if (endTime < now) {
                    Ducky.typingChannelToEndTime -= channel
                } else {
                    channel.sendTyping().queue()
                }
            }
        }, 100, 7000)

        Thread {
            CmdDoMath // load the math library asynchronously
        }.start()
    }
}

/**
 * The main function which does nothing on its own, just initializes [Ducky]
 */
fun main(args: Array<String>) {
    Ducky
}
