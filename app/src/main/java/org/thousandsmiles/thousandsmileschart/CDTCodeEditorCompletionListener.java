/*
 * (C) Copyright Syd Logan 2020
 * (C) Copyright Thousand Smiles Foundation 2020
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.thousandsmiles.thousandsmileschart;

import org.thousandsmiles.tscharts_lib.CDTCodesModel;

import java.util.ArrayList;

public interface CDTCodeEditorCompletionListener {
    void onCompletion(String tooth, boolean isMissing, ArrayList<CDTCodesModel> addedItems, ArrayList<CDTCodesModel> removedItems,
                      ArrayList<CDTCodesModel> completedItems, ArrayList<CDTCodesModel> uncompletedItems,
                      ArrayList<PatientDentalToothState> list);
    void onCancel();
}
