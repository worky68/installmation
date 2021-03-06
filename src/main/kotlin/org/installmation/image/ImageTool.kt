/*
 * Copyright 2019 Serge Merzliakov
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.installmation.image

import javafx.embed.swing.SwingFXUtils
import javafx.scene.image.Image
import net.sf.image4j.codec.ico.ICODecoder
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.File
import java.io.FileInputStream
import javax.imageio.ImageIO

/**
 * Useful Image functions
 */
object ImageTool {

   enum class ImageType(val value: String) {
      Png("png"),
      Jpeg("jpeg"),
      Jpg("jpg"),
      Icns("icns"),
      Ico("ico")
   }

   class Dimension(val width: Int, val height: Int)

   /**
    * Duplicates the image with new size
    */
   fun newImageWithSize(type: ImageType, imageFile: File, outputName: String, outputPath: File, newWidth: Int, newHeight: Int): File {
      try {
         val inputImage: BufferedImage = ImageIO.read(imageFile)
         val outputImage = BufferedImage(newWidth, newHeight, inputImage.type)

         val g2d = outputImage.createGraphics()
         g2d.drawImage(inputImage, 0, 0, newWidth, newHeight, null)
         g2d.dispose()

         val outputFile = File(outputPath, outputName)
         ImageIO.write(outputImage, type.name.toLowerCase(), outputFile)
         return outputFile
      } catch (e: Exception) {
         throw ImageProcessingException("Error creating new ${type.name.toLowerCase()} image from ${imageFile.path}", e)
      }
   }

   /**
    * return width and height of image file
    */
   fun imageDimensions(f: File): Dimension {
      try {
         val imageTest: ByteArray = f.readBytes()
         val bs = ByteArrayInputStream(imageTest)
         val bi: BufferedImage = ImageIO.read(bs)
         return Dimension(bi.width, bi.height)
      } catch (e: Exception) {
         throw ImageProcessingException("Error determine image dimensions of ${f.path}", e)
      }
   }

   /**
    * return comma delimited list of supported image types
    */
   fun validImageTypes(): String {
      val images = StringBuilder()
      for (t in ImageType.values()) {
         images.append(t.value).append(',')
      }
      images.setLength(images.length - 1)
      return images.toString()
   }

   /**
    * Return a regex for matching supported image types
    */
   fun imageExtensionsRegex(): Regex {
      val regex = StringBuilder()
      for (t in ImageType.values()) {
         regex.append(t.value).append('|')
      }
      regex.setLength(regex.length - 1)
      return Regex(regex.toString(), RegexOption.IGNORE_CASE)
   }

   /**
    * return true if the file is a real, non-empty image file
    */
   fun isValidImageFile(f: File): Boolean {
      return f.exists() && f.isFile && f.extension.matches(imageExtensionsRegex()) && f.length() > 0
   }

   fun createImage(f: File): Image {
      return when (f.extension.toLowerCase()) {
         ImageType.Png.value, ImageType.Jpeg.value, ImageType.Jpg.value -> Image(FileInputStream(f))
         ImageType.Icns.value -> icnsDefaultImage()
         ImageType.Ico.value -> extractICOImage(f)
         else -> Image(FileInputStream(f))
      }
   }

   /**
    * Extracting ICNS images is a pain in the ass as of December 2019
    * Apache commons-image fails on a lots of ICNS files.
    * So just show an placeholder for now.
    */
   private fun icnsDefaultImage(): Image {
      val bufferedImage = ImageIO.read(javaClass.getResource("/image/icns.png"))
      return SwingFXUtils.toFXImage(bufferedImage, null)
   }

   private fun extractICOImage(f: File): Image {
      val images = ICODecoder.read(f)
      return SwingFXUtils.toFXImage(images[0], null)
   }
}
