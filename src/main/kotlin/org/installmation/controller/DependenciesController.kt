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

import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.ContextMenu
import javafx.scene.control.ListView
import javafx.scene.control.MenuItem
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.installmation.configuration.Configuration
import org.installmation.configuration.UserHistory
import org.installmation.model.Workspace
import org.installmation.service.ProjectService
import org.installmation.ui.dialog.ChooseDirectoryDialog
import org.installmation.ui.dialog.SimpleListItemDeleter


class DependenciesController(private val configuration: Configuration,
                             private val userHistory: UserHistory,
                             private val workspace: Workspace,
                             private val projectService: ProjectService) {

   companion object {
      val log: Logger = LogManager.getLogger(DependenciesController::class.java)
   }

   @FXML lateinit var classPathListView: ListView<String>
   @FXML lateinit var moduleListView: ListView<String>
   @FXML lateinit var classpathListContextMenu: ContextMenu
   @FXML lateinit var moduleListContextMenu: ContextMenu

   // model
   private val classpathItems: ObservableList<String> = FXCollections.observableArrayList<String>()
   private val moduleItems: ObservableList<String> = FXCollections.observableArrayList<String>()

   init {
      configuration.eventBus.register(this)
   }

   @FXML
   fun initialize() {
      classPathListView.items = classpathItems.sorted()
      moduleListView.items = moduleItems.sorted()

      initializeContextMenus()
   }

   private fun initializeContextMenus() {
      val removeClassPathItem = MenuItem("Remove")
      removeClassPathItem.onAction = SimpleListItemDeleter(classPathListView, classpathItems)
      classpathListContextMenu.items.add(removeClassPathItem)

      val removeModuleItem = MenuItem("Remove")
      removeModuleItem.onAction = SimpleListItemDeleter(moduleListView, moduleItems)
      moduleListContextMenu.items.add(removeModuleItem)
   }

   @FXML
   fun addClasspathItem() {
      val result = ChooseDirectoryDialog.showAndWait(moduleListView.scene.window as Stage, "Add Classpath Item", userHistory)
      if (result.ok) {
         classpathItems.add(result.data!!.path)
         // TODO - Here
         //workspace.currentProject

         log.debug("Added ${result.data.path} to classpath")
      }
   }

   @FXML
   fun addModuleItem() {
      val result = ChooseDirectoryDialog.showAndWait(moduleListView.scene.window as Stage, "Add Module Item", userHistory)
      if (result.ok) {
         moduleItems.add(result.data!!.path)
         // TODO - Here
         //workspace.currentProject
         log.debug("Added ${result.data.path} to modules")
      }
   }

   //-------------------------------------------------------
   //  Event Subscribers
   //-------------------------------------------------------

}



