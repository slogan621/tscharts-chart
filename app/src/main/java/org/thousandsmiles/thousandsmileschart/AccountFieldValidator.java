/*
 * (C) Copyright Syd Logan 2021
 * (C) Copyright Thousand Smiles Foundation 2021
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

public class AccountFieldValidator {
    public boolean isValidEmail(String str) {
        String regex = "^[\\w-_\\.+]*[\\w-_\\.]\\@([\\w]+\\.)+[\\w]+[\\w]$";
        return str.matches(regex);
    }

    public boolean isValidPassword(String str) {
        boolean ret = true;
        int len = str.length();
        boolean hasUpper = !str.equals(str.toLowerCase());
        boolean hasSpecial = !str.matches("[A-Za-z0-9 ]*");
        boolean hasDigit = str.matches(".*\\d.*");

        return len >= 8 && hasUpper && hasSpecial && hasDigit;
    }
}
