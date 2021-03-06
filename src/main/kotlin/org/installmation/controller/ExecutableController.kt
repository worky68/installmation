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

package org.installmation.controller

import com.google.common.eventbus.Subscribe
import javafx.fxml.FXML
import javafx.scene.control.TextField
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.installmation.configuration.Configuration
import org.installmation.configuration.UserHistory
import org.installmation.javafx.FileFieldUtils
import org.installmation.service.*
import org.installmation.ui.dialog.ChooseFileDialog
import org.installmation.ui.dialog.InstallmationExtensionFilters


class ExecutableController(configuration: Configuration,
                           private val userHistory: UserHistory,
                           private val workspace: Workspace) {

   companion object {
      val log: Logger = LogManager.getLogger(ExecutableController::class.java)
   }

   @FXML private lateinit var mainJarField: TextField
   @FXML private lateinit var mainClassField: TextField

   init {
      configuration.eventBus.register(this)
   }

   @FXML
   fun initialize() {
   }

   @FXML
   fun chooseMainJar() {
      val result = ChooseFileDialog.showAndWait(mainJarField.scene.window as Stage, "Select Main Application Jar File", userHistory, InstallmationExtensionFilters.jarFilter())
      if (result.ok) {
         mainJarField.text = result.data!!.path
      }
   }

   @FXML
   fun updateProject() {
      workspace.save()
   }

   //-------------------------------------------------------
   //  Event Subscribers
   //-------------------------------------------------------

   @Subscribe
   fun handleProjectCreated(e: ProjectCreatedEvent) {
   }
   
   @Subscribe
   fun handleProjectBeginSave(e: ProjectBeginSaveEvent) {
      checkNotNull(e.project)

      e.project.mainJar = FileFieldUtils.getPath(mainJarField)
      e.project.mainClass = mainClassField.text
   }

   @Subscribe
   fun handleProjectLoaded(e: ProjectLoadedEvent) {
      checkNotNull(e.project)
      mainJarField.text = e.project.mainJar?.path
      mainClassField.text = e.project.mainClass
   }

   @Subscribe
   fun handleProjectClosed(e: ProjectClosedEvent) {
      mainJarField.text = null
      mainClassField.text = null
   }

}




