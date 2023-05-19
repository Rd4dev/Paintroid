/*
 * Paintroid: An image manipulation application for Android.
 * Copyright (C) 2010-2015 The Catrobat Team
 * (<http://developer.catrobat.org/credits>)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.catrobat.paintroid.iotasks

import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.catrobat.paintroid.FileIO
import org.catrobat.paintroid.command.serialization.CommandSerializer
import org.catrobat.paintroid.contract.LayerContracts
import org.catrobat.paintroid.model.Project
import org.catrobat.paintroid.projectDB
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.*

class SaveImage(
    activity: SaveImageCallback,
    private val requestCode: Int,
    private val layerModel: LayerContracts.Model,
    private val commandSerializer: CommandSerializer,
    private var uri: Uri?,
    private val saveAsCopy: Boolean,
    private val context: Context,
    private val scopeIO: CoroutineScope,
    private val idlingResource: CountingIdlingResource
) {
    private val callbackRef: WeakReference<SaveImageCallback> = WeakReference(activity)

    companion object {
        private val TAG = SaveImage::class.java.simpleName
    }

    private fun getImageUri(
        callback: SaveImageCallback,
        bitmap: Bitmap?
    ): Uri? {
        Log.d("saveimage", "getImageUri: in getImageUri")
        val filename = FileIO.defaultFileName
        return if (uri == null) {
//            Log.d("saveimage", "getImageUri: if")
//            Log.d("saveimage", "getImageUri: filename - $filename")
//            Log.d("saveimage", "getImageUri: callback content resolver - ${callback.contentResolver}")
//            Log.d("saveimage", "getImageUri: context - $context")
//            Log.d("saveimage", "getImageUri: save - ${FileIO.saveBitmapToFile(filename, bitmap, callback.contentResolver, context)}")
            val imageUri = FileIO.saveBitmapToFile(filename, bitmap, callback.contentResolver, context)
//            Log.d("saveimage", "getImageUri: if imageUri - $imageUri")
            imageUri
        } else {
//            Log.d("saveimage", "getImageUri: else")
            uri?.let { FileIO.saveBitmapToUri(it, bitmap, context) }
        }
    }

    private fun saveOraFile(
        layers: List<LayerContracts.Layer>,
        uri: Uri,
        fileName: String,
        bitmap: Bitmap?,
        contentResolver: ContentResolver?
    ): Uri? = try {
        OpenRasterFileFormatConversion.saveOraFileToUri(
            layers,
            uri,
            fileName,
            bitmap,
            contentResolver
        )
    } catch (e: IOException) {
        Log.d(TAG, "Can't save image file ${e.message}")
        null
    }

    private fun exportOraFile(
        layers: List<LayerContracts.Layer>,
        fileName: String,
        bitmap: Bitmap?,
        contentResolver: ContentResolver?
    ): Uri? = try {
        OpenRasterFileFormatConversion.exportToOraFile(
            layers,
            fileName,
            bitmap,
            contentResolver
        )
    } catch (e: IOException) {
        Log.d(TAG, "Can't save image file ${e.message}")
        null
    }

    @SuppressWarnings("TooGenericExceptionCaught")
    fun execute() {
        val callback = callbackRef.get()
        if (callback == null || callback.isFinishing) {
            return
        } else {
            callback.onSaveImagePreExecute(requestCode)
        }

        var currentUri: Uri? = null
        scopeIO.launch {
            try {
                idlingResource.increment()
                val bitmap = layerModel.getBitmapOfAllLayers()
                Log.d("saveimage", "execute: bitmap - $bitmap")
                val filename = FileIO.defaultFileName
                Log.d("saveimage", "execute: filename - $filename")

                val dbp = projectDB.dao.getProjects()

                currentUri = if (FileIO.fileType == FileIO.FileType.ORA) {
                    val layers = layerModel.layers
                    if (uri != null && filename.endsWith(FileIO.FileType.ORA.toExtension())) {
                        uri?.let {
                            saveOraFile(layers, it, filename, bitmap, callback.contentResolver)
                        }
                    } else {
                        val imageUri = exportOraFile(layers, filename, bitmap, callback.contentResolver)
                        imageUri
                    }
                } else if (FileIO.fileType == FileIO.FileType.CATROBAT) {
                    Log.d("saveimage", "execute: catrobat")
//                    Log.d("saveimage", "execute: projects in DB - $dbp")

                    if (uri != null) {
                        uri?.let {
                            commandSerializer.overWriteFile(filename, it, callback.contentResolver)
                        }
                    } else {
                        /*projectDB.dao.insertProject(Project("saveImageTest", uri.toString(), Calendar.getInstance().time.toString(), Calendar.getInstance().time.toString(), "", FileIO.fileType.toString(), 0, getImageUri(callback, bitmap).toString()))
                        Log.d("saveimage", "execute: projects in DB - $dbp")*/
                        commandSerializer.writeToFile(filename)
                    }
                } else {
//                    Log.d("saveimage", "execute: else")
//                    Log.d("saveimage", "execute: bitmap - $bitmap")
//                    Log.d("saveimage", "execute: callback - $callback")
//                    Log.d("saveimage", "execute: getImageUri - ${getImageUri(callback, bitmap)}")
                    getImageUri(callback, bitmap)
                }

//                projectDB.dao.insertProject(Project("saveImageTest", uri.toString(), Calendar.getInstance().time.toString(), Calendar.getInstance().time.toString(), "", FileIO.fileType.toString(), 0, getImageUri(callback, bitmap).toString()))
//                Log.d("saveimage", "execute: bitmap - $bitmap")
//                Log.d("saveimage", "execute: callback - $callback")
//                getImageUri(callback, bitmap)
//                Log.d("saveimage", "execute: getImageUri - ${getImageUri(callback, bitmap)}")
                projectDB.dao.insertProject(Project(filename, uri.toString(), Calendar.getInstance().time.toString(), Calendar.getInstance().time.toString(), "", FileIO.fileType.toString(), 0, "paintroid/imagePreviewTestPath"))
                Log.d("saveimage", "execute: here!")

                idlingResource.decrement()
            } catch (e: Exception) {
                idlingResource.decrement()
                when (e) {
                    is IOException -> Log.d(TAG, "Can't save image file", e)
                    is NullPointerException -> Log.e(TAG, "Can't load image file", e)
                }
            }

            withContext(Dispatchers.Main) {
                if (!callback.isFinishing) {
                    callback.onSaveImagePostExecute(requestCode, currentUri, saveAsCopy)
                }
            }
        }
    }

    interface SaveImageCallback {
        val contentResolver: ContentResolver
        val isFinishing: Boolean
        fun onSaveImagePreExecute(requestCode: Int)
        fun onSaveImagePostExecute(requestCode: Int, uri: Uri?, saveAsCopy: Boolean)
    }
}
