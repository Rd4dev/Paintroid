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
import java.text.SimpleDateFormat
import java.util.*

class SaveImage(
    activity: SaveImageCallback,
    private val requestCode: Int,
    private val layerModel: LayerContracts.Model,
    private val commandSerializer: CommandSerializer,
    private var uri: Uri?,
    private val saveAsCopy: Boolean,
    private val saveProject: Boolean,
    private val context: Context,
    private val scopeIO: CoroutineScope,
    private val idlingResource: CountingIdlingResource
) {
    private val callbackRef: WeakReference<SaveImageCallback> = WeakReference(activity)

    companion object {
        private val TAG = SaveImage::class.java.simpleName
    }

    var imagePreviewPath: Uri? = null

    private fun getImageUri(
        callback: SaveImageCallback,
        bitmap: Bitmap?
    ): Uri? {
        Log.d("getimageuricall", "getImageUri: called")
        Log.d("imagepreview", "getImageUri: in getImageUri")
        val filename = FileIO.defaultFileName
        return if (uri == null) {
            Log.d("imagepreview", "getImageUri: in uri == null")
//            Log.d("saveimage", "getImageUri: if")
//            Log.d("saveimage", "getImageUri: filename - $filename")
//            Log.d("saveimage", "getImageUri: callback content resolver - ${callback.contentResolver}")
//            Log.d("saveimage", "getImageUri: context - $context")
//            Log.d("saveimage", "getImageUri: save - ${FileIO.saveBitmapToFile(filename, bitmap, callback.contentResolver, context)}")
            Log.d("imagepreview", "getImageUri: filename - $filename")
            Log.d("imagepreview", "getImageUri: bitmap - $bitmap")
            Log.d("imagepreview", "getImageUri: callback - $callback")
            Log.d("imagepreview", "getImageUri: callback contentResolver - ${callback.contentResolver}")
            Log.d("imagepreview", "getImageUri: context - $context")
            val imageUri = FileIO.saveBitmapToFile(filename.replace(".catrobat-image", ".png"), bitmap, callback.contentResolver, context)
            Log.d("imagepreview", "getImageUri: in uri - $imageUri")
//            Log.d("saveimage", "getImageUri: if imageUri - $imageUri")
            Log.d("getimageuricall", "getImageUri: imageuri - $imageUri")
            imageUri
        } else {
//            Log.d("saveimage", "getImageUri: else")
            Log.d("imagepreview", "getImageUri: in else")
            uri?.let {
                Log.d("imagepreview", "getImageUri: Saving bitmap to uri - $uri")
                Log.d("getimageuricall", "getImageUri: uri - $uri")
//                FileIO.saveBitmapToUri(it, bitmap, context)
                FileIO.saveBitmapToFile(filename.replace(".catrobat-image", ".png"), bitmap, callback.contentResolver, context)
            }
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

                currentUri =
                    if(saveProject == true){
                        FileIO.fileType = FileIO.FileType.CATROBAT
                        currentUri = if (uri != null) {
                            uri?.let {
                                Log.d("storeimageuri", "currentUri: Uri - $uri")
                                commandSerializer.overWriteFile(filename, it, callback.contentResolver)
                            }
                        } else {
                            /*projectDB.dao.insertProject(Project("saveImageTest", uri.toString(), Calendar.getInstance().time.toString(), Calendar.getInstance().time.toString(), "", FileIO.fileType.toString(), 0, getImageUri(callback, bitmap).toString()))
                            Log.d("saveimage", "execute: projects in DB - $dbp")*/
                            commandSerializer.writeToFile(filename)
                        }
                        imagePreviewPath = getImageUri(callback, bitmap)
                        val date = Calendar.getInstance().time.toString()
                        Log.d("storeimageuri", "handleRequestPermissionsResult: Image Preview Path - ${imagePreviewPath}")
                        if (uri != null) {
                            uri?.let {
                                Log.d("storeimageuri", "handleRequestPermissionsResult: FileIO in if - ${FileIO.storeImageUri}")
                                projectDB.dao.updateProjectUri(filename, imagePreviewPath.toString(), currentUri.toString())
                                commandSerializer.overWriteFile(filename, it, callback.contentResolver)
                            }
                        } else {
                            /*projectDB.dao.insertProject(Project("saveImageTest", uri.toString(), Calendar.getInstance().time.toString(), Calendar.getInstance().time.toString(), "", FileIO.fileType.toString(), 0, getImageUri(callback, bitmap).toString()))
                            Log.d("saveimage", "execute: projects in DB - $dbp")*/
                            Log.d("storeimageuri", "handleRequestPermissionsResult: FileIO in else - ${FileIO.storeImageUri}")
                            Log.d("imagepreview", "execute: here after imagepreviewpath- $imagePreviewPath")
                            Log.d("imagepreview", "execute: here after imagepreviewpath- $imagePreviewPath")

                            projectDB.dao.insertProject(
                                Project(
                                    filename,
                                    currentUri.toString(),
                                    date,
                                    date,
                                    "",
                                    FileIO.fileType.toString(),
                                    0,
                                    imagePreviewPath.toString()
                                )
                            )
                            commandSerializer.writeToFile(filename)
                        }
                    }
                    else if (FileIO.fileType == FileIO.FileType.ORA) {
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


                /*if(saveProject == true) {
//                    Log.d("projecturi", "execute: uri - $uri")
                    Log.d("projecturi", "execute: currenturi - $currentUri")
                    Log.d("projecturi", "execute: image path - $imagePreviewPath")

                    //date time format
//                    val inputFormat = SimpleDateFormat("EEE MMM dd HH:mm:ss zzz yyyy")
//                    val outputFormat = SimpleDateFormat("dd/MM/yyyy")
//                    val date = inputFormat.parse(Calendar.getInstance().time.toString())
                    val date = Calendar.getInstance().time.toString()
//                    val formattedDate = outputFormat.format(date)

                    if (FileIO.checkFileExists(
                            FileIO.FileType.CATROBAT,
                            filename,
                            context.contentResolver
                        )
                    ) {
                        Log.d("storeimageuri", "In update project - Filename: $filename")
                        Log.d("storeimageuri", "In update project - Filename: ${FileIO.checkFileExists(FileIO.FileType.CATROBAT, filename, context.contentResolver)}")
                        projectDB.dao.updateProjectUri(filename, imagePreviewPath.toString(), currentUri.toString())
                    } else {
                        Log.d("storeimageuri", "In insert project")
                        projectDB.dao.insertProject(
                            Project(
                                filename,
                                currentUri.toString(),
                                date,
                                date,
                                "",
                                FileIO.fileType.toString(),
                                0,
                                imagePreviewPath.toString()
                            )
                        )
                        Log.d("saveimage", "execute: here!")
                    }
                }*/


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
