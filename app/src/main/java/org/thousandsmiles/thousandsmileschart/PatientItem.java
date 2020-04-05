/*
 * (C) Copyright Syd Logan 2017
 * (C) Copyright Thousand Smiles Foundation 2017
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

import org.json.JSONObject;

public class PatientItem {
    public final String id;
    public boolean isNext = false;
    public final String content;
    public final String details;
    public final JSONObject pObject;

    public PatientItem(String id, String content, String details, JSONObject pObject, boolean isNext) {
        this.id = id;
        this.content = content;
        this.details = details;
        this.pObject = pObject;
        this.isNext = isNext;
    }

    @Override
    public String toString() {
        return content;
    }
}

