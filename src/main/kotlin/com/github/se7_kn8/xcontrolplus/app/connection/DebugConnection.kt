package com.github.se7_kn8.xcontrolplus.app.connection

import com.github.se7_kn8.xcontrolplus.app.context.ApplicationContext
import com.github.se7_kn8.xcontrolplus.app.context.WindowContext
import com.github.se7_kn8.xcontrolplus.protocol.packet.EchoPacket
import javafx.collections.FXCollections
import javafx.geometry.Orientation
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.stage.Stage
import javafx.stage.StageStyle
import jfxtras.styles.jmetro.JMetro
import jfxtras.styles.jmetro.Style

class DebugConnection {

    companion object {
        private val instance by lazy { DebugConnection() }

        fun showWindow() {
            if (!instance.window.isShowing) {
                instance.window.show()
            } else {
                instance.window.toFront()
            }
        }
    }

    val window = Stage()
    val connectionHandler = ApplicationContext.get().connectionHandler

    init {
        window.initOwner(WindowContext.get().primaryStage)
        window.initStyle(StageStyle.UTILITY)
        window.title = "Debug connection"


        val root = VBox(10.0)

        root.children.add(Label("Debug connection").apply { font = Font.font(20.0) })
        root.children.add(Separator(Orientation.HORIZONTAL))
        root.children.add(createEchoPacketRow())
        root.children.add(Separator(Orientation.HORIZONTAL))
        root.children.add(createTrackPowerRow())
        root.children.add(Separator(Orientation.HORIZONTAL))
        root.children.add(createTurnoutRow())
        root.children.add(Separator(Orientation.HORIZONTAL))

        val scene = Scene(root)
        JMetro(Style.LIGHT).scene = scene
        window.scene = scene
    }

    private fun createEchoPacketRow(): Node {
        val name = Label("Echo packet")
        val send = CheckBox("Send")
        val invoke = Button("Invoke")
        invoke.setOnAction {
            if (send.isSelected) {
                connectionHandler.sendPacket(EchoPacket())
            } else {
                connectionHandler.accept(EchoPacket())
            }
        }

        return createRow(name, send, invoke)
    }

    private fun createTrackPowerRow(): Node {
        val name = Label("Track power packet")

        val type = ComboBox(FXCollections.observableArrayList("RESUME", "EMERGENCY_STOP"))
        val send = CheckBox("Send")
        val invoke = Button("Invoke")
        invoke.setOnAction {
            if (send.isSelected) {
                when (type.selectionModel.selectedItem) {
                    "RESUME" -> {
                        connectionHandler.sendPacket(TrackPowerPacket.newResumeRequest())
                    }
                    "EMERGENCY_STOP" -> {
                        connectionHandler.sendPacket(TrackPowerPacket.newEmergencyStopRequest())
                    }
                }
            } else {
                when (type.selectionModel.selectedItem) {
                    "RESUME" -> {
                        connectionHandler.accept(TrackPowerPacket(0x1))
                    }
                    "EMERGENCY_STOP" -> {
                        connectionHandler.accept(TrackPowerPacket(0xAB))
                    }
                }
            }
        }

        return createRow(name, type, send, invoke)
    }

    private fun createTurnoutRow(): Node {
        val name = Label("Turnout packet")
        val address = Spinner<Int>(1, 1024, 1)
        val turn = CheckBox("Turn")
        val send = CheckBox("Send")
        val invoke = Button("Invoke")
        invoke.setOnAction {
            if (send.isSelected) {
                connectionHandler.sendPacket(TurnoutPacket(address.value, if (turn.isSelected) 1 else 0))
            } else {
                connectionHandler.accept(TurnoutPacket(address.value, if (turn.isSelected) 1 else 0))
            }
        }
        return createRow(name, address, turn, send, invoke)
    }


    private fun createRow(vararg nodes: Node): HBox {
        val box = HBox(10.0)

        box.children.addAll(nodes)
        return box
    }


}
