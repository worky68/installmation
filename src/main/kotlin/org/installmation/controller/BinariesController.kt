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
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.fxml.FXML
import javafx.scene.control.Button
import javafx.scene.control.ComboBox
import javafx.stage.Stage
import org.apache.logging.log4j.LogManager
import org.apache.logging.log4j.Logger
import org.installmation.configuration.Configuration
import org.installmation.configuration.UserHistory
import org.installmation.core.OperatingSystem
import org.installmation.javafx.ComboUtils
import org.installmation.javafx.EventUtils
import org.installmation.model.JDKListUpdatedEvent
import org.installmation.model.ModuleJmodUpdatedEvent
import org.installmation.model.ModuleLibUpdatedEvent
import org.installmation.model.NamedDirectory
import org.installmation.model.binary.JDK
import org.installmation.model.binary.JDKFactory
import org.installmation.service.ProjectBeginSaveEvent
import org.installmation.service.ProjectClosedEvent
import org.installmation.service.ProjectLoadedEvent
import org.installmation.service.Workspace
import org.installmation.ui.dialog.BinaryArtefactDialog
import org.installmation.ui.dialog.HelpDialog


class BinariesController(private val configuration: Configuration,
                         private val userHistory: UserHistory,
                         private val workspace: Workspace) {

   companion object {
      val log: Logger = LogManager.getLogger(BinariesController::class.java)
      const val PROPERTY_HELP_FX_LIBS = "help.fx.libs"
      const val PROPERTY_HELP_FX_MODULES = "help.fx.modules"
      const val PROPERTY_HELP_JPACKAGE = "help.fx.jpackage"
      const val PROPERTY_HELP_JDK = "help.fx.jdk"
      const val TITLE_HELP_FX_LIBRARIES = "JavaFX Libraries"
      const val TITLE_HELP_FX_MODULES = "JavaFX JMod Files"
      const val TITLE_HELP_JPACKAGE = "jpackage"
      const val TITLE_HELP_JDK = "JDK to Deploy"
   }

   // combos
   @FXML private lateinit var moduleJmodComboBox: ComboBox<NamedDirectory>
   @FXML private lateinit var moduleLibComboBox: ComboBox<NamedDirectory>
   @FXML private lateinit var installJDKComboBox: ComboBox<JDK>
   @FXML private lateinit var jpackageComboBox: ComboBox<JDK>

   // buttons
   @FXML private lateinit var configureJPackageButton: Button
   @FXML private lateinit var configureModuleJmodButton: Button
   @FXML private lateinit var configureModuleLibrariesButton: Button

   // help
   @FXML private lateinit var helpFXLibrariesButton: Button
   @FXML private lateinit var helpFXModulesButton: Button
   @FXML private lateinit var helpJpackageButton: Button
   @FXML private lateinit var helpJDKButton: Button


   // model loaded from configuration
   private val jpackageJDKItems: ObservableList<JDK> = FXCollections.observableArrayList()
   private val installJDKItems: ObservableList<JDK> = FXCollections.observableArrayList()
   private val moduleLibItems: ObservableList<NamedDirectory> = FXCollections.observableArrayList()
   private val moduleJmodItems: ObservableList<NamedDirectory> = FXCollections.observableArrayList()

   init {
      configuration.eventBus.register(this)
   }

   @FXML
   fun initialize() {
      initializeConfiguredBinaries()

      EventUtils.selectionChangedHandler(moduleJmodComboBox) { updateProject() }
      EventUtils.selectionChangedHandler(moduleLibComboBox) { updateProject() }
      EventUtils.selectionChangedHandler(installJDKComboBox) { updateProject() }
      EventUtils.selectionChangedHandler(jpackageComboBox) { updateProject() }
   }

   /**
    * JDKs and drop down lists
    */
   private fun initializeConfiguredBinaries() {
      jpackageJDKItems.addAll(configuration.jdkEntries.values)
      jpackageComboBox.items = jpackageJDKItems.sorted()

      installJDKItems.addAll(configuration.jdkEntries.values)
      installJDKComboBox.items = installJDKItems.sorted()

      moduleLibItems.addAll(configuration.javafxLibEntries.entries.map { NamedDirectory(it.key, it.value) })
      moduleLibComboBox.items = moduleLibItems.sorted()
      moduleLibComboBox.converter = StringConverterFactory.namedItemConverter(moduleLibComboBox.items)

      moduleJmodItems.addAll(configuration.javafxModuleEntries.entries.map { NamedDirectory(it.key, it.value) })
      moduleJmodComboBox.items = moduleJmodItems.sorted()
      moduleJmodComboBox.converter = StringConverterFactory.namedItemConverter(moduleJmodComboBox.items)
   }

   /**
    * Only fire this on true selection updates, rather than on action occurring the combo
    * boxes, otherwise his gets fires on project load unnecessarily (loads fire on action
    * events)
    */
   @FXML
   fun updateProject() {
      workspace.save()
   }

   @FXML
   fun configureModuleLibraries() {
      val dialog = moduleLibraryDialog()
      val result = dialog.showAndWait()
      if (result.ok) {
         ComboUtils.comboSelect(moduleLibComboBox, result.data?.name)
         // update model
         val updatedModel = dialog.updatedModel()
         if (updatedModel != null) {
            configuration.eventBus.post(ModuleLibUpdatedEvent(updatedModel))
         }
      }
   }

   @FXML
   fun configureJPackageBinaries() {
      updateJDKList(jpackageComboBox)
   }

   @FXML
   fun configureInstallJDK() {
      updateJDKList(installJDKComboBox)
   }

   @FXML
   fun configureModuleJmods() {
      val dialog = moduleJmodDialog()
      val result = dialog.showAndWait()
      if (result.ok) {
         ComboUtils.comboSelect(moduleJmodComboBox, result.data?.name)
         // update model
         val updatedModel = dialog.updatedModel()
         if (updatedModel != null) {
            configuration.eventBus.post(ModuleJmodUpdatedEvent(updatedModel))
         }
      }
   }

   @FXML
   fun helpFXLibraries() {
      
      HelpDialog.showAndWait(TITLE_HELP_FX_LIBRARIES, configuration.resourceBundle.getString(PROPERTY_HELP_FX_LIBS))
   }

   @FXML
   fun helpFXModules() {
      HelpDialog.showAndWait(TITLE_HELP_FX_MODULES, configuration.resourceBundle.getString(PROPERTY_HELP_FX_MODULES))
   }

   @FXML
   fun helpJpackage() {
      HelpDialog.showAndWait(TITLE_HELP_JPACKAGE, configuration.resourceBundle.getString(PROPERTY_HELP_JPACKAGE))
   }

   @FXML
   fun helpJDK() {
      HelpDialog.showAndWait(TITLE_HELP_JDK, configuration.resourceBundle.getString(PROPERTY_HELP_JDK))
   }

   /**
    * Update list of known JDKs. Used for both JPackager access, and
    * setting the JDK to add to installer.
    */
   private fun updateJDKList(combo: ComboBox<JDK>) {
      val dialog = jdkDialog()
      val result = dialog.showAndWait()
      if (result.ok) {
         ComboUtils.comboSelect(combo, result.data?.name)

         val updatedModel = dialog.updatedModel()
         if (updatedModel != null) {
            configuration.eventBus.post(JDKListUpdatedEvent(updatedModel))
         }
      }
   }

   private fun jdkDialog(): BinaryArtefactDialog {
      val items = jpackageJDKItems.map { NamedDirectory(it.name, it.path) }
      return BinaryArtefactDialog(applicationStage(), "JPackager JDKs", items, userHistory)
   }

   private fun moduleLibraryDialog(): BinaryArtefactDialog {
      val items = moduleLibItems.map { NamedDirectory(it.name, it.path) }
      return BinaryArtefactDialog(applicationStage(), "JavaFX Library Directories", items, userHistory)
   }

   private fun moduleJmodDialog(): BinaryArtefactDialog {
      val items = moduleJmodItems.map { NamedDirectory(it.name, it.path) }
      return BinaryArtefactDialog(applicationStage(), "JavaFX Module Directories", items, userHistory)
   }

   private fun applicationStage(): Stage {
      return configureJPackageButton.scene.window as Stage
   }

   //-------------------------------------------------------
   //  Event Subscribers
   //-------------------------------------------------------

   @Subscribe
   fun handleProjectBeginSave(e: ProjectBeginSaveEvent) {
      checkNotNull(e.project)
      // do not clear project path collections - other event handlers will be adding things
      // as well
      e.project.installJDK = installJDKComboBox.selectionModel.selectedItem
      e.project.jpackageJDK = jpackageComboBox.selectionModel.selectedItem
      e.project.javaFXLib = moduleLibComboBox.selectionModel.selectedItem
      e.project.javaFXMods = moduleJmodComboBox.selectionModel.selectedItem
   }

   @Subscribe
   fun handleProjectLoaded(e: ProjectLoadedEvent) {
      checkNotNull(e.project)
      jpackageComboBox.selectionModel.select(e.project.jpackageJDK)
      installJDKComboBox.selectionModel.select(e.project.installJDK)
      moduleJmodComboBox.selectionModel.select(e.project.javaFXMods)
      moduleLibComboBox.selectionModel.select(e.project.javaFXLib)
   }
   
   @Subscribe
   fun handleProjectClosed(e: ProjectClosedEvent) {
      jpackageComboBox.selectionModel.clearSelection()
      installJDKComboBox.selectionModel.clearSelection()
      moduleJmodComboBox.selectionModel.clearSelection()
   }

   /**
    * Clear out and refresh list of JDKs from this controller's model
    * and configuration
    */
   @Subscribe
   fun handleJDKUpdated(e: JDKListUpdatedEvent) {
      jpackageJDKItems.clear()
      installJDKItems.clear()
      configuration.jdkEntries.clear()
      e.updated.map {
         val jdk = JDKFactory.create(OperatingSystem.os(), it.name, it.path)
         jpackageJDKItems.add(jdk)
         installJDKItems.add(jdk)
         configuration.jdkEntries[it.name] = jdk
      }
   }

   /**
    * Clear out and refresh list of FX modules from this controller's model
    * and configuration
    */
   @Subscribe
   fun handleModuleJmodUpdated(e: ModuleJmodUpdatedEvent) {
      moduleJmodItems.clear()
      configuration.javafxModuleEntries.clear()
      e.updated.map {
         val nd = NamedDirectory(it.name, it.path)
         moduleJmodItems.add(nd)
         configuration.javafxModuleEntries[it.name] = it.path
      }
   }

   /**
    * Clear out and refresh list of FX modules from this controller's model
    * and configuration
    */
   @Subscribe
   fun handleModuleLibUpdated(e: ModuleLibUpdatedEvent) {
      moduleLibItems.clear()
      configuration.javafxLibEntries.clear()
      e.updated.map {
         val nd = NamedDirectory(it.name, it.path)
         moduleLibItems.add(nd)
         configuration.javafxLibEntries[it.name] = it.path
      }
   }
}




