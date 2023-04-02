package com.nima.bluetoothchatapp

import com.nima.bluetoothchatapp.message.Message_da_gui
import com.nima.bluetoothchatapp.message.MessageStatus
import junit.framework.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class MessageHeaderTest {
    private lateinit var randoms : List<String>
    @Before
    fun setup(){
        val generator  = RandomUIDGenerator()
        randoms = generator.generate(12)
    }
    @Test
    fun decodeStringToMessageAck(){
        assertEquals("00${randoms[0]}salam".decode(),Message_da_gui(true, MessageStatus.TrangThai_khongco_Thu(), randoms[0],"salam"))
        assertEquals("12${randoms[1]}".decode(),Message_da_gui(false, MessageStatus.TrangThai_Thu_Da_Seen(), randoms[1],""))
        assertEquals("02${randoms[2]}s".decode(),Message_da_gui(true, MessageStatus.TrangThai_Thu_Da_Seen(), randoms[2],"s"))
        assertEquals("10${randoms[3]}dddddddddddddddddddddddd".decode(),Message_da_gui(false, MessageStatus.TrangThai_khongco_Thu(), randoms[3],"dddddddddddddddddddddddd"))
        assertEquals("00${randoms[4]}1111".decode(),Message_da_gui(true, MessageStatus.TrangThai_khongco_Thu(), randoms[4],"1111"))
        assertEquals("11${randoms[5]}a.fasfadsf.".decode(),Message_da_gui(false, MessageStatus.TrangThai_Thu_Da_Gui(), randoms[5],"a.fasfadsf."))
    }

    @Before
    fun createRandomNumbers(){
        val generator  = RandomUIDGenerator()
        randoms = generator.generate(12)
    }
    @Test
    fun encodeMessageAckToString(){
        assertEquals("00${randoms[0]}nima",Message_da_gui(true,MessageStatus.TrangThai_khongco_Thu(),randoms[0],"nima").encode())
        assertEquals("12${randoms[1]}abdpoor",Message_da_gui(false,MessageStatus.TrangThai_Thu_Da_Seen(),randoms[1],"abdpoor").encode())
        assertEquals("01${randoms[2]}",Message_da_gui(true,MessageStatus.TrangThai_Thu_Da_Gui(),randoms[2],"").encode())
        assertEquals("10${randoms[3]}.",Message_da_gui(false,MessageStatus.TrangThai_khongco_Thu(),randoms[3],".").encode())
        assertEquals("01${randoms[4]}aldsjflasfdjlasdkf",Message_da_gui(true,MessageStatus.TrangThai_Thu_Da_Gui(),randoms[4],"aldsjflasfdjlasdkf").encode())
        assertEquals("12${randoms[5]}_alsfj",Message_da_gui(false,MessageStatus.TrangThai_Thu_Da_Seen(),randoms[5],"_alsfj").encode())
        assertEquals("02${randoms[6]}",Message_da_gui(true,MessageStatus.TrangThai_Thu_Da_Seen(),randoms[6],"").encode())
        assertEquals("12${randoms[7]}",Message_da_gui(false,MessageStatus.TrangThai_Thu_Da_Seen(),randoms[7],"").encode())
        assertEquals("00${randoms[8]}cddald",Message_da_gui(true,MessageStatus.TrangThai_khongco_Thu(),randoms[8],"cddald").encode())
        assertEquals("12${randoms[9]}nima",Message_da_gui(false,MessageStatus.TrangThai_Thu_Da_Seen(),randoms[9],"nima").encode())
        assertEquals("01${randoms[10]}v",Message_da_gui(true,MessageStatus.TrangThai_Thu_Da_Gui(),randoms[10],"v").encode())
        assertEquals("11${randoms[11]}salama",Message_da_gui(false,MessageStatus.TrangThai_Thu_Da_Gui(),randoms[11],"salama").encode())
    }
}