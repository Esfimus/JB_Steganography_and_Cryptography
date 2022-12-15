package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

/**
 * Saves buffered image to the output file
 */
fun saveImage(image: BufferedImage, imageFile: File) {
    ImageIO.write(image, "png", imageFile)
}

/**
 * Converts a text message to the list of bits
 * from bytes of every character in a message
 */
fun messageToBits(text: String): List<Int> {
    val byteArray = text.encodeToByteArray()
    val binaryList = mutableListOf<Int>()
    for (i in byteArray) {
        val oneByteList = mutableListOf<Int>()
        for (bit in 7 downTo 0) {
            val intI = i.toInt() shr bit
            oneByteList.add(intI and 1)
        }
        binaryList.addAll(oneByteList)
    }
    return binaryList
}

/**
 * Converts a list of bits to the text message
 */
fun bitsToMessage(list: List<Int>): String {
    var message = ""
    val listString = list.joinToString("")
    for (i in 0 until listString.length / 8) {
        val byteString = listString.substring(i * 8, i * 8 + 8)
        val intChar = Integer.parseInt(byteString, 2)
        message += intChar.toChar()
    }
    return message
}

/**
 * Encrypts the given message with the password with XOR operation
 */
fun encryption(message: String, password: String): List<Int> {
    val binListMessage = messageToBits(message)
    val binListPassword = messageToBits(password)
    val encryptedList = mutableListOf<Int>()
    for (i in binListMessage.indices) {
        encryptedList.add(binListMessage[i] xor binListPassword[i % binListPassword.size])
    }
    // adding finishing beacon
    val beaconStarting = MutableList(22) { 0 }
    val beaconFinishing = MutableList(2) { 1 }
    encryptedList.addAll(beaconStarting)
    encryptedList.addAll(beaconFinishing)
    return encryptedList
}

fun decryption(encryptedList: List<Int>, password: String): String {
    val binListPassword = messageToBits(password)
    val binListMessage = mutableListOf<Int>()
    for (i in encryptedList.indices) {
        binListMessage.add(encryptedList[i] xor binListPassword[i % binListPassword.size])
    }
    return bitsToMessage(binListMessage)
}

/**
 * Reads both input and output image names,
 * adds 1 to the least significant bit of three colors
 */
fun hideMessageInImage() {
    try {
        println("Input image file:")
        val inputFileName = readln()
        println("Output image file:")
        val outputFileName = readln()
        println("Message to hide:")
        val message = readln()
        println("Password:")
        val password = readln()
        // creating a buffered image from the input file
        val image: BufferedImage = ImageIO.read(File(inputFileName))
        val binListEncrypted = encryption(message, password)

        // checking if the image size is enough to hold the message
        if (image.height * image.width < binListEncrypted.size) {
            println("The input image is not large enough to hold this message.")
            return
        }
        // iterating through evey pixel and changing the last bit of blue color to 1 or 0 according to the message bits
        var i = 0
        loop@ for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixelColor = Color(image.getRGB(x, y))
                val newBlue: Int = if (binListEncrypted[i] == 0) {
                    // setting 0 for the last binary digit
                    pixelColor.blue and 1.inv()
                } else {
                    // setting 1 for the last binary digit
                    pixelColor.blue or 1
                }
                i++
                val newColor = Color(pixelColor.red, pixelColor.green, newBlue)
                image.setRGB(x, y, newColor.rgb)
                if (i == binListEncrypted.size) {
                    break@loop
                }
            }
        }
        saveImage(image, File(outputFileName))
        println("Message saved in $outputFileName image.")
    } catch(e: Exception) {
        println("Can't read input file!")
    }
}

/**
 * Reads input image name, extracts every last bit of blue color
 * and converts the list of bits to a hidden message
 */
fun showMessageFromImage() {
    try {
        println("Input image file:")
        val inputFileName = readln()
        println("Password:")
        val password = readln()
        val image: BufferedImage = ImageIO.read(File(inputFileName))
        val binListEncrypted = mutableListOf<Int>()
        loop@ for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val pixelColor = Color(image.getRGB(x, y))
                binListEncrypted.add(pixelColor.blue and 1)
                if (binListEncrypted.size > 24) {
                    if (
                        binListEncrypted
                            .joinToString("")
                            .substring(binListEncrypted.size - 24) == "000000000000000000000011"
                    ) {
                        break@loop
                    }
                }
            }
        }
        // cleaning the list of bits, removing last 24 signal bits
        val clearedList = mutableListOf<Int>()
        for (i in 0..binListEncrypted.size - 25) {
            clearedList.add(binListEncrypted[i])
        }
        println("Message:\n${decryption(clearedList, password)}")
    } catch(e: Exception) {
        println("Can't read input file!")
    }
}

/**
 * Steganography and cryptography app
 */
fun cryptoApp() {
    var userInput = ""
    do {
        println("Task (hide, show, exit):")
        userInput = readln()
        when (userInput) {
            "hide" -> hideMessageInImage()
            "show" -> showMessageFromImage()
            "exit" -> println("Bye!")
            else -> println("Wrong task: $userInput")
        }
    } while (userInput != "exit")
}

fun main() {
    cryptoApp()
}