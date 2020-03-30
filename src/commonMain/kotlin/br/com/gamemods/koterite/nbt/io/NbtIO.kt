@file:JvmName("_NbtIO_Internal")
@file:Suppress("unused")

package br.com.gamemods.koterite.nbt.io

import br.com.gamemods.koterite.annotation.Throws
import br.com.gamemods.koterite.annotation.UnstableDefault
import br.com.gamemods.nbtmanipulator.*
import io.ktor.utils.io.core.*
import io.ktor.utils.io.errors.IOException
import kotlin.jvm.JvmName
import kotlin.jvm.JvmStatic
import kotlin.reflect.KClass

/**
 * Contains useful methods do read and write [NbtFile] from [File] and [InputStream]/[OutputStream].
 */
object NbtIO {
    /**
     * Writes the [NbtFile] in the stream.
     * @param outputStream The stream that the file will be written
     * @param file The file that will be written on the stream
     * @param compressed If the file will be compressed by [GZIPOutputStream].
     */
    @JvmStatic
    @Throws(IOException::class)
    @UnstableDefault
    @ExperimentalUnsignedTypes
    fun writeNbtFile(outputStream: Output, file: NbtFile, compressed: Boolean = true, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN) {
        val tag = file.tag
        val typeId = tag.typeId
        val serializer = byteOrder.serializers[typeId]
        val output = if (compressed) TODO() /*GZIPOutputStream(outputStream)*/ else outputStream

        output.writeByte(typeId.toByte())
        output.writeUTF(file.name, byteOrder)

        serializer.writeTag(output, tag)
        output.flush()
        /*TODO if (output is GZIPOutputStream) {
            output.finish()
        }*/
    }

//TODO
//    /**
//     * Writes the [NbtFile] in a [File].
//     * @param file The output file
//     * @param file The NBT file that will be written on the output file
//     * @param compressed If the file will be compressed by [GZIPOutputStream]
//     */
//    @JvmStatic
//    @Throws(IOException::class)
//    fun writeNbtFile(file: File, tag: NbtFile, compressed: Boolean = true) {
//        file.outputStream().buffered().use { writeNbtFile(it, tag, compressed); it.flush() }
//    }

    /**
     * Read a [NbtFile] from the [InputStream].
     * @param inputStream The input stream that will be read
     * @param compressed If the file needs to be decompressed by [GZIPInputStream]
     */
    @JvmStatic
    @Throws(IOException::class)
    @UnstableDefault
    @ExperimentalUnsignedTypes
    fun readNbtFile(inputStream: Input, compressed: Boolean = true, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): NbtFile {
        val input = if (compressed) TODO()/*GZIPInputStream(inputStream)*/ else inputStream

        val typeId = input.readByte().toInt()
        val serializer = byteOrder.serializers[typeId]

        val name = input.readUTF(byteOrder)

        return NbtFile(name, serializer.readTag(input))
    }

//TODO
//    /**
//     * Read a [NbtFile] from a [File].
//     * @param file The input file that will be read
//     * @param compressed If the file needs to be decompressed by [GZIPInputStream]
//     */
//    @JvmStatic
//    @Throws(IOException::class)
//    fun readNbtFile(file: File, compressed: Boolean = true): NbtFile {
//        return file.inputStream().buffered().use { readNbtFile(it, compressed) }
//    }
}


@ExperimentalUnsignedTypes
private val bigEndianSerializers = listOf(
    NbtEndSerial,
    NbtByteSerial,
    NbtShortSerial(ByteOrder.BIG_ENDIAN),
    NbtIntSerial(ByteOrder.BIG_ENDIAN),
    NbtLongSerial(ByteOrder.BIG_ENDIAN),
    NbtFloatSerial(ByteOrder.BIG_ENDIAN),
    NbtDoubleSerial(ByteOrder.BIG_ENDIAN),
    NbtByteArraySerial(ByteOrder.BIG_ENDIAN),
    NbtStringSerial(ByteOrder.BIG_ENDIAN),
    NbtListSerial(ByteOrder.BIG_ENDIAN),
    NbtCompoundSerial(ByteOrder.BIG_ENDIAN),
    NbtIntArraySerial(ByteOrder.BIG_ENDIAN),
    NbtLongArraySerial(ByteOrder.BIG_ENDIAN)
)

@ExperimentalUnsignedTypes
private val littleEndianSerializers = listOf(
    NbtEndSerial,
    NbtByteSerial,
    NbtShortSerial(ByteOrder.LITTLE_ENDIAN),
    NbtIntSerial(ByteOrder.LITTLE_ENDIAN),
    NbtLongSerial(ByteOrder.LITTLE_ENDIAN),
    NbtFloatSerial(ByteOrder.LITTLE_ENDIAN),
    NbtDoubleSerial(ByteOrder.LITTLE_ENDIAN),
    NbtByteArraySerial(ByteOrder.LITTLE_ENDIAN),
    NbtStringSerial(ByteOrder.LITTLE_ENDIAN),
    NbtListSerial(ByteOrder.LITTLE_ENDIAN),
    NbtCompoundSerial(ByteOrder.LITTLE_ENDIAN),
    NbtIntArraySerial(ByteOrder.LITTLE_ENDIAN),
    NbtLongArraySerial(ByteOrder.LITTLE_ENDIAN)
)

@ExperimentalUnsignedTypes
private val ByteOrder.serializers
    get() = if(this == ByteOrder.BIG_ENDIAN) bigEndianSerializers else littleEndianSerializers

@ExperimentalUnsignedTypes
private val NbtTag.typeId
    get() = bigEndianSerializers.indexOfFirst { it.kClass == this::class }

private sealed class NbtSerial<T: NbtTag>(val kClass: KClass<T>, val byteOrder: ByteOrder){
    @ExperimentalUnsignedTypes
    inline val serializers get() = byteOrder.serializers

    abstract fun readTag(input: Input): T
    abstract fun writeTag(output: Output, tag: T)

    @Suppress("UNCHECKED_CAST")
    @JvmName("writeRawTag")
    fun writeTag(output: Output, tag: NbtTag) {
        writeTag(output, tag as T)
    }
}

private object NbtEndSerial: NbtSerial<NbtEnd>(NbtEnd::class, ByteOrder.BIG_ENDIAN) {
    override fun readTag(input: Input): NbtEnd {
        return NbtEnd
    }

    override fun writeTag(output: Output, tag: NbtEnd) {
    }
}

private object NbtByteSerial: NbtSerial<NbtByte>(NbtByte::class, ByteOrder.BIG_ENDIAN) {
    override fun readTag(input: Input): NbtByte {
        return NbtByte(input.readByte())
    }

    override fun writeTag(output: Output, tag: NbtByte) {
        output.writeByte(tag.signed)
    }
}

private class NbtShortSerial(byteOrder: ByteOrder): NbtSerial<NbtShort>(NbtShort::class, byteOrder) {
    override fun readTag(input: Input): NbtShort {
        return NbtShort(input.readShort(byteOrder))
    }

    override fun writeTag(output: Output, tag: NbtShort) {
        output.writeShort(tag.value, byteOrder)
    }
}

private class NbtIntSerial(byteOrder: ByteOrder): NbtSerial<NbtInt>(NbtInt::class, byteOrder) {
    override fun readTag(input: Input): NbtInt {
        return NbtInt(input.readInt(byteOrder))
    }

    override fun writeTag(output: Output, tag: NbtInt) {
        output.writeInt(tag.value, byteOrder)
    }
}

private class NbtLongSerial(byteOrder: ByteOrder): NbtSerial<NbtLong>(NbtLong::class, byteOrder) {
    override fun readTag(input: Input): NbtLong {
        return NbtLong(input.readLong(byteOrder))
    }

    override fun writeTag(output: Output, tag: NbtLong) {
        output.writeLong(tag.value, byteOrder)
    }
}

private class NbtFloatSerial(byteOrder: ByteOrder): NbtSerial<NbtFloat>(NbtFloat::class, byteOrder) {
    override fun readTag(input: Input): NbtFloat {
        return NbtFloat(input.readFloat(byteOrder))
    }

    override fun writeTag(output: Output, tag: NbtFloat) {
        output.writeFloat(tag.value, byteOrder)
    }
}

private class NbtDoubleSerial(byteOrder: ByteOrder): NbtSerial<NbtDouble>(NbtDouble::class, byteOrder) {
    override fun readTag(input: Input): NbtDouble {
        return NbtDouble(input.readDouble(byteOrder))
    }

    override fun writeTag(output: Output, tag: NbtDouble) {
        output.writeDouble(tag.value, byteOrder)
    }
}

private class NbtByteArraySerial(byteOrder: ByteOrder): NbtSerial<NbtByteArray>(NbtByteArray::class, byteOrder) {
    override fun readTag(input: Input): NbtByteArray {
        val size = input.readInt(byteOrder)
        val bytes = ByteArray(size)
        input.readFully(bytes)
        return NbtByteArray(bytes)
    }

    override fun writeTag(output: Output, tag: NbtByteArray) {
        output.writeInt(tag.value.size, byteOrder)
        output.writeFully(tag.value)
    }
}

@ExperimentalUnsignedTypes
private class NbtStringSerial(byteOrder: ByteOrder): NbtSerial<NbtString>(NbtString::class, byteOrder) {
    override fun readTag(input: Input): NbtString {
        return NbtString(input.readUTF(byteOrder))
    }

    override fun writeTag(output: Output, tag: NbtString) {
        output.writeUTF(tag.value, byteOrder)
    }
}

@ExperimentalUnsignedTypes
private class NbtListSerial(byteOrder: ByteOrder): NbtSerial<NbtList<*>>(NbtList::class, byteOrder) {
    override fun readTag(input: Input): NbtList<*> {
        val type = input.readByte().toInt()
        val size = input.readInt(byteOrder)
        if (type == 0 && size > 0) {
            error("Missing type on NbtList")
        }
        val serializer = serializers[type]
        val list = mutableListOf<NbtTag>()
        for (i in 1..size) {
            list += serializer.readTag(input)
        }
        return NbtList(list)
    }

    override fun writeTag(output: Output, tag: NbtList<*>) {
        val sample = tag.firstOrNull() ?: NbtEnd
        val typeId = sample.typeId
        val serializer = serializers[typeId]

        if (typeId == 0 && tag.size > 0) {
            error("NbtList cannot have NbtEnd")
        }

        output.writeByte(typeId.toByte())
        output.writeInt(tag.size, byteOrder)
        tag.forEach {
            serializer.writeTag(output, it)
        }
    }
}

@ExperimentalUnsignedTypes
private class NbtCompoundSerial(byteOrder: ByteOrder): NbtSerial<NbtCompound>(NbtCompound::class, byteOrder) {
    override fun readTag(input: Input): NbtCompound {
        val map = mutableMapOf<String, NbtTag>()
        while (true) {
            val typeId = input.readByte().toInt()
            if (typeId == 0) {
                break
            }

            val name = input.readUTF(byteOrder)
            val serializer = serializers[typeId]
            val childTag = serializer.readTag(input)
            map[name] = childTag
        }
        return NbtCompound(map)
    }

    override fun writeTag(output: Output, tag: NbtCompound) {
        check(tag.values.none { it == NbtEnd }) {
            "NbtCompound cannot have an NbtEnd"
        }

        tag.forEach { (name, childTag) ->
            val typeId = childTag.typeId
            val serializer = serializers[typeId]
            output.writeByte(typeId.toByte())
            output.writeUTF(name, byteOrder)
            serializer.writeTag(output, childTag)
        }

        output.writeByte(0)
    }
}

private class NbtIntArraySerial(byteOrder: ByteOrder): NbtSerial<NbtIntArray>(NbtIntArray::class, byteOrder) {
    override fun readTag(input: Input): NbtIntArray {
        val size = input.readInt(byteOrder)
        val array = IntArray(size)
        for (i in 0 until size) {
            array[i] = input.readInt(byteOrder)
        }
        return NbtIntArray(array)
    }

    override fun writeTag(output: Output, tag: NbtIntArray) {
        output.writeInt(tag.value.size, byteOrder)
        tag.value.forEach {
            output.writeInt(it, byteOrder)
        }
    }
}

private class NbtLongArraySerial(byteOrder: ByteOrder): NbtSerial<NbtLongArray>(NbtLongArray::class, byteOrder) {
    override fun readTag(input: Input): NbtLongArray {
        val size = input.readInt(byteOrder)
        val array = LongArray(size)
        for (i in 0 until size) {
            array[i] = input.readLong(byteOrder)
        }
        return NbtLongArray(array)
    }

    override fun writeTag(output: Output, tag: NbtLongArray) {
        output.writeInt(tag.value.size, byteOrder)
        tag.value.forEach {
            output.writeLong(it, byteOrder)
        }
    }
}

@ExperimentalUnsignedTypes
fun Input.readUTF(byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN): String {
    val len = readUShort(byteOrder).toInt()
    return readTextExactBytes(len)
}

@ExperimentalUnsignedTypes
fun Output.writeUTF(v: String, byteOrder: ByteOrder = ByteOrder.BIG_ENDIAN) {
    val bytes = v.toByteArray()
    writeUShort(bytes.size.toUShort(), byteOrder)
    writeFully(bytes)
}

@ExperimentalUnsignedTypes
fun Input.readUShort(byteOrder: ByteOrder): UShort {
    return readShort(byteOrder).toUShort()
}

@ExperimentalUnsignedTypes
fun Output.writeUShort(v: UShort, byteOrder: ByteOrder) {
    writeShort(v.toShort(), byteOrder)
}
