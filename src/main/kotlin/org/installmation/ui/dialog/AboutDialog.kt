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

package org.installmation.ui.dialog

import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.layout.Pane
import javafx.stage.Stage
import org.installmation.core.InstallmationVersion

/**
 * Simple about dialog. Still needs proper setting of build and version
 */
class AboutDialog(parentStage: Stage) : CustomDialog<String>(parentStage, "About Installmation") {

   @FXML private lateinit var versionLabel: Label

   init {
      val loader = FXMLLoader(javaClass.classLoader.getResource("fxml/dialog/aboutDialog.fxml"))
      loader.setController(this)
      val root = loader.load<Pane>()
      versionLabel.text = InstallmationVersion.version()
      stage.scene = Scene(root)
      stage.isResizable = false
   }

   override fun result(): DialogResult<String> {
      return DialogResult(true, "")
   }

   @FXML
   fun close() {
      stage.close()
   }
}